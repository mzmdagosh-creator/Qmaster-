package com.example.ui.screens.dashboards

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.RoleBadge
import com.example.ui.components.StatCard
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MemberDashboardScreen(
    currentUser: UserEntity,
    attempts: List<QuizAttemptEntity>,
    recommendations: List<AdvisorRecommendationEntity>,
    bookmarkedCount: Int,
    onLaunchPractice: () -> Unit,
    onLaunchExam: () -> Unit,
    onTrainWeakTopics: () -> Unit,
    onViewBookmarks: () -> Unit,
    onMarkRecommendationComplete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val averageScore = if (attempts.isNotEmpty()) {
        attempts.map { it.scorePercentage }.average().toFloat()
    } else 0f

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Student Practice Hub",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Welcome, ${currentUser.displayName}. Track progress and master exam concepts.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                RoleBadge(role = UserRole.MEMBER)
            }
        }

        // Weak Topics Alert with 1-Tap Train Button
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = EduAmberAccent.copy(alpha = 0.12f)),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.TrendingDown, null, tint = EduRoseDanger)
                            Text(
                                text = "Identified Weak Topic Alert",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(EduRoseDanger.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("BST Traversals", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EduRoseDanger)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Your recent diagnostic test showed incorrect answers on Tree Traversals. Dedicated practice will reinforce your understanding.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onTrainWeakTopics,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("train_weak_topics_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = EduAmberAccent)
                    ) {
                        Icon(Icons.Default.Bolt, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Launch Targeted Practice Session", fontWeight = FontWeight.Bold, color = Slate900)
                    }
                }
            }
        }

        // Quick Launch Grid
        item {
            Text(
                text = "Interactive Practice Modes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onLaunchPractice() }
                        .testTag("member_launch_practice_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(14.dp),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(EduAmberAccent.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Bolt, null, tint = EduAmberAccent)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Practice Mode", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Instant feedback & retry", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onLaunchExam() }
                        .testTag("member_launch_exam_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(14.dp),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Timer, null, tint = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Examination", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Timed test assessment", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // Performance Statistics
        item {
            Text(
                text = "Performance Telemetry",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Average Score",
                    value = if (attempts.isNotEmpty()) "%.0f%%".format(averageScore) else "N/A",
                    subtitle = "Across all tests",
                    icon = Icons.Default.Grade,
                    iconTint = EduEmeraldSuccess,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Quizzes Taken",
                    value = attempts.size.toString(),
                    subtitle = "Completed sessions",
                    icon = Icons.Default.CheckCircle,
                    iconTint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Bookmarks",
                    value = bookmarkedCount.toString(),
                    subtitle = "Saved for revision",
                    icon = Icons.Default.Bookmark,
                    iconTint = EduAmberAccent,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Advisor Guidance Section
        if (recommendations.isNotEmpty()) {
            item {
                Text(
                    text = "Advisor Recommendations (${recommendations.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(recommendations) { rec ->
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
                                text = "From: ${rec.advisorName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (rec.isCompleted) {
                                Box(
                                    modifier = Modifier
                                        .background(EduEmeraldSuccess.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("COMPLETED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = EduEmeraldSuccess)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(rec.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Focus: ${rec.topicName}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(rec.note, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)

                        if (!rec.isCompleted) {
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(
                                onClick = { onMarkRecommendationComplete(rec.id) },
                                modifier = Modifier.testTag("complete_rec_${rec.id}")
                            ) {
                                Icon(Icons.Default.Done, null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Mark as Completed")
                            }
                        }
                    }
                }
            }
        }

        // Recent Quiz History
        item {
            Text(
                text = "Recent Assessment History",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (attempts.isEmpty()) {
            item {
                Text(
                    text = "No assessments taken yet. Launch your first practice session above!",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(attempts) { att ->
                val isPass = att.scorePercentage >= 70f
                val pillColor = if (isPass) EduEmeraldSuccess else EduAmberAccent

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(att.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                text = "${att.mode.label} • ${dateFormat.format(Date(att.completedAt))}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${att.correctCount} / ${att.totalQuestions} questions correct",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Box(
                            modifier = Modifier
                                .background(pillColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .border(1.dp, pillColor, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "%.0f%%".format(att.scorePercentage),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = pillColor
                            )
                        }
                    }
                }
            }
        }
    }
}
