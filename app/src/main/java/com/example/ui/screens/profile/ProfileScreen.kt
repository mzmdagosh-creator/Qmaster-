package com.example.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.security.Permission
import com.example.security.SecurityGuard
import com.example.ui.components.RoleBadge
import com.example.ui.theme.EduEmeraldSuccess
import com.example.ui.theme.EduRoseDanger
import com.example.ui.theme.Slate400

@Composable
fun ProfileScreen(
    currentUser: UserEntity,
    isDarkTheme: Boolean,
    onToggleDarkTheme: (Boolean) -> Unit,
    onOpenUserSwitcher: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentUser.displayName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = currentUser.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        RoleBadge(role = currentUser.role)
                    }
                }
            }
        }

        // Persona Quick-Switch Action Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Switch Active RBAC Persona", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Test the UI from another user's vantage point", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Button(
                        onClick = onOpenUserSwitcher,
                        modifier = Modifier.testTag("profile_switch_persona_button")
                    ) {
                        Text("Switch Role")
                    }
                }
            }
        }

        // RBAC Permissions Checklist for Current User
        item {
            Text(
                text = "RBAC Security & Permission Authorization Matrix",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Permissions granted to '${currentUser.role.displayName}' under server-side security policies:",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val permissionsList = listOf(
                        Pair(Permission.PRACTICE_AND_QUIZ, "Take Quizzes & Interactive Practice Sessions"),
                        Pair(Permission.CREATE_OR_SUBMIT_QUESTION, "Author & Submit New MCQs (Community or Direct)"),
                        Pair(Permission.MODERATE_QUESTIONS, "Approve, Reject & Revise Submissions in Queue"),
                        Pair(Permission.EDIT_ANY_QUESTION, "Edit Content Bank Questions Across Faculty"),
                        Pair(Permission.DELETE_ANY_QUESTION, "Permanently Delete Questions from Bank"),
                        Pair(Permission.CREATE_RECOMMENDATIONS, "Curate & Issue Academic Guidance Recommendations"),
                        Pair(Permission.VIEW_MEMBER_PROGRESS, "View Aggregated Student Performance & Mastery"),
                        Pair(Permission.EXPORT_DATA, "Export Question Bank Data in JSON Specification"),
                        Pair(Permission.IMPORT_DATA, "Bulk Import External Questions via JSON"),
                        Pair(Permission.MANAGE_USERS, "Modify Account Roles, Privileges & Suspend Users"),
                        Pair(Permission.VIEW_AUDIT_LOGS, "Inspect Tamper-Evident Security Audit Logs")
                    )

                    permissionsList.forEach { (perm, desc) ->
                        val granted = SecurityGuard.hasPermission(currentUser, perm)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = if (granted) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                contentDescription = if (granted) "Granted" else "Denied",
                                tint = if (granted) EduEmeraldSuccess else Slate400,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = desc,
                                fontSize = 12.sp,
                                fontWeight = if (granted) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (granted) MaterialTheme.colorScheme.onSurface else Slate400
                            )
                        }
                    }
                }
            }
        }

        // Appearance & Settings
        item {
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
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.DarkMode, null)
                            Text("Dark Theme", fontWeight = FontWeight.SemiBold)
                        }
                        Switch(
                            checked = isDarkTheme,
                            onCheckedChange = onToggleDarkTheme,
                            modifier = Modifier.testTag("dark_theme_switch")
                        )
                    }
                }
            }
        }

        // Sign Out Button
        item {
            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("profile_logout_button")
            ) {
                Icon(Icons.Default.Logout, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Out of Session")
            }
        }
    }
}
