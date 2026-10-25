package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.FirestoreUserProfile
import com.example.data.model.UserRole
import com.example.data.model.UserStatus
import com.example.data.remote.OperationType
import com.example.data.remote.handleFirestoreError
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirestoreUserRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in before accessing Firestore.")
    }

    /**
     * Ensures a user document exists at `users/{uid}` and returns the user's profile
     * (including their real `role` field from Firestore).
     * If `users/{uid}` already exists in Firestore, its existing `role` is preserved and returned.
     * If `users/{uid}` does not exist yet, a new document is initialized with `role = "MEMBER"`.
     */
    suspend fun ensureAndFetchUserProfile(
        uid: String,
        email: String,
        displayName: String
    ): Result<FirestoreUserProfile> {
        val authedUid = requireUserId()
        require(authedUid == uid) { "Authenticated UID mismatch." }

        val docRef = db.collection("users").document(uid)
        return try {
            val existingSnapshot = docRef.get().await()
            if (existingSnapshot.exists()) {
                val existingProfile = existingSnapshot.toObject(
                    FirestoreUserProfile::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
                if (existingProfile != null) {
                    return Result.success(existingProfile)
                }
            }

            val safeEmail = email.trim().ifBlank { "$uid@edubank.org" }.take(254)
            val safeName = displayName.trim().ifBlank {
                safeEmail.substringBefore("@").ifBlank { "Student" }
            }.take(100)

            val payload = mapOf(
                "uid" to uid,
                "email" to safeEmail,
                "displayName" to safeName,
                "role" to UserRole.MEMBER.name,
                "status" to UserStatus.ACTIVE.name,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()

            val createdSnapshot = docRef.get().await()
            val createdProfile = createdSnapshot.toObject(
                FirestoreUserProfile::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            ) ?: FirestoreUserProfile(
                id = uid,
                uid = uid,
                email = safeEmail,
                displayName = safeName,
                role = UserRole.MEMBER.name,
                status = UserStatus.ACTIVE.name
            )
            Result.success(createdProfile)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    /**
     * Fetches a user's profile document from `users/{uid}`.
     */
    suspend fun getUserProfileById(uid: String): Result<FirestoreUserProfile?> {
        val docRef = db.collection("users").document(uid)
        return try {
            val snapshot = docRef.get().await()
            if (!snapshot.exists()) {
                Result.success(null)
            } else {
                val profile = snapshot.toObject(
                    FirestoreUserProfile::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
                Result.success(profile)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            Result.failure(e)
        }
    }

    /**
     * Real-time observation of `users/{uid}` so any role or status changes in Firestore
     * immediately propagate to the app's permission engine and UI.
     */
    fun observeUserProfile(uid: String): Flow<FirestoreUserProfile?> {
        val docRef = db.collection("users").document(uid)
        return docRef.snapshots()
            .map { snapshot ->
                if (snapshot.exists()) {
                    snapshot.toObject(
                        FirestoreUserProfile::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                } else {
                    null
                }
            }
            .catch { error ->
                if (error is Exception) {
                    handleFirestoreError(error, OperationType.GET, docRef.path)
                }
                throw error
            }
    }

    /**
     * Queries the current user's own document in `users` using the rule-aligned `uid` filter.
     */
    suspend fun queryOwnUserProfiles(uid: String): Result<List<FirestoreUserProfile>> {
        val path = "users"
        return try {
            val querySnapshot = db.collection(path)
                .whereEqualTo("uid", uid)
                .get()
                .await()
            val profiles = querySnapshot.toObjects(
                FirestoreUserProfile::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            )
            Result.success(profiles)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, path)
            Result.failure(e)
        }
    }

    /**
     * Updates the authenticated user's own `displayName`.
     */
    suspend fun updateOwnDisplayName(newDisplayName: String): Result<Unit> {
        val uid = requireUserId()
        val trimmed = newDisplayName.trim()
        require(trimmed.isNotEmpty() && trimmed.length <= 100) { "Display name must be 1..100 chars" }
        val docRef = db.collection("users").document(uid)
        return try {
            docRef.update(
                mapOf(
                    "displayName" to trimmed,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    /**
     * Updates a user's `role` in `users/{targetUid}` (permitted only when caller is an Admin).
     */
    suspend fun updateUserRole(targetUid: String, newRole: UserRole): Result<Unit> {
        requireUserId()
        val docRef = db.collection("users").document(targetUid)
        return try {
            docRef.update(
                mapOf(
                    "role" to newRole.name,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    /**
     * Updates a user's `status` in `users/{targetUid}` (permitted only when caller is an Admin).
     */
    suspend fun updateUserStatus(targetUid: String, newStatus: UserStatus): Result<Unit> {
        requireUserId()
        val docRef = db.collection("users").document(targetUid)
        return try {
            docRef.update(
                mapOf(
                    "status" to newStatus.name,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }
}
