package org.ldv.chatbot.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class ChatBotController {
    @GetMapping("/exemple")
    fun exemple(): String {
        return "exemple"
    }
}