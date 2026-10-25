package com.example.ui.screens.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.QuestionWithOptions
import com.example.data.model.QuizAnswerEntity
import com.example.data.model.QuizAttemptEntity
import com.example.ui.theme.EduAmberAccent
import com.example.ui.theme.EduEmeraldSuccess
import com.example.ui.theme.EduRoseDanger

@Composable
fun QuizResultScreen(
    attempt: QuizAttemptEntity,
    answers: List<QuizAnswerEntity>,
    questions: List<QuestionWithOptions>,
    onRetryMissed: (List<QuestionWithOptions>) -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPassed = attempt.scorePercentage >= 70.0f
    val scoreColor = if (isPassed) EduEmeraldSuccess else if (attempt.scorePercentage >= 50f) EduAmberAccent else EduRoseDanger

    val missedQuestions = questions.filter { q ->
        val ans = answers.firstOrNull { it.questionId == q.question.id }
        ans?.isCorrect == false
    }

    Scaffold(
        bottomBar = {
            Surface(tonalElevation = 6.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (missedQuestions.isNotEmpty()) {
                        OutlinedButton(
                            onClick = { onRetryMissed(missedQuestions) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("retry_missed_questions_button")
                        ) {
                            Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Retry Missed (${missedQuestions.size})")
                        }
                    }

                    Button(
                        onClick = onFinish,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("finish_quiz_result_button")
                    ) {
                        Text("Back to Dashboard")
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Score Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .background(scoreColor.copy(alpha = 0.15f), CircleShape)
                                .border(4.dp, scoreColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "%.0f%%".format(attempt.scorePercentage),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = scoreColor
                                )
                                Text(
                                    text = if (isPassed) "PASSED" else "NEEDS REVIEW",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = scoreColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = attempt.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = "${attempt.mode.label} • ${attempt.subjectName}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Metric pills row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${attempt.correctCount} / ${attempt.totalQuestions}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = EduEmeraldSuccess
                                )
                                Text("Correct", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${attempt.totalQuestions - attempt.correctCount}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = EduRoseDanger
                                )
                                Text("Incorrect", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                val mins = attempt.timeSpentSeconds / 60
                                val secs = attempt.timeSpentSeconds % 60
                                Text(
                                    text = "%02d:%02d".format(mins, secs),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text("Time Spent", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Question Review & Rationales",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Question by question review
            itemsIndexed(questions) { index, item ->
                val q = item.question
                val ans = answers.firstOrNull { it.questionId == q.id }
                val isCorrect = ans?.isCorrect == true
                val selectedOptionIds = ans?.selectedOptionIdsJson?.split(",")?.filter { it.isNotBlank() }?.toSet() ?: emptySet()

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Q${index + 1}. ${q.title}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (isCorrect) EduEmeraldSuccess.copy(alpha = 0.15f) else EduRoseDanger.copy(alpha = 0.15f),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isCorrect) "CORRECT" else "INCORRECT",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = if (isCorrect) EduEmeraldSuccess else EduRoseDanger
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = q.content,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Options review
                        item.options.sortedBy { it.orderIndex }.forEach { opt ->
                            val isSelected = selectedOptionIds.contains(opt.id)
                            val optCorrect = opt.isCorrect

                            val optBg = when {
                                optCorrect -> EduEmeraldSuccess.copy(alpha = 0.1f)
                                isSelected && !optCorrect -> EduRoseDanger.copy(alpha = 0.1f)
                                else -> Color.Transparent
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(optBg, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (optCorrect) {
                                    Icon(Icons.Default.Check, null, tint = EduEmeraldSuccess, modifier = Modifier.size(16.dp))
                                } else if (isSelected) {
                                    Icon(Icons.Default.Close, null, tint = EduRoseDanger, modifier = Modifier.size(16.dp))
                                } else {
                                    Spacer(modifier = Modifier.width(16.dp))
                                }

                                Text(
                                    text = opt.text,
                                    fontSize = 12.sp,
                                    fontWeight = if (optCorrect || isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // Explanation
                        if (q.explanation.isNotBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "Explanation: ${q.explanation}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
