package com.example.ui.screens.qbank

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.ui.theme.EduEmeraldSuccess

@Composable
fun ImportExportDialog(
    currentUser: UserEntity,
    exportedJson: String,
    onImportJson: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Export, 1: Import
    var importInput by remember { mutableStateOf("") }
    var copiedNotice by remember { mutableStateOf(false) }
    var importStatusMessage by remember { mutableStateOf<String?>(null) }
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Question Data Exchange",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Export JSON") },
                        icon = { Icon(Icons.Default.Download, null, modifier = Modifier.size(16.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Import JSON") },
                        icon = { Icon(Icons.Default.Upload, null, modifier = Modifier.size(16.dp)) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (selectedTab == 0) {
                    // Export Tab
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(scrollState)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Standard Question Bank JSON:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Button(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(exportedJson))
                                    copiedNotice = true
                                },
                                modifier = Modifier.testTag("copy_json_export_button"),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (copiedNotice) "Copied!" else "Copy JSON", fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = exportedJson,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    // Import Tab
                    val canImport = currentUser.role == UserRole.ADMIN || currentUser.role == UserRole.MODERATOR

                    if (!canImport) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "Permission Restricted: Only Administrators and Content Moderators have privilege to import bulk question data.",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontSize = 12.sp
                            )
                        }
                    } else {
                        Column {
                            Text(
                                text = "Paste Valid Question Bank JSON:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = importInput,
                                onValueChange = {
                                    importInput = it
                                    importStatusMessage = null
                                },
                                placeholder = { Text("Paste JSON array or payload here...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .testTag("import_json_input"),
                                textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                                maxLines = 10
                            )

                            if (importStatusMessage != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = importStatusMessage ?: "",
                                    color = if (importStatusMessage?.startsWith("Success") == true) EduEmeraldSuccess else MaterialTheme.colorScheme.error,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (selectedTab == 1 && (currentUser.role == UserRole.ADMIN || currentUser.role == UserRole.MODERATOR)) {
                Button(
                    onClick = {
                        if (importInput.isBlank()) {
                            importStatusMessage = "Please enter JSON data to import."
                            return@Button
                        }
                        try {
                            onImportJson(importInput)
                            importStatusMessage = "Successfully submitted import."
                        } catch (e: Exception) {
                            importStatusMessage = "Import failed: ${e.message}"
                        }
                    },
                    modifier = Modifier.testTag("confirm_import_button")
                ) {
                    Text("Import Questions")
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("Done")
                }
            }
        },
        dismissButton = {
            if (selectedTab == 1) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}
