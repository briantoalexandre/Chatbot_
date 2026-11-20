package org.ldv.chatbot.controller

import org.ldv.chatbot.model.dao.ConversationDAO
import org.ldv.chatbot.model.dao.MessageDAO
import org.ldv.chatbot.model.dao.ModeleIADAO
import org.ldv.chatbot.model.entity.Conversation
import org.ldv.chatbot.model.entity.Message
import org.ldv.chatbot.model.entity.TypeMessage
import org.ldv.chatbot.service.IAServiceFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime

@Controller
class ChatController(
    private val iaServiceFactory: IAServiceFactory,
    private val conversationDAO: ConversationDAO,
    private val messageDAO: MessageDAO,
    private val modeleIADAO: ModeleIADAO
) {
    // Page d'accueil : liste des conversations
    @GetMapping("/")
    fun accueil(model: Model): String {
        model.addAttribute("conversation", null)
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
        @RequestParam idModele: Long
    ): String {
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
            modeleIA = modele
        )
        val conversationEnregistree = conversationDAO.save(conversation)

        val messageUtilisateur = Message(
            contenu = prompt,
            dateCreation = LocalDateTime.now(),
            typeMessage = TypeMessage.UTILISATEUR,
            conversation = conversationEnregistree
        )
        messageDAO.save(messageUtilisateur)

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
        } catch (e: Exception) {
            println("Erreur lors de l’appel au modèle ${modele.nom} : ${e.message}")
        }

        conversationEnregistree.dateDerniereModification = LocalDateTime.now()
        conversationDAO.save(conversationEnregistree)

        return "redirect:/conversations/${conversationEnregistree.id}"
    }

    // Affichage d'une conversation
    @GetMapping("/conversations/{id}")
    fun afficherConversation(
        @PathVariable id: Long,
        model: Model
    ): String {
        val conversation = conversationDAO.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }

        model.addAttribute(
            "conversations",
            conversationDAO.findAllByOrderByDateDerniereModificationDesc()
        )
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
        @RequestParam prompt: String
    ): String {
        val conversation = conversationDAO.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }

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

    // Pages des modèles IA
    @GetMapping("/modeles", "/modeles/")
    fun models(model: Model): String {
        model.addAttribute("modeles", modeleIADAO.findAll())
        return "model/index"
    }

    @GetMapping("/modeles/nouveau")
    fun newModel(): String = "model/create"

    @GetMapping("/modeles/{id}/modifier")
    fun editModel(
        @PathVariable id: Long,
        model: Model
    ): String {
        model.addAttribute(
            "modele",
            modeleIADAO.findById(id)
                .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }
        )
        return "model/edit"
    }

    @GetMapping("/modeles/{id}")
    fun modelDetail(
        @PathVariable id: Long,
        model: Model
    ): String {
        model.addAttribute(
            "modele",
            modeleIADAO.findById(id)
                .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }
        )
        return "model/detail"
    }
}