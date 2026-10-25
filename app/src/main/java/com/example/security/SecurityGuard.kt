package com.example.security

import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.data.model.UserStatus

enum class Permission {
    MANAGE_USERS,
    MANAGE_SETTINGS,
    VIEW_AUDIT_LOGS,
    MODERATE_QUESTIONS,
    CREATE_RECOMMENDATIONS,
    VIEW_MEMBER_PROGRESS,
    CREATE_OR_SUBMIT_QUESTION,
    EDIT_ANY_QUESTION,
    DELETE_ANY_QUESTION,
    EXPORT_DATA,
    IMPORT_DATA,
    PRACTICE_AND_QUIZ
}

object SecurityGuard {

    fun hasPermission(user: UserEntity, permission: Permission): Boolean {
        if (user.status == UserStatus.SUSPENDED) return false

        return when (permission) {
            Permission.MANAGE_USERS -> user.role == UserRole.ADMIN
            Permission.MANAGE_SETTINGS -> user.role == UserRole.ADMIN
            Permission.VIEW_AUDIT_LOGS -> user.role == UserRole.ADMIN

            Permission.MODERATE_QUESTIONS -> user.role == UserRole.ADMIN || user.role == UserRole.MODERATOR
            Permission.EDIT_ANY_QUESTION -> user.role == UserRole.ADMIN || user.role == UserRole.MODERATOR
            Permission.DELETE_ANY_QUESTION -> user.role == UserRole.ADMIN

            Permission.CREATE_RECOMMENDATIONS -> user.role == UserRole.ADMIN || user.role == UserRole.ADVISOR
            Permission.VIEW_MEMBER_PROGRESS -> user.role == UserRole.ADMIN || user.role == UserRole.ADVISOR

            Permission.CREATE_OR_SUBMIT_QUESTION -> true // All active roles can submit or create
            Permission.EXPORT_DATA -> user.role == UserRole.ADMIN || user.role == UserRole.MODERATOR || user.role == UserRole.ADVISOR || user.role == UserRole.MEMBER
            Permission.IMPORT_DATA -> user.role == UserRole.ADMIN || user.role == UserRole.MODERATOR
            Permission.PRACTICE_AND_QUIZ -> true
        }
    }

    fun assertPermission(user: UserEntity, permission: Permission, resourceAuthorId: String? = null) {
        if (user.status == UserStatus.SUSPENDED) {
            throw SecurityException("Access Denied: Account '${user.username}' is currently suspended.")
        }

        // Allow author to edit/delete their own pending question
        if (resourceAuthorId != null && resourceAuthorId == user.id) {
            return
        }

        if (!hasPermission(user, permission)) {
            throw SecurityException("Forbidden: User role '${user.role}' lacks permission for '${permission.name}'.")
        }
    }
}
