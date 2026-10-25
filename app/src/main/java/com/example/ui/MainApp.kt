package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.*
import com.example.ui.components.RoleBadge
import com.example.ui.screens.dashboards.*
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.qbank.QuestionBankScreen
import com.example.ui.screens.quiz.ActiveQuizScreen
import com.example.ui.screens.quiz.QuizConfigScreen
import com.example.ui.screens.quiz.QuizResultScreen
import kotlinx.coroutines.launch

enum class AppDestination(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard),
    QUESTION_BANK("Questions", Icons.AutoMirrored.Filled.MenuBook),
    QUIZ_PRACTICE("Practice", Icons.Default.Bolt),
    PROFILE("Profile", Icons.Default.Person)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(
    viewModel: MainViewModel,
    isDarkTheme: Boolean,
    onToggleDarkTheme: (Boolean) -> Unit,
    onSignOut: () -> Unit
) {
    // Subscribe to the real-time Firestore user profile flow so role changes in `users/{uid}` update live
    val firestoreProfile by viewModel.firestoreUserProfile.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val isLoadingRole by viewModel.isLoadingRole.collectAsStateWithLifecycle()
    val authError by viewModel.authError.collectAsStateWithLifecycle()

    val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val questions by viewModel.questions.collectAsStateWithLifecycle()
    val bookmarkedIds by viewModel.bookmarkedIds.collectAsStateWithLifecycle()
    val pendingTasks by viewModel.pendingTasks.collectAsStateWithLifecycle()
    val recentTasks by viewModel.recentTasks.collectAsStateWithLifecycle()
    val pendingCount by viewModel.pendingCount.collectAsStateWithLifecycle()
    val approvedCount by viewModel.approvedCount.collectAsStateWithLifecycle()
    val rejectedCount by viewModel.rejectedCount.collectAsStateWithLifecycle()
    val auditLogs by viewModel.auditLogs.collectAsStateWithLifecycle()
    val totalUsers by viewModel.totalUsers.collectAsStateWithLifecycle()
    val activeUsers by viewModel.activeUsers.collectAsStateWithLifecycle()
    val totalQuestions by viewModel.totalQuestions.collectAsStateWithLifecycle()
    val totalAttempts by viewModel.totalAttempts.collectAsStateWithLifecycle()
    val userAttempts by viewModel.userAttempts.collectAsStateWithLifecycle()
    val userRecommendations by viewModel.userRecommendations.collectAsStateWithLifecycle()
    val allRecommendations by viewModel.allRecommendations.collectAsStateWithLifecycle()

    var currentDestination by remember { mutableStateOf(AppDestination.DASHBOARD) }

    // Quiz workflow state
    var activeQuizQuestions by remember { mutableStateOf<List<QuestionWithOptions>?>(null) }
    var activeQuizMode by remember { mutableStateOf(QuizMode.PRACTICE) }
    var activeQuizSubjectName by remember { mutableStateOf("") }
    var completedAttempt by remember { mutableStateOf<QuizAttemptEntity?>(null) }
    var completedAnswers by remember { mutableStateOf<List<QuizAnswerEntity>>(emptyList()) }

    val coroutineScope = rememberCoroutineScope()

    // Handle system back navigation if in quiz or sub-screen
    BackHandler(enabled = activeQuizQuestions != null || completedAttempt != null || currentDestination != AppDestination.DASHBOARD) {
        when {
            completedAttempt != null -> {
                completedAttempt = null
                completedAnswers = emptyList()
            }
            activeQuizQuestions != null -> {
                activeQuizQuestions = null
            }
            currentDestination != AppDestination.DASHBOARD -> {
                currentDestination = AppDestination.DASHBOARD
            }
        }
    }

    val user = currentUser ?: firestoreProfile?.toUserEntity()

    if (user == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isLoadingRole) {
                    CircularProgressIndicator()
                    Text(
                        text = "Fetching your role from Firestore ('users/{uid}')...",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    Text(
                        text = authError ?: "Unable to load user profile from Firestore.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(onClick = { viewModel.fetchAndEnsureFirestoreRole() }) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Retry")
                        }
                        OutlinedButton(onClick = onSignOut) {
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sign Out")
                        }
                    }
                }
            }
        }
        return
    }

    // Active Quiz Screen
    if (activeQuizQuestions != null) {
        ActiveQuizScreen(
            questions = activeQuizQuestions!!,
            mode = activeQuizMode,
            subjectName = activeQuizSubjectName,
            onCompleteQuiz = { attempt, answers ->
                viewModel.submitQuizAttempt(attempt, answers)
                completedAttempt = attempt
                completedAnswers = answers
                activeQuizQuestions = null
            },
            onCancelQuiz = { activeQuizQuestions = null },
            isBookmarked = { id -> bookmarkedIds.contains(id) },
            onToggleBookmark = { id -> viewModel.toggleBookmark(id) }
        )
        return
    }

    // Completed Quiz Result Screen
    if (completedAttempt != null) {
        QuizResultScreen(
            attempt = completedAttempt!!,
            answers = completedAnswers,
            questions = questions.filter { q -> completedAnswers.any { it.questionId == q.question.id } },
            onRetryMissed = { missed ->
                activeQuizQuestions = missed
                activeQuizMode = QuizMode.PRACTICE
                activeQuizSubjectName = "Remediation Practice"
                completedAttempt = null
            },
            onFinish = {
                completedAttempt = null
                completedAnswers = emptyList()
                currentDestination = AppDestination.DASHBOARD
            }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "EduBank MCQ",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                        RoleBadge(role = user.role)
                    }
                },
                actions = {
                    IconButton(
                        onClick = onSignOut,
                        modifier = Modifier.testTag("topbar_logout_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Sign Out"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                windowInsets = WindowInsets.navigationBars,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                AppDestination.values().forEach { destination ->
                    NavigationBarItem(
                        selected = currentDestination == destination,
                        onClick = { currentDestination = destination },
                        icon = { Icon(destination.icon, contentDescription = destination.label) },
                        label = { Text(destination.label) },
                        modifier = Modifier.testTag("nav_${destination.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentDestination) {
                AppDestination.DASHBOARD -> {
                    when (user.role) {
                        UserRole.ADMIN -> {
                            AdminDashboardScreen(
                                currentUser = user,
                                totalUsers = totalUsers,
                                activeUsers = activeUsers,
                                totalQuestions = totalQuestions,
                                totalAttempts = totalAttempts,
                                allUsers = allUsers,
                                recentAuditLogs = auditLogs,
                                pendingTasksCount = pendingCount,
                                onUpdateRole = { uid, r -> viewModel.updateUserRole(uid, r) },
                                onUpdateStatus = { uid, s -> viewModel.updateUserStatus(uid, s) },
                                onNavigateToQuestions = { currentDestination = AppDestination.QUESTION_BANK },
                                onNavigateToModeration = { currentDestination = AppDestination.QUESTION_BANK }
                            )
                        }
                        UserRole.MODERATOR -> {
                            ModeratorDashboardScreen(
                                currentUser = user,
                                pendingTasks = pendingTasks,
                                recentTasks = recentTasks,
                                pendingCount = pendingCount,
                                approvedCount = approvedCount,
                                rejectedCount = rejectedCount,
                                onResolveTask = { qId, act, notes -> viewModel.resolveModerationTask(qId, act, notes) }
                            )
                        }
                        UserRole.ADVISOR -> {
                            AdvisorDashboardScreen(
                                currentUser = user,
                                students = allUsers.filter { it.role == UserRole.MEMBER },
                                recommendations = allRecommendations,
                                subjects = subjects,
                                onCreateRecommendation = { uid, uname, sname, tname, title, note, pri ->
                                    viewModel.createAdvisorRecommendation(uid, uname, sname, tname, title, note, pri)
                                }
                            )
                        }
                        UserRole.MEMBER -> {
                            MemberDashboardScreen(
                                currentUser = user,
                                attempts = userAttempts,
                                recommendations = userRecommendations,
                                bookmarkedCount = bookmarkedIds.size,
                                onLaunchPractice = { currentDestination = AppDestination.QUIZ_PRACTICE },
                                onLaunchExam = { currentDestination = AppDestination.QUIZ_PRACTICE },
                                onTrainWeakTopics = {
                                    coroutineScope.launch {
                                        val weakQuestions = viewModel.generateQuiz("subj_cs", null, 5)
                                        activeQuizQuestions = weakQuestions
                                        activeQuizMode = QuizMode.PRACTICE
                                        activeQuizSubjectName = "Weak Topics: Tree Traversals & BST"
                                    }
                                },
                                onViewBookmarks = { currentDestination = AppDestination.QUESTION_BANK },
                                onMarkRecommendationComplete = { recId ->
                                    viewModel.markRecommendationComplete(recId)
                                }
                            )
                        }
                    }
                }
                AppDestination.QUESTION_BANK -> {
                    QuestionBankScreen(
                        questions = questions,
                        subjects = subjects,
                        currentUser = user,
                        bookmarkedIds = bookmarkedIds,
                        onBookmarkToggle = { qId -> viewModel.toggleBookmark(qId) },
                        onSaveQuestion = { q, opts -> viewModel.saveQuestion(q, opts) },
                        onDuplicateQuestion = { qId -> viewModel.duplicateQuestion(qId) },
                        onArchiveQuestion = { qId -> viewModel.archiveQuestion(qId) },
                        onDeleteQuestion = { qId -> viewModel.deleteQuestion(qId) },
                        onExportJson = { viewModel.exportJson() },
                        onImportJson = { json -> viewModel.importJson(json) }
                    )
                }
                AppDestination.QUIZ_PRACTICE -> {
                    QuizConfigScreen(
                        subjects = subjects,
                        onStartQuiz = { subjectId, diff, limit, mode ->
                            coroutineScope.launch {
                                val generated = viewModel.generateQuiz(subjectId, diff, limit)
                                activeQuizQuestions = generated
                                activeQuizMode = mode
                                activeQuizSubjectName = subjects.firstOrNull { it.id == subjectId }?.name ?: "All Disciplines"
                            }
                        }
                    )
                }
                AppDestination.PROFILE -> {
                    ProfileScreen(
                        currentUser = user,
                        isDarkTheme = isDarkTheme,
                        onToggleDarkTheme = onToggleDarkTheme,
                        onLogout = onSignOut
                    )
                }
            }
        }
    }
}
