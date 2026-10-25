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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AuditLogEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.data.model.UserStatus
import com.example.ui.components.RoleBadge
import com.example.ui.components.StatCard
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    currentUser: UserEntity,
    totalUsers: Int,
    activeUsers: Int,
    totalQuestions: Int,
    totalAttempts: Int,
    allUsers: List<UserEntity>,
    recentAuditLogs: List<AuditLogEntity>,
    pendingTasksCount: Int,
    onUpdateRole: (userId: String, newRole: UserRole) -> Unit,
    onUpdateStatus: (userId: String, newStatus: UserStatus) -> Unit,
    onNavigateToQuestions: () -> Unit,
    onNavigateToModeration: () -> Unit,
    modifier: Modifier = Modifier
) {
    var userToManage by remember { mutableStateOf<UserEntity?>(null) }
    val dateFormat = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }

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
                        text = "System Administration",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Welcome back, ${currentUser.displayName}. Full platform RBAC access granted.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                RoleBadge(role = UserRole.ADMIN)
            }
        }

        // Pending Moderation Alert Banner (if any)
        if (pendingTasksCount > 0) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = EduAmberAccent.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.HourglassTop, null, tint = EduAmberAccent)
                            Column {
                                Text(
                                    text = "$pendingTasksCount Questions Awaiting Review",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Submitted by members for question bank publication",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        TextButton(
                            onClick = onNavigateToModeration,
                            modifier = Modifier.testTag("admin_review_queue_button")
                        ) {
                            Text("Open Queue")
                        }
                    }
                }
            }
        }

        // Platform KPIs
        item {
            Text(
                text = "Platform Key Metrics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Total Users",
                    value = totalUsers.toString(),
                    subtitle = "$activeUsers Active Accounts",
                    icon = Icons.Default.People,
                    iconTint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Question Bank",
                    value = totalQuestions.toString(),
                    subtitle = "Across 4 Subjects",
                    icon = Icons.Default.MenuBook,
                    iconTint = EduEmeraldSuccess,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Quiz Attempts",
                    value = totalAttempts.toString(),
                    subtitle = "Practice & Exam Runs",
                    icon = Icons.Default.Analytics,
                    iconTint = EduVioletAdvisor,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Audit Events",
                    value = recentAuditLogs.size.toString(),
                    subtitle = "Secured Server Logs",
                    icon = Icons.Default.Security,
                    iconTint = EduRoseDanger,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // User Management Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "User Accounts & Role Assignments",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${allUsers.size} accounts",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(allUsers) { user ->
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
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = user.displayName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            if (user.status == UserStatus.SUSPENDED) {
                                Box(
                                    modifier = Modifier
                                        .background(EduRoseDanger.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("SUSPENDED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = EduRoseDanger)
                                }
                            }
                        }
                        Text(
                            text = "${user.email} • @${user.username}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        RoleBadge(role = user.role)
                    }

                    IconButton(
                        onClick = { userToManage = user },
                        modifier = Modifier.testTag("manage_user_${user.username}")
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = "Manage Permissions")
                    }
                }
            }
        }

        // Recent Audit Logs Section
        item {
            Text(
                text = "Recent Security Audit Logs",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(recentAuditLogs.take(8)) { log ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(modifier = Modifier.size(6.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                            Text(
                                text = log.action,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = log.details,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = dateFormat.format(Date(log.timestamp)),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // User Management Dialog
    userToManage?.let { user ->
        var selectedRole by remember { mutableStateOf(user.role) }
        var isSuspended by remember { mutableStateOf(user.status == UserStatus.SUSPENDED) }

        AlertDialog(
            onDismissRequest = { userToManage = null },
            title = {
                Text("Manage User: ${user.displayName}")
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Assign Role:",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium
                    )

                    UserRole.values().forEach { role ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedRole = role }
                        ) {
                            RadioButton(
                                selected = selectedRole == role,
                                onClick = { selectedRole = role }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(role.displayName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(role.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    HorizontalDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isSuspended) "Account is Suspended" else "Account is Active",
                            fontWeight = FontWeight.Bold,
                            color = if (isSuspended) EduRoseDanger else EduEmeraldSuccess
                        )
                        Switch(
                            checked = isSuspended,
                            onCheckedChange = { isSuspended = it }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (selectedRole != user.role) {
                            onUpdateRole(user.id, selectedRole)
                        }
                        val newStatus = if (isSuspended) UserStatus.SUSPENDED else UserStatus.ACTIVE
                        if (newStatus != user.status) {
                            onUpdateStatus(user.id, newStatus)
                        }
                        userToManage = null
                    },
                    modifier = Modifier.testTag("save_user_mgmt_button")
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { userToManage = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
