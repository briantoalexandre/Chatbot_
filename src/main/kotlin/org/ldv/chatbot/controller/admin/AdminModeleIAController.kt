package org.ldv.chatbot.controller.admin

import org.ldv.chatbot.model.dao.ModeleIADAO
import org.ldv.chatbot.model.dao.FournisseurIADAO
import org.ldv.chatbot.model.dao.ConversationDAO
import org.ldv.chatbot.model.entity.ModeleIA
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
@RequestMapping("/modeles")
class AdminModeleIAController(
    private val modeleIADAO: ModeleIADAO,
    private val fournisseurIADAO: FournisseurIADAO,
    private val conversationDAO: ConversationDAO
) {
//    @GetMapping //OBSELETE
//    fun index(model: Model): String {
//        model.addAttribute("modelesIA", modeleIADAO.findAll())
//        return "admin/modeleIA/index"
//    }
    // Pages des modèles IA
    @GetMapping
    fun models(model: Model): String {
        model.addAttribute("modeles", modeleIADAO.findAll())
        return "model/index"
    }
//    @GetMapping("/create") OBSELETE
//    fun create(model: Model): String {
//
//        return "admin/modeleIA/create"
//    }

    @GetMapping("nouveau")
    fun newModel(model: Model): String {
        model.addAttribute("fournisseurs", fournisseurIADAO.findAll())
        return "model/create"
    }

    @PostMapping
    fun store(
        @RequestParam nom: String,
        @RequestParam urlAPI: String,
        @RequestParam description: String,
        @RequestParam cheminLogo: String,
        @RequestParam fourniseurIAId: Long,
        redirectAttributes: RedirectAttributes
    ): String {
        if (nom.isBlank() || urlAPI.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Le nom et l'URL sont obligatoires")
        }
        val fourniseurIA = fournisseurIADAO.findById(fourniseurIAId)
            .orElseThrow {
                ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Fournisseur IA introuvable")
            }
        val modeleIA = ModeleIA(
            nom = nom,
            urlAPI = urlAPI,
            description = description,
            cheminLogo = cheminLogo,
            fourniseurIA = fourniseurIA
        )
        modeleIADAO.save(modeleIA)
        redirectAttributes.addFlashAttribute(
            "succes", "Le modèle IA a été créé avec succès.")
        return "redirect:/"
    }

//    @GetMapping("/{id}") //OBSELETE
//    fun show(@PathVariable id: Long, model: Model): String {
//        val modeleIA = modeleIADAO.findById(id).orElseThrow {
//            ResponseStatusException(HttpStatus.NOT_FOUND,
//                "Modèle IA introuvable")
//        }
//        model.addAttribute("modeleIA", modeleIA)
//        return "admin/modeleIA/show"
//    }

    @GetMapping("{id}")
    fun modelDetail(
        @PathVariable id: Long,
        model: Model
    ): String {
        model.addAttribute(
            "modele",
            modeleIADAO.findById(id)
                .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Modèle IA introuvable") }
        )
        return "model/detail"
    }

//    @GetMapping("/{id}/edit") //OBSELETE
//    fun edit(@PathVariable id: Long, model: Model): String {
//        val modeleIA = modeleIADAO.findById(id).orElseThrow {
//            ResponseStatusException(HttpStatus.NOT_FOUND,
//                "Modèle IA introuvable")
//        }
//        model.addAttribute("modeleIA", modeleIA)
//        model.addAttribute("fournisseursIA", fournisseurIADAO.findAll())
//        return "admin/modeleIA/edit"
//    }

    @GetMapping("{id}/modifier")
    fun editModel(
        @PathVariable id: Long,
        model: Model
    ): String {
        model.addAttribute(
            "modele",
            modeleIADAO.findById(id)
                .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }
        )
        model.addAttribute("fournisseurs", fournisseurIADAO.findAll())
        return "model/edit"
    }

    @PutMapping("{id}")
    fun update(
        @PathVariable id: Long,
        @RequestParam nom: String,
        @RequestParam urlAPI: String,
        @RequestParam description: String,
        @RequestParam cheminLogo: String,
        @RequestParam fourniseurIAId: Long,
        redirectAttributes: RedirectAttributes
    ): String {
        if (nom.isBlank() || urlAPI.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Le nom et l'URL sont obligatoires")
        }
        val modeleIA = modeleIADAO.findById(id).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND,
                "Modèle IA introuvable")
        }
        val fourniseurIA = fournisseurIADAO.findById(fourniseurIAId)
            .orElseThrow {
                ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Fournisseur IA introuvable")
            }
        modeleIA.nom = nom
        modeleIA.urlAPI = urlAPI
        modeleIA.description = description
        modeleIA.cheminLogo = cheminLogo
        modeleIA.fourniseurIA = fourniseurIA
        modeleIADAO.save(modeleIA)
        redirectAttributes.addFlashAttribute(
            "succes", "Le modèle IA a été modifié avec succès.")
        return "redirect:/"
    }

}