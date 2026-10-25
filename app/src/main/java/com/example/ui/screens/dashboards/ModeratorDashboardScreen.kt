package com.example.ui.screens.dashboards

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.ui.components.RoleBadge
import com.example.ui.components.StatCard
import com.example.ui.theme.*

@Composable
fun ModeratorDashboardScreen(
    currentUser: UserEntity,
    pendingTasks: List<ModerationTaskEntity>,
    recentTasks: List<ModerationTaskEntity>,
    pendingCount: Int,
    approvedCount: Int,
    rejectedCount: Int,
    onResolveTask: (questionId: String, action: ModerationAction, feedbackNotes: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var taskToReject by remember { mutableStateOf<ModerationTaskEntity?>(null) }
    var taskToRevise by remember { mutableStateOf<ModerationTaskEntity?>(null) }
    var feedbackInput by remember { mutableStateOf("") }

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
                        text = "Moderation Queue",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Curate content quality, review community submissions & enforce standards",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                RoleBadge(role = UserRole.MODERATOR)
            }
        }

        // Stats Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Pending Review",
                    value = pendingCount.toString(),
                    subtitle = "Action required",
                    icon = Icons.Default.HourglassEmpty,
                    iconTint = EduAmberAccent,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Approved",
                    value = approvedCount.toString(),
                    subtitle = "Published to Bank",
                    icon = Icons.Default.CheckCircle,
                    iconTint = EduEmeraldSuccess,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Rejected",
                    value = rejectedCount.toString(),
                    subtitle = "Filtered out",
                    icon = Icons.Default.Cancel,
                    iconTint = EduRoseDanger,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Pending Submissions Section
        item {
            Text(
                text = "Pending Question Submissions (${pendingTasks.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (pendingTasks.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.TaskAlt, null, tint = EduEmeraldSuccess, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("All Submissions Reviewed!", fontWeight = FontWeight.Bold)
                        Text("No pending community questions in the moderation queue.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(pendingTasks) { task ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Submitted by ${task.submittedByName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Box(
                                modifier = Modifier
                                    .background(EduAmberAccent.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("NEEDS REVIEW", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EduAmberAccent)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = task.questionTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (task.feedbackNotes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Author's Note: ${task.feedbackNotes}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Action Buttons: Approve, Revise, Reject
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    onResolveTask(task.questionId, ModerationAction.APPROVED, "Approved by ${currentUser.displayName}")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EduEmeraldSuccess),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("approve_task_${task.id}")
                            ) {
                                Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Approve")
                            }

                            OutlinedButton(
                                onClick = {
                                    taskToRevise = task
                                    feedbackInput = "Please add a more comprehensive explanation for distractor options."
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("revise_task_${task.id}")
                            ) {
                                Text("Revise")
                            }

                            OutlinedButton(
                                onClick = {
                                    taskToReject = task
                                    feedbackInput = "Question does not adhere to syllabus standards or is duplicate."
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = EduRoseDanger),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("reject_task_${task.id}")
                            ) {
                                Text("Reject")
                            }
                        }
                    }
                }
            }
        }

        // Recent Moderation History
        item {
            Text(
                text = "Recent Moderation Activity",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(recentTasks.filter { it.action != ModerationAction.SUBMITTED }.take(5)) { task ->
            val color = when (task.action) {
                ModerationAction.APPROVED -> EduEmeraldSuccess
                ModerationAction.REJECTED -> EduRoseDanger
                ModerationAction.REVISION_REQUESTED -> EduVioletAdvisor
                else -> Slate400
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(task.questionTitle, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            text = "${task.action.label} • Reviewer: ${task.reviewerName ?: "Faculty"}",
                            fontSize = 11.sp,
                            color = color
                        )
                    }
                }
            }
        }
    }

    // Reject Dialog
    taskToReject?.let { task ->
        AlertDialog(
            onDismissRequest = { taskToReject = null },
            title = { Text("Reject Submission") },
            text = {
                Column {
                    Text("Provide reason for rejecting '${task.questionTitle}':")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = feedbackInput,
                        onValueChange = { feedbackInput = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResolveTask(task.questionId, ModerationAction.REJECTED, feedbackInput)
                        taskToReject = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EduRoseDanger),
                    modifier = Modifier.testTag("confirm_reject_button")
                ) {
                    Text("Confirm Rejection")
                }
            },
            dismissButton = {
                TextButton(onClick = { taskToReject = null }) { Text("Cancel") }
            }
        )
    }

    // Revision Request Dialog
    taskToRevise?.let { task ->
        AlertDialog(
            onDismissRequest = { taskToRevise = null },
            title = { Text("Request Revision") },
            text = {
                Column {
                    Text("Specify revision instructions for the author:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = feedbackInput,
                        onValueChange = { feedbackInput = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResolveTask(task.questionId, ModerationAction.REVISION_REQUESTED, feedbackInput)
                        taskToRevise = null
                    },
                    modifier = Modifier.testTag("confirm_revise_button")
                ) {
                    Text("Send Revision Request")
                }
            },
            dismissButton = {
                TextButton(onClick = { taskToRevise = null }) { Text("Cancel") }
            }
        )
    }
}
