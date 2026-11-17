package org.ldv.chatbot.model.dao

import org.ldv.chatbot.model.entity.TypeMessage
import org.springframework.data.jpa.repository.JpaRepository

interface TypeMessageDAO: JpaRepository<TypeMessage, Long> {
}