document.addEventListener("DOMContentLoaded", function () {
    // La lecture n'est demandée que si le contrôleur vient de générer une réponse.
    const pageChat = document.querySelector(".chat-page");
    if (pageChat?.dataset.lectureAuto !== "true") {
        return;
    }

    // La Web Speech API n'est pas disponible dans tous les navigateurs.
    if (!("speechSynthesis" in window)) {
        console.warn("La synthèse vocale n'est pas prise en charge par ce navigateur.");
        return;
    }

    const messagesAssistant = document.querySelectorAll(".message-assistant");
    if (messagesAssistant.length === 0) {
        return;
    }

    // Seule la dernière réponse de l'assistant est lue.
    const dernierMessage = messagesAssistant[messagesAssistant.length - 1];
    const texte = dernierMessage.innerText?.trim();
    if (!texte) {
        return;
    }

    const lecture = new SpeechSynthesisUtterance(texte);
    lecture.lang = "fr-FR";
    lecture.rate = 1;

    window.speechSynthesis.cancel();
    window.speechSynthesis.speak(lecture);
});
