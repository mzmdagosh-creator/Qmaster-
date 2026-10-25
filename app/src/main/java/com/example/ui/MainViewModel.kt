package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    private val authRepo = AuthRepository(database)
    private val questionRepo = QuestionRepository(database)
    private val quizRepo = QuizRepository(database)
    private val moderationRepo = ModerationRepository(database)
    private val advisorRepo = AdvisorRepository(database)
    private val auditRepo = AuditRepository(database)

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

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    init {
        // Auto-login default persona (Admin) on first run
        viewModelScope.launch(Dispatchers.IO) {
            authRepo.autoLoginDefaultUser()
        }
    }

    fun login(identifier: String, pass: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _authError.value = null
            val result = authRepo.login(identifier, pass)
            result.onFailure {
                _authError.value = it.message ?: "Authentication failed."
            }
        }
    }

    fun register(user: String, email: String, pass: String, name: String, role: UserRole) {
        viewModelScope.launch(Dispatchers.IO) {
            _authError.value = null
            val result = authRepo.register(user, email, pass, name, role)
            result.onFailure {
                _authError.value = it.message ?: "Registration failed."
            }
        }
    }

    fun switchUserPersona(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            authRepo.switchUserPersona(userId)
        }
    }

    fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            authRepo.logout()
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
            authRepo.updateUserRole(user, userId, newRole)
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
}
