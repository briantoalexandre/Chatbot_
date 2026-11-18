package org.ldv.chatbot.model.entity

import jakarta.persistence.*
import java.time.LocalDateTime


@Entity
class Message (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    var id: Long? = null,
    @Column(columnDefinition = "TEXT", nullable = false)
    var contenu: String,
    @Column
    var dateCreation: LocalDateTime,
    @Column
    var typeMessage: TypeMessage,
    @ManyToOne
    @JoinColumn(name="id_conversation", nullable = true)
    var conversation: Conversation? = null,

) {

}