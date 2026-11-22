package org.ldv.chatbot.service

import org.ldv.chatbot.model.entity.ModeleIA
import org.ldv.chatbot.parser.MarkdownToHtmlConverter
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient

data class GeminiApiRequest(
    val contents: List<GeminiRequestContent>
)

data class GeminiRequestContent(
    val role: String,
    val parts: List<GeminiRequestPart>
)

data class GeminiRequestPart(
    val text: String
)

data class GeminiApiResponse(
    val candidates: List<GeminiCandidate>? = null
)

data class GeminiCandidate(
    val content: GeminiResponseContent? = null
)

data class GeminiResponseContent(
    val parts: List<GeminiResponsePart>? = null
)

data class GeminiResponsePart(
    val text: String? = null
)

@Service
class GeminiService(
    private val restClientBuilder: RestClient.Builder,

    @Value("\${gemini.api.key}")
    private val apiKey: String

) : IAService {

    override fun getFournisseur(): String = "Gemini"

    override fun envoyerPrompt(
        prompt: String,
        modele: ModeleIA
    ): String {

        // 1. Préparer la requête Gemini
        val request = GeminiApiRequest(
            contents = listOf(
                GeminiRequestContent(
                    role = "user",
                    parts = listOf(
                        GeminiRequestPart(text = prompt)
                    )
                )
            )
        )

        // 2. Construire le client HTTP
        val client = restClientBuilder.build()

        // 3. Appeler l'API Gemini
        val response = client.post()
            .uri(modele.urlAPI)
            .header("x-goog-api-key", apiKey)
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()
            .body(GeminiApiResponse::class.java)

        // 4. Récupérer la réponse
        val reponse = response?.candidates
            ?.firstNotNullOfOrNull { candidate ->
                candidate.content?.parts
                    ?.firstNotNullOfOrNull { it.text }
            }

        if (reponse.isNullOrBlank()) {
            throw IllegalStateException(
                "Gemini n'a retourné aucun texte."
            )
        }

        // 5. Convertir Markdown en HTML
        return MarkdownToHtmlConverter.convert(reponse)
    }
}
