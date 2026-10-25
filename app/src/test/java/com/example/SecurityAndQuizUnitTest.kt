package com.example

import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.data.model.UserStatus
import com.example.security.PasswordHasher
import com.example.security.Permission
import com.example.security.SecurityGuard
import org.junit.Assert.*
import org.junit.Test

class SecurityAndQuizUnitTest {

    @Test
    fun testPasswordHasher_generatesSaltAndValidatesHash() {
        val salt = PasswordHasher.generateSalt()
        assertTrue(salt.length >= 16)

        val password = "StrongPassword123!"
        val hash = PasswordHasher.hashPassword(password, salt)
        assertNotEquals(password, hash)

        // Verify valid password returns true
        assertTrue(PasswordHasher.verifyPassword(password, salt, hash))
        // Verify invalid password returns false
        assertFalse(PasswordHasher.verifyPassword("WrongPassword", salt, hash))
    }

    @Test
    fun testSecurityGuard_rolePermissions() {
        val admin = UserEntity("1", "admin", "admin@test.com", "", "", UserRole.ADMIN, "Admin")
        val moderator = UserEntity("2", "mod", "mod@test.com", "", "", UserRole.MODERATOR, "Mod")
        val advisor = UserEntity("3", "adv", "adv@test.com", "", "", UserRole.ADVISOR, "Advisor")
        val member = UserEntity("4", "student", "student@test.com", "", "", UserRole.MEMBER, "Student")

        // Admin privileges
        assertTrue(SecurityGuard.hasPermission(admin, Permission.MANAGE_USERS))
        assertTrue(SecurityGuard.hasPermission(admin, Permission.VIEW_AUDIT_LOGS))
        assertTrue(SecurityGuard.hasPermission(admin, Permission.DELETE_ANY_QUESTION))

        // Moderator privileges
        assertFalse(SecurityGuard.hasPermission(moderator, Permission.MANAGE_USERS))
        assertTrue(SecurityGuard.hasPermission(moderator, Permission.MODERATE_QUESTIONS))
        assertTrue(SecurityGuard.hasPermission(moderator, Permission.EDIT_ANY_QUESTION))

        // Advisor privileges
        assertTrue(SecurityGuard.hasPermission(advisor, Permission.CREATE_RECOMMENDATIONS))
        assertTrue(SecurityGuard.hasPermission(advisor, Permission.VIEW_MEMBER_PROGRESS))
        assertFalse(SecurityGuard.hasPermission(advisor, Permission.MANAGE_USERS))

        // Member privileges
        assertTrue(SecurityGuard.hasPermission(member, Permission.PRACTICE_AND_QUIZ))
        assertTrue(SecurityGuard.hasPermission(member, Permission.CREATE_OR_SUBMIT_QUESTION))
        assertFalse(SecurityGuard.hasPermission(member, Permission.MODERATE_QUESTIONS))
        assertFalse(SecurityGuard.hasPermission(member, Permission.DELETE_ANY_QUESTION))
    }

    @Test
    fun testSecurityGuard_suspendedUserBlocked() {
        val suspended = UserEntity("5", "bad", "bad@test.com", "", "", UserRole.ADMIN, "Bad", status = UserStatus.SUSPENDED)
        assertFalse(SecurityGuard.hasPermission(suspended, Permission.MANAGE_USERS))

        assertThrows(SecurityException::class.java) {
            SecurityGuard.assertPermission(suspended, Permission.PRACTICE_AND_QUIZ)
        }
    }
}
