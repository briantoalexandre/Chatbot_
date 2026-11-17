package org.ldv.chatbot.model.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
class Conversation(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    var id: Long? = null,
    @Column
    var titre: String,
    @Column
    var dateCreation: LocalDateTime,
    @Column
    var dateDerniereModification: LocalDateTime = LocalDateTime.now(),
    @Column
    var estArchivee: Boolean = false,
    @ManyToOne
    @JoinColumn(name = "id_modele")
    var modeleIA : ModeleIA? = null,
    @OneToMany
    @JoinColumn(name = "id_message")
    var messages: MutableList<Message> = mutableListOf()
) {
}