package org.ldv.chatbot.service

import org.ldv.chatbot.dto.mistral.*
import org.ldv.chatbot.model.entity.*
import org.ldv.chatbot.parser.MarkdownToHtmlConverter
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient


@Service
class MistralService(
    private val restClientBuilder: RestClient.Builder,
    @Value("\${mistral.api.key}")
    private val apiKey: String
) : IAService {
    override fun getFournisseur(): String = "Mistral"
    override fun envoyerPrompt(prompt: String, modele: ModeleIA): String {
        // Étape 1 : Création du message de l'utilisateur
        val message = MistralMessage(
            role = "user",
            content = prompt
        )
        // Étape 2 : Préparation de la requête pour Mistral
        val request = MistralChatRequest(
            model = modele.nom,
            messages = listOf(message)
        )
        // Étape 3 : Envoi de la requête HTTP POST à l'API
        val client = restClientBuilder.build()
        val response = client.post()
            .uri(modele.urlAPI)
            .header("Authorization", "Bearer $apiKey")
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()
            .body(MistralChatResponse::class.java)

        // Étape 4 : Récupération du texte de la réponse
        val choix = response?.choices?.firstOrNull()
        val reponse = choix?.message?.content
        if (reponse != null) {
            // Etape 5 : MarkDown en HTML
            val HTMLreponse = MarkdownToHtmlConverter.convert(reponse) // Markdown to HTML
            return HTMLreponse
        } else {
            return "Aucune réponse reçue de Mistral."
        }
    }
}