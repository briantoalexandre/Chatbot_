package org.ldv.chatbot.controller

import org.ldv.chatbot.model.dao.*
import org.ldv.chatbot.model.entity.*
import org.ldv.chatbot.service.IAServiceFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.time.LocalDateTime
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UsernameNotFoundException


@Controller
class ChatController(
    private val iaServiceFactory: IAServiceFactory,
    private val conversationDAO: ConversationDAO,
    private val messageDAO: MessageDAO,
    private val modeleIADAO: ModeleIADAO,
    private val utilisateurDAO: UtilisateurDAO
) {
    // Page d'accueil : liste des conversations
    @GetMapping("/")
    fun accueil(
        model: Model,
        authentication: Authentication
    ): String {
        //Recupere l'utilisateur de la session
        val utilisateur = this.getUtilisateurConnecte(authentication)
        // Ajout de l'utilisateur à la vue
        model.addAttribute("utilisateur", utilisateur)
        model.addAttribute("conversation", null) //conversationDAO.findAllByUtilisateurOrderByDateDerniereModificationDesc(utilisateur)
        model.addAttribute("modeles", modeleIADAO.findAll())
        model.addAttribute("messages", emptyList<Message>())
        model.addAttribute(
            "conversations",
            conversationDAO.findAllByOrderByDateDerniereModificationDesc()
        )
        return "utilisateur/chat"
    }

    // Création d'une nouvelle conversation
    @PostMapping("/conversations")
    fun creerConversation(
        @RequestParam prompt: String,
        @RequestParam idModele: Long,
        redirectAttributes: RedirectAttributes,
        authentication: Authentication
    ): String {
        // Recupere l'utilisateur
        val utilisateur = this.getUtilisateurConnecte(authentication)
        val modele = modeleIADAO.findById(idModele)
            .orElseThrow {
                ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Modèle IA introuvable"
                )
            }

        val conversation = Conversation(
            titre = prompt.take(50),
            dateCreation = LocalDateTime.now(),
            modeleIA = modele,
            utilisateur = utilisateur
        )
        val conversationEnregistree = conversationDAO.save(conversation)

        val messageUtilisateur = Message(
            contenu = prompt,
            dateCreation = LocalDateTime.now(),
            typeMessage = TypeMessage.UTILISATEUR,
            conversation = conversationEnregistree
        )
        messageDAO.save(messageUtilisateur)

        var reponseGeneree = false
        try {
            val iaService = iaServiceFactory.getService(modele)
            val reponse = iaService.envoyerPrompt(prompt, modele)
            val messageAssistant = Message(
                contenu = reponse,
                dateCreation = LocalDateTime.now(),
                typeMessage = TypeMessage.ASSISTANT,
                conversation = conversationEnregistree
            )
            messageDAO.save(messageAssistant)
            reponseGeneree = true
        } catch (e: Exception) {
            // La conversation est conservée, mais aucune lecture ne sera demandée.
            e.printStackTrace()
        }

        conversationEnregistree.dateDerniereModification = LocalDateTime.now()
        conversationDAO.save(conversationEnregistree)

        if (reponseGeneree) {
            redirectAttributes.addFlashAttribute("lectureAuto", true)
        }

        return "redirect:/conversations/${conversationEnregistree.id}"
    }

    // Affichage d'une conversation
    @GetMapping("/conversations/{id}")
    fun afficherConversation(
        @PathVariable id: Long,
        model: Model,
        authentication: Authentication
    ): String {
        //Recupere l'utilisateur
        val utilisateur = this.getUtilisateurConnecte(authentication)
        model.addAttribute("utilisateur", utilisateur)
        val conversation = conversationDAO
            .findByIdAndUtilisateur(id,utilisateur)
            ?: throw
            ResponseStatusException(HttpStatus.NOT_FOUND,"Conversation introuvable")

        model.addAttribute(
            "conversations",
            conversationDAO.findAllByUtilisateurOrderByDateDerniereModificationDesc(utilisateur))

        model.addAttribute("conversation", conversation)
        model.addAttribute(
            "messages",
            conversation.messages.sortedBy { it.dateCreation }
        )
        return "utilisateur/chat"
    }

    // Envoi d'un nouveau message dans une conversation existante
    @PostMapping("/conversations/{id}/messages")
    fun envoyerMessage(
        @PathVariable id: Long,
        @RequestParam prompt: String,
        authentication: Authentication
    ): String {
        val utilisateur = this.getUtilisateurConnecte(authentication)
        val conversation = conversationDAO
            .findByIdAndUtilisateur(id,utilisateur)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND,"Conversation introuvable")

        val messageUtilisateur = Message(
            contenu = prompt,
            dateCreation = LocalDateTime.now(),
            typeMessage = TypeMessage.UTILISATEUR,
            conversation = conversation
        )
        messageDAO.save(messageUtilisateur)

        try {
            val modele = conversation.modeleIA!!
            val iaService = iaServiceFactory.getService(modele)
            val reponse = iaService.envoyerPrompt(prompt, modele)
            val messageAssistant = Message(
                contenu = reponse,
                dateCreation = LocalDateTime.now(),
                typeMessage = TypeMessage.ASSISTANT,
                conversation = conversation
            )
            messageDAO.save(messageAssistant)
        }catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
//        } catch (e: Exception) {
//            println("Erreur lors de l'appel à ${conversation.modeleIA!!.nom} : ${e.message}")
//        }

        conversation.dateDerniereModification = LocalDateTime.now()
        conversationDAO.save(conversation)

        return "redirect:/conversations/$id"
    }

    // Pages générales
    @GetMapping("/a-propos")
    fun about(): String = "about"

    // Pages des publications
    @GetMapping("/posts", "/posts/")
    fun posts(): String = "posts/index"

    @GetMapping("/posts/latest")
    fun latestPosts(): String = "posts/latest"

    @GetMapping("/posts/related")
    fun relatedPosts(): String = "posts/related"

    @GetMapping("/posts/category")
    fun postCategories(): String = "posts/category"

    // Pages des services
    @GetMapping("/services", "/services/")
    fun services(): String = "services/index"

    @GetMapping("/services/free")
    fun freeService(): String = "services/free"

    @GetMapping("/services/premium")
    fun premiumService(): String = "services/premium"

    @GetMapping("/services/cases")
    fun serviceCases(): String = "services/cases"

//    // Pages des modèles IA
//    @GetMapping("/modeles", "/modeles/")
//    fun models(model: Model): String {
//        model.addAttribute("modeles", modeleIADAO.findAll())
//        return "model/index"
//    }

//    @GetMapping("/modeles/nouveau")
//    fun newModel(): String = "model/create"

//    @GetMapping("/modeles/{id}/modifier")
//    fun editModel(
//        @PathVariable id: Long,
//        model: Model
//    ): String {
//        model.addAttribute(
//            "modele",
//            modeleIADAO.findById(id)
//                .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }
//        )
//        return "model/edit"
//    }

//    @GetMapping("/modeles/{id}")
//    fun modelDetail(
//        @PathVariable id: Long,
//        model: Model
//    ): String {
//        model.addAttribute(
//            "modele",
//            modeleIADAO.findById(id)
//                .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }
//        )
//        return "model/detail"
//    }

    fun getUtilisateurConnecte( authentication: Authentication):
            Utilisateur {
        val email = authentication.name
        val utilisateur = utilisateurDAO
            .findByEmailIgnoreCase(email)
            ?: throw UsernameNotFoundException("Utilisateur introuvable")
                return utilisateur
    }

    @GetMapping("/login")
    fun login(): String {
        return "auth/login"
    }
}