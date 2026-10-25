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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvisorDashboardScreen(
    currentUser: UserEntity,
    students: List<UserEntity>,
    recommendations: List<AdvisorRecommendationEntity>,
    subjects: List<SubjectEntity>,
    onCreateRecommendation: (targetUserId: String, targetUserName: String, subjectName: String, topicName: String, title: String, note: String, priority: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCreateDialog by remember { mutableStateOf(false) }

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
                        text = "Academic Advisor Center",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Provide pedagogical guidance, curate study paths & monitor mastery",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                RoleBadge(role = UserRole.ADVISOR)
            }
        }

        // Stats
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Assigned Members",
                    value = students.size.toString(),
                    subtitle = "Enrolled in advising",
                    icon = Icons.Default.School,
                    iconTint = EduVioletAdvisor,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Recommendations",
                    value = recommendations.size.toString(),
                    subtitle = "Active guidance plans",
                    icon = Icons.Default.Recommend,
                    iconTint = EduEmeraldSuccess,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Action Button: Send Recommendation
        item {
            Button(
                onClick = { showCreateDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("open_create_rec_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create New Student Guidance Recommendation")
            }
        }

        // Student Progress Overview
        item {
            Text(
                text = "Member Learning & Progress Telemetry",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(students) { student ->
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
                        Column {
                            Text(student.displayName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(student.email, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Box(
                            modifier = Modifier
                                .background(EduEmeraldSuccess.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text("75% Avg Accuracy", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EduEmeraldSuccess)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AssistChip(
                            onClick = {},
                            label = { Text("Weakness: Tree Traversals", fontSize = 10.sp) },
                            colors = AssistChipDefaults.assistChipColors(containerColor = EduRoseDanger.copy(alpha = 0.1f))
                        )
                        AssistChip(
                            onClick = {},
                            label = { Text("Strength: Sorting & Big-O", fontSize = 10.sp) },
                            colors = AssistChipDefaults.assistChipColors(containerColor = EduEmeraldSuccess.copy(alpha = 0.1f))
                        )
                    }
                }
            }
        }

        // Active Advisor Recommendations
        item {
            Text(
                text = "Active Academic Recommendations (${recommendations.size})",
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
                            text = "To: ${rec.targetUserName}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Box(
                            modifier = Modifier
                                .background(
                                    if (rec.priority == "HIGH") EduRoseDanger.copy(alpha = 0.15f) else EduAmberAccent.copy(alpha = 0.15f),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${rec.priority} PRIORITY",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (rec.priority == "HIGH") EduRoseDanger else EduAmberAccent
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(rec.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Subject: ${rec.subjectName} • Topic: ${rec.topicName}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(rec.note, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }

    // Create Recommendation Dialog
    if (showCreateDialog) {
        var targetStudent by remember { mutableStateOf(students.firstOrNull()) }
        var selectedSubject by remember { mutableStateOf(subjects.firstOrNull()) }
        var topicInput by remember { mutableStateOf("Tree Traversals & Balancing") }
        var titleInput by remember { mutableStateOf("Targeted BST Practice Drill") }
        var noteInput by remember { mutableStateOf("Complete 5 questions in Practice Mode before taking the Friday exam.") }
        var priority by remember { mutableStateOf("HIGH") }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Create Guidance Recommendation") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Target Student
                    var studentExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = studentExpanded,
                        onExpandedChange = { studentExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = targetStudent?.displayName ?: "Select Student",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Student") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = studentExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = studentExpanded,
                            onDismissRequest = { studentExpanded = false }
                        ) {
                            students.forEach { s ->
                                DropdownMenuItem(
                                    text = { Text(s.displayName) },
                                    onClick = {
                                        targetStudent = s
                                        studentExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Title
                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Topic
                    OutlinedTextField(
                        value = topicInput,
                        onValueChange = { topicInput = it },
                        label = { Text("Focus Topic") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Guidance Note
                    OutlinedTextField(
                        value = noteInput,
                        onValueChange = { noteInput = it },
                        label = { Text("Guidance & Remediation Instructions") },
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val s = targetStudent ?: return@Button
                        onCreateRecommendation(
                            s.id,
                            s.displayName,
                            selectedSubject?.name ?: "Computer Science",
                            topicInput,
                            titleInput,
                            noteInput,
                            priority
                        )
                        showCreateDialog = false
                    },
                    modifier = Modifier.testTag("submit_recommendation_button")
                ) {
                    Text("Send Recommendation")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
