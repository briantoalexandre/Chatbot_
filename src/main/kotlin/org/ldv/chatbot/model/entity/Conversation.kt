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
    @JoinColumn(name = "id_modele", nullable = true)
    var modeleIA : ModeleIA? = null,
    @OneToMany(mappedBy = "conversation", cascade = [CascadeType.ALL])
    var messages: MutableList<Message> = mutableListOf(),
    @ManyToOne(optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    var utilisateur: Utilisateur

//    @OneToMany
//    @JoinColumn(name = "id_message")
//    var messages: MutableList<Message> = mutableListOf()
) {
}