package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole(val displayName: String, val description: String) {
    ADMIN("Administrator", "Full system access, user management, audit logs & system config"),
    MODERATOR("Moderator", "Review submissions, approve/reject questions & content quality"),
    ADVISOR("Advisor", "Academic guidance, student progress insights & recommendations"),
    MEMBER("Member", "Practice question bank, take exams, bookmark & track progress")
}

enum class UserStatus {
    ACTIVE,
    SUSPENDED
}

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val username: String,
    val email: String,
    val passwordHash: String,
    val salt: String,
    val role: UserRole,
    val displayName: String,
    val avatarUrl: String = "",
    val status: UserStatus = UserStatus.ACTIVE,
    val createdAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long = System.currentTimeMillis()
)
