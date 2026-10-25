package com.example.ui.screens.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.DiagramRenderer
import com.example.ui.theme.EduAmberAccent
import com.example.ui.theme.EduEmeraldSuccess
import com.example.ui.theme.EduRoseDanger
import kotlinx.coroutines.delay
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveQuizScreen(
    questions: List<QuestionWithOptions>,
    mode: QuizMode,
    subjectName: String,
    onCompleteQuiz: (QuizAttemptEntity, List<QuizAnswerEntity>) -> Unit,
    onCancelQuiz: () -> Unit,
    isBookmarked: (String) -> Boolean,
    onToggleBookmark: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (questions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("No questions available for this configuration.")
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onCancelQuiz) { Text("Go Back") }
            }
        }
        return
    }

    var currentIndex by remember { mutableStateOf(0) }
    // User selected options: questionId -> Set of option IDs
    val selectedAnswers = remember { mutableStateMapOf<String, Set<String>>() }
    // Practice mode checked questions: questionId -> Boolean
    val practiceCheckedQuestions = remember { mutableStateMapOf<String, Boolean>() }
    // Exam mode flagged questions: questionId -> Boolean
    val flaggedQuestions = remember { mutableStateMapOf<String, Boolean>() }

    var timeSeconds by remember { mutableStateOf(0) }
    var isTimerRunning by remember { mutableStateOf(true) }

    var showPaletteSheet by remember { mutableStateOf(false) }
    var showSubmitConfirm by remember { mutableStateOf(false) }

    // Timer effect
    LaunchedEffect(isTimerRunning) {
        while (isTimerRunning) {
            delay(1000L)
            timeSeconds++
        }
    }

    val currentQ = questions[currentIndex]
    val q = currentQ.question
    val currentSelections = selectedAnswers[q.id] ?: emptySet()
    val isPracticeChecked = practiceCheckedQuestions[q.id] == true
    val isFlagged = flaggedQuestions[q.id] == true

    val scrollState = rememberScrollState()

    fun finalizeSubmission() {
        isTimerRunning = false
        var correctCount = 0
        val answerEntities = mutableListOf<QuizAnswerEntity>()

        for (item in questions) {
            val qId = item.question.id
            val userSelected = selectedAnswers[qId] ?: emptySet()
            val correctOptionIds = item.options.filter { it.isCorrect }.map { it.id }.toSet()

            val isCorrect = userSelected.isNotEmpty() && userSelected == correctOptionIds
            if (isCorrect) correctCount++

            answerEntities.add(
                QuizAnswerEntity(
                    id = "ans_${UUID.randomUUID().toString().take(8)}",
                    quizAttemptId = "", // filled when saved
                    questionId = qId,
                    selectedOptionIdsJson = userSelected.joinToString(","),
                    isCorrect = isCorrect,
                    timeSpentSeconds = (timeSeconds / questions.size.coerceAtLeast(1))
                )
            )
        }

        val percentage = (correctCount.toFloat() / questions.size.toFloat()) * 100f
        val attempt = QuizAttemptEntity(
            id = "att_${UUID.randomUUID().toString().take(8)}",
            userId = "", // filled by caller
            title = if (subjectName.isBlank()) "Comprehensive Quiz" else "$subjectName Assessment",
            subjectId = q.subjectId,
            subjectName = if (subjectName.isBlank()) "All Disciplines" else subjectName,
            mode = mode,
            totalQuestions = questions.size,
            correctCount = correctCount,
            timeSpentSeconds = timeSeconds.toLong(),
            scorePercentage = percentage
        )

        onCompleteQuiz(attempt, answerEntities)
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Row: Exit, Mode badge, Timer, Palette button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onCancelQuiz,
                        modifier = Modifier.testTag("exit_quiz_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Exit Quiz")
                    }

                    // Mode Badge
                    Box(
                        modifier = Modifier
                            .background(
                                if (mode == QuizMode.PRACTICE) EduAmberAccent.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primaryContainer,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = mode.label,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (mode == QuizMode.PRACTICE) EduAmberAccent else MaterialTheme.colorScheme.primary
                        )
                    }

                    // Timer
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Timer, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        val mins = timeSeconds / 60
                        val secs = timeSeconds % 60
                        Text(
                            text = "%02d:%02d".format(mins, secs),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    // Palette / Overview button
                    IconButton(
                        onClick = { showPaletteSheet = true },
                        modifier = Modifier.testTag("open_palette_button")
                    ) {
                        Icon(Icons.Default.GridView, contentDescription = "Question Palette")
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Progress Bar
                LinearProgressIndicator(
                    progress = { (currentIndex + 1).toFloat() / questions.size.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    color = MaterialTheme.colorScheme.primary,
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Question ${currentIndex + 1} of ${questions.size}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = q.difficulty.label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = when (q.difficulty) {
                            DifficultyLevel.EASY -> EduEmeraldSuccess
                            DifficultyLevel.MEDIUM -> EduAmberAccent
                            DifficultyLevel.HARD -> EduRoseDanger
                        }
                    )
                }
            }
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous Button
                    OutlinedButton(
                        onClick = { if (currentIndex > 0) currentIndex-- },
                        enabled = currentIndex > 0,
                        modifier = Modifier.testTag("prev_question_button")
                    ) {
                        Icon(Icons.Default.ArrowBack, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Prev")
                    }

                    // Middle action: Flag in Exam or Check in Practice
                    if (mode == QuizMode.EXAMINATION) {
                        IconButton(
                            onClick = { flaggedQuestions[q.id] = !isFlagged },
                            modifier = Modifier.testTag("flag_question_button")
                        ) {
                            Icon(
                                imageVector = if (isFlagged) Icons.Filled.Flag else Icons.Outlined.Flag,
                                contentDescription = "Flag for review",
                                tint = if (isFlagged) EduAmberAccent else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else if (mode == QuizMode.PRACTICE && !isPracticeChecked) {
                        Button(
                            onClick = {
                                if (currentSelections.isNotEmpty()) {
                                    practiceCheckedQuestions[q.id] = true
                                }
                            },
                            enabled = currentSelections.isNotEmpty(),
                            modifier = Modifier.testTag("check_answer_button")
                        ) {
                            Text("Check Answer")
                        }
                    }

                    // Next or Finish Button
                    if (currentIndex < questions.size - 1) {
                        Button(
                            onClick = { currentIndex++ },
                            modifier = Modifier.testTag("next_question_button")
                        ) {
                            Text("Next")
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.ArrowForward, null, modifier = Modifier.size(16.dp))
                        }
                    } else {
                        Button(
                            onClick = { showSubmitConfirm = true },
                            colors = ButtonDefaults.buttonColors(containerColor = EduEmeraldSuccess),
                            modifier = Modifier.testTag("submit_exam_button")
                        ) {
                            Text("Submit Exam")
                        }
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Bookmark icon & Question Type
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AssistChip(
                    onClick = {},
                    label = { Text(q.type.title) }
                )
                IconButton(
                    onClick = { onToggleBookmark(q.id) },
                    modifier = Modifier.testTag("quiz_bookmark_button")
                ) {
                    Icon(
                        imageVector = if (isBookmarked(q.id)) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = if (isBookmarked(q.id)) EduAmberAccent else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Question Title & Content
            Text(
                text = q.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = q.content,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Case Vignette if present
            if (!q.caseScenario.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "Case Scenario Vignette:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = q.caseScenario,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Diagram if present
            if (q.diagramType != null) {
                DiagramRenderer(diagramType = q.diagramType)
            }

            // Answer Options
            Text(
                text = if (q.type == QuestionType.MULTI_CHOICE) "Select all options that apply:" else "Select the best option:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            currentQ.options.sortedBy { it.orderIndex }.forEachIndexed { index, opt ->
                val isSelected = currentSelections.contains(opt.id)
                val isCorrect = opt.isCorrect

                // In practice mode after checking: highlight green if correct, red if selected incorrect
                val containerColor = when {
                    mode == QuizMode.PRACTICE && isPracticeChecked && isCorrect -> EduEmeraldSuccess.copy(alpha = 0.15f)
                    mode == QuizMode.PRACTICE && isPracticeChecked && isSelected && !isCorrect -> EduRoseDanger.copy(alpha = 0.15f)
                    isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    else -> MaterialTheme.colorScheme.surface
                }

                val borderColor = when {
                    mode == QuizMode.PRACTICE && isPracticeChecked && isCorrect -> EduEmeraldSuccess
                    mode == QuizMode.PRACTICE && isPracticeChecked && isSelected && !isCorrect -> EduRoseDanger
                    isSelected -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !(mode == QuizMode.PRACTICE && isPracticeChecked)) {
                            if (q.type == QuestionType.MULTI_CHOICE) {
                                val current = currentSelections.toMutableSet()
                                if (isSelected) current.remove(opt.id) else current.add(opt.id)
                                selectedAnswers[q.id] = current
                            } else {
                                selectedAnswers[q.id] = setOf(opt.id)
                            }
                        }
                        .testTag("quiz_option_${opt.id}"),
                    colors = CardDefaults.cardColors(containerColor = containerColor),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder().copy(width = if (isSelected || (mode == QuizMode.PRACTICE && isPracticeChecked && isCorrect)) 2.dp else 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (q.type == QuestionType.MULTI_CHOICE) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = null,
                                    enabled = !(mode == QuizMode.PRACTICE && isPracticeChecked)
                                )
                            } else {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = null,
                                    enabled = !(mode == QuizMode.PRACTICE && isPracticeChecked)
                                )
                            }

                            Text(
                                text = opt.text,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )

                            if (mode == QuizMode.PRACTICE && isPracticeChecked) {
                                if (isCorrect) {
                                    Icon(Icons.Default.CheckCircle, "Correct", tint = EduEmeraldSuccess)
                                } else if (isSelected) {
                                    Icon(Icons.Default.Cancel, "Incorrect", tint = EduRoseDanger)
                                }
                            }
                        }

                        // In practice mode after checking, show option rationale
                        if (mode == QuizMode.PRACTICE && isPracticeChecked && opt.explanation.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = opt.explanation,
                                fontSize = 11.sp,
                                color = if (isCorrect) EduEmeraldSuccess else EduRoseDanger,
                                modifier = Modifier.padding(start = 36.dp)
                            )
                        }
                    }
                }
            }

            // In Practice Mode: Full Explanation Box
            if (mode == QuizMode.PRACTICE && isPracticeChecked) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Lightbulb, null, tint = EduAmberAccent)
                            Text("Academic Explanation", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = q.explanation.ifBlank { "Solution verified by faculty review committee." },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        TextButton(
                            onClick = {
                                practiceCheckedQuestions[q.id] = false
                                selectedAnswers[q.id] = emptySet()
                            },
                            modifier = Modifier.testTag("retry_question_button")
                        ) {
                            Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Retry this Question")
                        }
                    }
                }
            }
        }
    }

    // Palette Bottom Sheet
    if (showPaletteSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPaletteSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Question Palette Navigator",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(modifier = Modifier.size(12.dp).background(EduEmeraldSuccess, CircleShape))
                        Text("Answered", fontSize = 11.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(modifier = Modifier.size(12.dp).background(EduAmberAccent, CircleShape))
                        Text("Flagged", fontSize = 11.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(modifier = Modifier.size(12.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape))
                        Text("Unanswered", fontSize = 11.sp)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 240.dp)
                ) {
                    itemsIndexed(questions) { idx, item ->
                        val isAnswered = selectedAnswers[item.question.id]?.isNotEmpty() == true
                        val isF = flaggedQuestions[item.question.id] == true
                        val isCur = idx == currentIndex

                        val bg = when {
                            isCur -> MaterialTheme.colorScheme.primary
                            isF -> EduAmberAccent
                            isAnswered -> EduEmeraldSuccess
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }

                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(bg, RoundedCornerShape(10.dp))
                                .clickable {
                                    currentIndex = idx
                                    showPaletteSheet = false
                                }
                                .testTag("palette_item_$idx"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${idx + 1}",
                                fontWeight = FontWeight.Bold,
                                color = if (isCur || isAnswered || isF) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    // Submit Confirmation Dialog
    if (showSubmitConfirm) {
        val answeredCount = selectedAnswers.count { it.value.isNotEmpty() }
        AlertDialog(
            onDismissRequest = { showSubmitConfirm = false },
            title = { Text("Submit Assessment?") },
            text = {
                Text("You have answered $answeredCount of ${questions.size} questions. Are you ready to submit and calculate your final performance score?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSubmitConfirm = false
                        finalizeSubmission()
                    },
                    modifier = Modifier.testTag("confirm_submit_exam_button")
                ) {
                    Text("Confirm Submit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSubmitConfirm = false }) {
                    Text("Keep Working")
                }
            }
        )
    }
}
