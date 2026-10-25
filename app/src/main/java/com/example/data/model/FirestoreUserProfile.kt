package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class FirestoreUserProfile(
    @DocumentId val id: String = "",
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val role: String = "MEMBER",
    val status: String = "ACTIVE",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun parsedRole(): UserRole {
        return when (role.trim().uppercase()) {
            "ADMIN", "ADMINISTRATOR" -> UserRole.ADMIN
            "MODERATOR", "MOD" -> UserRole.MODERATOR
            "ADVISOR" -> UserRole.ADVISOR
            else -> UserRole.MEMBER
        }
    }

    fun parsedStatus(): UserStatus {
        return when (status.trim().uppercase()) {
            "SUSPENDED" -> UserStatus.SUSPENDED
            else -> UserStatus.ACTIVE
        }
    }

    fun toUserEntity(): UserEntity {
        val resolvedId = id.ifBlank { uid }
        val resolvedEmail = email.ifBlank { "$resolvedId@edubank.org" }
        val resolvedUsername = resolvedEmail.substringBefore("@").ifBlank { resolvedId.take(8) }
        val resolvedDisplayName = displayName.ifBlank { resolvedUsername }
        val createdMillis = createdAt?.toDate()?.time ?: System.currentTimeMillis()
        val updatedMillis = updatedAt?.toDate()?.time ?: createdMillis

        return UserEntity(
            id = resolvedId,
            username = resolvedUsername,
            email = resolvedEmail,
            passwordHash = "",
            salt = "",
            role = parsedRole(),
            displayName = resolvedDisplayName,
            status = parsedStatus(),
            createdAt = createdMillis,
            lastLoginAt = updatedMillis
        )
    }
}
