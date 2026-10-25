package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.remote.OperationType
import com.example.data.remote.handleFirestoreError
import com.example.data.repository.*
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(
    application: Application,
    private val currentUserId: String = Firebase.auth.currentUser!!.uid,
    private val userEmail: String = Firebase.auth.currentUser?.email ?: "",
    private val userDisplayName: String = Firebase.auth.currentUser?.displayName ?: ""
) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val databaseId = application.getString(R.string.firestore_database_id)
    private val firestore = FirebaseFirestore.getInstance(databaseId)
    private val firestoreUserRepo = FirestoreUserRepository(firestore)

    private val authRepo = AuthRepository(database)
    private val questionRepo = QuestionRepository(database)
    private val quizRepo = QuizRepository(database)
    private val moderationRepo = ModerationRepository(database)
    private val advisorRepo = AdvisorRepository(database)
    private val auditRepo = AuditRepository(database)

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _isLoadingRole = MutableStateFlow(true)
    val isLoadingRole: StateFlow<Boolean> = _isLoadingRole.asStateFlow()

    // Real-time stream of the user's document in Firestore (`users/{uid}`)
    // using the Two-Tier Error Handling Pattern (.map + .catch + .stateIn)
    val firestoreUserProfile: StateFlow<FirestoreUserProfile?> = firestoreUserRepo
        .observeUserProfile(currentUserId)
        .onEach { profile ->
            if (profile != null) {
                authRepo.syncAuthenticatedUser(profile.toUserEntity())
                _isLoadingRole.value = false
            }
        }
        .catch { e ->
            handleFirestoreError(e as? Exception ?: Exception(e), OperationType.GET, "users/$currentUserId")
            _authError.value = "Failed to load role from Firestore: ${e.localizedMessage ?: "Permission denied"}"
            _isLoadingRole.value = false
            emit(null)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val currentUser = authRepo.currentUser.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val allUsers = authRepo.observeAllUsers().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val subjects = database.subjectDao().observeAllSubjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val questions = questionRepo.observeAllQuestions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingTasks = moderationRepo.observePendingTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentTasks = moderationRepo.observeRecentTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingCount = moderationRepo.countPendingTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val approvedCount = moderationRepo.countApprovedTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val rejectedCount = moderationRepo.countRejectedTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val auditLogs = auditRepo.observeRecentLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalUsers = authRepo.countUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 4)

    val activeUsers = authRepo.countActiveUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 4)

    val totalQuestions = questionRepo.countTotalQuestions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 8)

    val totalAttempts = quizRepo.countTotalAttempts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

    // Bookmarks for current user
    val bookmarkedIds = currentUser.flatMapLatest { u ->
        if (u != null) quizRepo.observeBookmarkedQuestionIds(u.id) else flowOf(emptyList())
    }.map { it.toSet() }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    // Quiz attempts for current user
    val userAttempts = currentUser.flatMapLatest { u ->
        if (u != null) quizRepo.observeAttemptsForUser(u.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Advisor recommendations for current user
    val userRecommendations = currentUser.flatMapLatest { u ->
        if (u != null) advisorRepo.observeRecommendationsForUser(u.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRecommendations = advisorRepo.observeAllRecommendations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        fetchAndEnsureFirestoreRole()
    }

    /**
     * Fetches the authenticated user's role from `users/{uid}` in Cloud Firestore
     * (or initializes a default MEMBER document if none exists yet) and applies
     * the corresponding role permissions.
     */
    fun fetchAndEnsureFirestoreRole() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoadingRole.value = true
            _authError.value = null
            val result = firestoreUserRepo.ensureAndFetchUserProfile(
                uid = currentUserId,
                email = userEmail,
                displayName = userDisplayName
            )
            result.onSuccess { profile ->
                authRepo.syncAuthenticatedUser(profile.toUserEntity())
            }.onFailure { e ->
                _authError.value = "Could not fetch user role from Firestore: ${e.localizedMessage ?: "Unknown error"}"
            }
            _isLoadingRole.value = false
        }
    }

    fun clearSession() {
        viewModelScope.launch(Dispatchers.IO) {
            authRepo.clearSession()
        }
    }

    fun toggleBookmark(questionId: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            quizRepo.toggleBookmark(user.id, questionId)
        }
    }

    fun saveQuestion(question: QuestionEntity, options: List<QuestionOptionEntity>) {
        val user = currentUser.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            questionRepo.saveQuestion(user, question, options)
        }
    }

    fun duplicateQuestion(questionId: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            questionRepo.duplicateQuestion(user, questionId)
        }
    }

    fun archiveQuestion(questionId: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            questionRepo.archiveQuestion(user, questionId)
        }
    }

    fun deleteQuestion(questionId: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            questionRepo.deleteQuestion(user, questionId)
        }
    }

    suspend fun exportJson(): String = questionRepo.exportQuestionsToJson()

    fun importJson(json: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            questionRepo.importQuestionsFromJson(user, json)
        }
    }

    fun updateUserRole(userId: String, newRole: UserRole) {
        val user = currentUser.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                authRepo.updateUserRole(user, userId, newRole)
                firestoreUserRepo.updateUserRole(userId, newRole)
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.UPDATE, "users/$userId")
            }
        }
    }

    fun updateUserStatus(userId: String, newStatus: UserStatus) {
        val user = currentUser.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            authRepo.updateUserStatus(user, userId, newStatus)
        }
    }

    fun resolveModerationTask(questionId: String, action: ModerationAction, feedbackNotes: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            moderationRepo.resolveTask(user, questionId, action, feedbackNotes)
        }
    }

    fun createAdvisorRecommendation(
        targetUserId: String,
        targetUserName: String,
        subjectName: String,
        topicName: String,
        title: String,
        note: String,
        priority: String
    ) {
        val user = currentUser.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            advisorRepo.createRecommendation(
                user, targetUserId, targetUserName, subjectName, topicName, title, note, priority
            )
        }
    }

    fun markRecommendationComplete(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            advisorRepo.markRecommendationComplete(id)
        }
    }

    suspend fun generateQuiz(
        subjectId: String,
        difficulty: DifficultyLevel?,
        limit: Int
    ): List<QuestionWithOptions> {
        return quizRepo.generateQuiz(subjectId, difficulty, limit)
    }

    fun submitQuizAttempt(attempt: QuizAttemptEntity, answers: List<QuizAnswerEntity>) {
        val user = currentUser.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val withUser = attempt.copy(userId = user.id)
            val answersWithAttempt = answers.map { it.copy(quizAttemptId = withUser.id) }
            quizRepo.submitQuizAttempt(user.id, withUser, answersWithAttempt)
        }
    }

    class Factory(
        private val application: Application,
        private val currentUserId: String,
        private val userEmail: String,
        private val userDisplayName: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(
                application = application,
                currentUserId = currentUserId,
                userEmail = userEmail,
                userDisplayName = userDisplayName
            ) as T
        }
    }
}
