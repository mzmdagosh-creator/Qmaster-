package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.security.Permission
import com.example.security.SecurityGuard
import com.example.ui.theme.*

@Composable
fun QuestionCard(
    questionWithOptions: QuestionWithOptions,
    currentUser: UserEntity,
    isBookmarked: Boolean,
    onBookmarkToggle: () -> Unit,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val q = questionWithOptions.question
    var menuExpanded by remember { mutableStateOf(false) }

    val canEdit = SecurityGuard.hasPermission(currentUser, Permission.EDIT_ANY_QUESTION) || q.authorUserId == currentUser.id
    val canDelete = SecurityGuard.hasPermission(currentUser, Permission.DELETE_ANY_QUESTION) || q.authorUserId == currentUser.id

    val diffColor = when (q.difficulty) {
        DifficultyLevel.EASY -> EduEmeraldSuccess
        DifficultyLevel.MEDIUM -> EduAmberAccent
        DifficultyLevel.HARD -> EduRoseDanger
    }

    val statusColor = when (q.status) {
        QuestionStatus.APPROVED -> EduEmeraldSuccess
        QuestionStatus.PENDING_REVIEW -> EduAmberAccent
        QuestionStatus.REJECTED -> EduRoseDanger
        QuestionStatus.REVISION_REQUESTED -> EduVioletAdvisor
        QuestionStatus.ARCHIVED -> Slate400
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("question_card_${q.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Badges row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Difficulty
                    Box(
                        modifier = Modifier
                            .background(diffColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = q.difficulty.label,
                            color = diffColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Type
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = q.type.title,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Status
                    Box(
                        modifier = Modifier
                            .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = q.status.label,
                            color = statusColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBookmarkToggle,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("bookmark_button_${q.id}")
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = if (isBookmarked) "Remove bookmark" else "Bookmark question",
                            tint = if (isBookmarked) EduAmberAccent else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box {
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("menu_button_${q.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More actions",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Duplicate") },
                                onClick = {
                                    menuExpanded = false
                                    onDuplicate()
                                },
                                leadingIcon = { Icon(Icons.Default.ContentCopy, null) }
                            )

                            if (canEdit) {
                                DropdownMenuItem(
                                    text = { Text("Edit") },
                                    onClick = {
                                        menuExpanded = false
                                        onEdit()
                                    },
                                    leadingIcon = { Icon(Icons.Default.Edit, null) }
                                )
                                DropdownMenuItem(
                                    text = { Text("Archive") },
                                    onClick = {
                                        menuExpanded = false
                                        onArchive()
                                    },
                                    leadingIcon = { Icon(Icons.Default.Archive, null) }
                                )
                            }

                            if (canDelete) {
                                DropdownMenuItem(
                                    text = { Text("Delete", color = EduRoseDanger) },
                                    onClick = {
                                        menuExpanded = false
                                        onDelete()
                                    },
                                    leadingIcon = { Icon(Icons.Default.Delete, null, tint = EduRoseDanger) }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title
            Text(
                text = q.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Content snippet
            Text(
                text = q.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Special badges: Diagram, Case Scenario
            if (q.diagramType != null || q.caseScenario != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (q.diagramType != null) {
                        AssistChip(
                            onClick = {},
                            label = { Text("Diagram: ${q.diagramType}", fontSize = 10.sp) },
                            leadingIcon = { Icon(Icons.Default.Image, null, modifier = Modifier.size(14.dp)) },
                            colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        )
                    }
                    if (q.caseScenario != null) {
                        AssistChip(
                            onClick = {},
                            label = { Text("Case Vignette", fontSize = 10.sp) },
                            leadingIcon = { Icon(Icons.Default.Article, null, modifier = Modifier.size(14.dp)) },
                            colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer info: options count, author
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${questionWithOptions.options.size} Options",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "By ${q.authorName.ifBlank { "Faculty" }}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
