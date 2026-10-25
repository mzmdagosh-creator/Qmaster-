package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.UserRole
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.UUID

class UserRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun ensureAndFetchUserProfile_authenticatedOwner_createsAndReturnsMemberRole() = runBlocking {
        val uniqueEmail = "alice_${UUID.randomUUID().toString().take(8)}@test.com"
        val uid = signInTestUser(uniqueEmail)
        val repository = FirestoreUserRepository(firestore, auth)

        val result = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.ensureAndFetchUserProfile(uid, uniqueEmail, "Alice Student")
        }
        assertTrue("Expected success, got ${result.exceptionOrNull()}", result.isSuccess)
        val profile = result.getOrThrow()
        assertEquals(uid, profile.uid)
        assertEquals(UserRole.MEMBER, profile.parsedRole())
    }

    @Test
    fun observeUserProfile_authenticatedOwner_emitsRealtimeProfile() = runBlocking {
        val uniqueEmail = "observer_${UUID.randomUUID().toString().take(8)}@test.com"
        val uid = signInTestUser(uniqueEmail)
        val repository = FirestoreUserRepository(firestore, auth)

        withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.ensureAndFetchUserProfile(uid, uniqueEmail, "Observer User").getOrThrow()
        }

        val emitted = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeUserProfile(uid).first { it != null && it.uid == uid }
        }
        assertNotNull(emitted)
        assertEquals("Observer User", emitted?.displayName)
    }

    @Test
    fun queryOwnUserProfiles_authenticatedOwner_returnsMatchingProfile() = runBlocking {
        val uniqueEmail = "query_${UUID.randomUUID().toString().take(8)}@test.com"
        val uid = signInTestUser(uniqueEmail)
        val repository = FirestoreUserRepository(firestore, auth)

        withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.ensureAndFetchUserProfile(uid, uniqueEmail, "Query User").getOrThrow()
        }

        val listResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.queryOwnUserProfiles(uid)
        }
        assertTrue(listResult.isSuccess)
        assertTrue(listResult.getOrThrow().any { it.uid == uid })
    }

    @Test
    fun getUserProfileById_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        val aliceEmail = "alice_cross_${UUID.randomUUID().toString().take(8)}@test.com"
        val aliceUid = signInTestUser(aliceEmail)
        val repo = FirestoreUserRepository(firestore, auth)

        withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.ensureAndFetchUserProfile(aliceUid, aliceEmail, "Alice").getOrThrow()
        }

        val bobEmail = "bob_cross_${UUID.randomUUID().toString().take(8)}@test.com"
        signInTestUser(bobEmail)

        val crossReadResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.getUserProfileById(aliceUid)
        }
        assertTrue(crossReadResult.isFailure)
        val fsException = crossReadResult.exceptionOrNull()?.unwrapFirestoreException()
        assertNotNull("Expected FirebaseFirestoreException", fsException)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsException?.code)
    }

    @Test
    fun updateUserRole_nonAdminSelfEscalation_failsWithPermissionDenied() = runBlocking {
        val email = "escalate_${UUID.randomUUID().toString().take(8)}@test.com"
        val uid = signInTestUser(email)
        val repo = FirestoreUserRepository(firestore, auth)

        withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.ensureAndFetchUserProfile(uid, email, "Regular Member").getOrThrow()
        }

        val escalateResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.updateUserRole(uid, UserRole.ADMIN)
        }
        assertTrue(escalateResult.isFailure)
        val fsException = escalateResult.exceptionOrNull()?.unwrapFirestoreException()
        assertNotNull("Expected FirebaseFirestoreException", fsException)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsException?.code)
    }

    @Test
    fun observeUserProfile_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repo = FirestoreUserRepository(firestore, auth)

        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repo.observeUserProfile("some_random_uid").first()
            }
            fail("Expected exception for unauthenticated observer")
        } catch (e: Exception) {
            val fsException = checkNotNull(e.unwrapFirestoreException()) {
                "Expected nested FirebaseFirestoreException, got $e"
            }
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsException.code)
        }
    }

    private companion object {
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
