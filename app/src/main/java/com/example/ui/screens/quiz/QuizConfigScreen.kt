package com.example.ui.screens.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DifficultyLevel
import com.example.data.model.QuizMode
import com.example.data.model.SubjectEntity
import com.example.ui.theme.EduAmberAccent
import com.example.ui.theme.EduBluePrimary

@Composable
fun QuizConfigScreen(
    subjects: List<SubjectEntity>,
    onStartQuiz: (subjectId: String, difficulty: DifficultyLevel?, limit: Int, mode: QuizMode) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSubjectId by remember { mutableStateOf("") } // empty = All
    var selectedDifficulty by remember { mutableStateOf<DifficultyLevel?>(null) }
    var selectedCount by remember { mutableStateOf(5) }
    var selectedMode by remember { mutableStateOf(QuizMode.PRACTICE) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Hero Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    RoundedCornerShape(20.dp)
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Column {
                        Text(
                            text = "Interactive Practice Engine",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Configure custom learning sessions or simulated examinations",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Mode Selection
        Text(
            text = "Select Assessment Mode:",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Practice Mode Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { selectedMode = QuizMode.PRACTICE }
                    .testTag("select_mode_practice"),
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedMode == QuizMode.PRACTICE) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                ),
                border = if (selectedMode == QuizMode.PRACTICE) CardDefaults.outlinedCardBorder().copy(width = 2.dp) else CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Bolt, null, tint = EduAmberAccent)
                        Text("Practice Mode", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Instant feedback after every question, reveals explanations immediately, retry missed questions.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Exam Mode Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { selectedMode = QuizMode.EXAMINATION }
                    .testTag("select_mode_exam"),
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedMode == QuizMode.EXAMINATION) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                ),
                border = if (selectedMode == QuizMode.EXAMINATION) CardDefaults.outlinedCardBorder().copy(width = 2.dp) else CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Timer, null, tint = MaterialTheme.colorScheme.primary)
                        Text("Examination", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Timed formal test, question palette navigation, review only after complete submission.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Subject Selection
        Text(
            text = "Target Subject:",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedSubjectId.isEmpty(),
                onClick = { selectedSubjectId = "" },
                label = { Text("All Subjects") },
                modifier = Modifier.testTag("quiz_subject_all")
            )
            subjects.forEach { s ->
                FilterChip(
                    selected = selectedSubjectId == s.id,
                    onClick = { selectedSubjectId = s.id },
                    label = { Text(s.code) },
                    modifier = Modifier.testTag("quiz_subject_${s.code}")
                )
            }
        }

        // Difficulty Selection
        Text(
            text = "Difficulty Level:",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedDifficulty == null,
                onClick = { selectedDifficulty = null },
                label = { Text("Any Difficulty") },
                modifier = Modifier.testTag("quiz_diff_any")
            )
            DifficultyLevel.values().forEach { d ->
                FilterChip(
                    selected = selectedDifficulty == d,
                    onClick = { selectedDifficulty = d },
                    label = { Text(d.label) },
                    modifier = Modifier.testTag("quiz_diff_${d.name}")
                )
            }
        }

        // Number of Questions
        Text(
            text = "Number of Questions:",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf(5, 10, 15, 20).forEach { count ->
                FilterChip(
                    selected = selectedCount == count,
                    onClick = { selectedCount = count },
                    label = { Text("$count MCQs") },
                    modifier = Modifier.testTag("quiz_count_$count")
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Start Quiz Button
        Button(
            onClick = {
                onStartQuiz(selectedSubjectId, selectedDifficulty, selectedCount, selectedMode)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("launch_quiz_button"),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Launch ${selectedMode.label} ($selectedCount Questions)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
