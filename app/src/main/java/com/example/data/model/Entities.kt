package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dating_profiles")
data class DatingProfile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val age: Int,
    val occupation: String,
    val bio: String,
    val city: String,
    val distanceKm: Int,
    val verifiedHuman: Boolean = true,
    val drawableResName: String,
    val idealDateTitle: String,
    val idealDateDescription: String,
    val idealDateLocation: String,
    val idealDateCategory: String, // "Café & Charla", "Cena & Vinos", "Aventura al Aire Libre", "Arte & Cultura", "Música"
    val interests: String, // Comma-separated
    val nonNegotiables: String, // Comma-separated
    val relationshipGoal: String,
    val compatibilityScore: Int, // 0 - 100
    val icebreakerPrompt: String,
    val icebreakerAnswer: String,
    val isLiked: Boolean = false,
    val isMatched: Boolean = false,
    val isPassed: Boolean = false
)

@Entity(tableName = "real_dates")
data class RealDate(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: Long,
    val profileName: String,
    val profileDrawable: String,
    val title: String,
    val location: String,
    val dateTimeString: String,
    val category: String,
    val status: String, // "CONFIRMADA", "PROPUESTA", "COMPLETADA"
    val notes: String = "",
    val isSafetyProtocolActive: Boolean = true,
    val createdTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: Long,
    val senderName: String,
    val messageText: String,
    val isFromUser: Boolean,
    val isDateProposal: Boolean = false,
    val dateProposalId: Long? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey
    val id: Int = 1,
    val name: String = "Alma",
    val age: Int = 26,
    val occupation: String = "Diseñadora & Amante del Café",
    val city: String = "Madrid / CDMX",
    val bio: String = "Creo en las miradas directas, el humor inteligente y las citas en la vida real sin pantallas de por medio.",
    val idealDate: String = "Café de especialidad en una librería oculta y caminar sin rumbo.",
    val nonNegotiables: String = "Comunicación abierta, Puntualidad con cariño, Respeto mutuo",
    val isVerified: Boolean = true,
    val antiGhostingPledgeSigned: Boolean = true,
    val preferredDateCategory: String = "Café & Charla"
)
