package com.example.ui.screens.qbank

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.QuestionCard
import com.example.ui.theme.EduEmeraldSuccess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionBankScreen(
    questions: List<QuestionWithOptions>,
    subjects: List<SubjectEntity>,
    currentUser: UserEntity,
    bookmarkedIds: Set<String>,
    onBookmarkToggle: (String) -> Unit,
    onSaveQuestion: (QuestionEntity, List<QuestionOptionEntity>) -> Unit,
    onDuplicateQuestion: (String) -> Unit,
    onArchiveQuestion: (String) -> Unit,
    onDeleteQuestion: (String) -> Unit,
    onExportJson: suspend () -> String,
    onImportJson: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedSubjectId by remember { mutableStateOf<String?>(null) }
    var selectedDifficulty by remember { mutableStateOf<DifficultyLevel?>(null) }
    var selectedType by remember { mutableStateOf<QuestionType?>(null) }
    var selectedStatus by remember { mutableStateOf<QuestionStatus?>(null) }

    var editingQuestion by remember { mutableStateOf<QuestionWithOptions?>(null) }
    var isCreatingNew by remember { mutableStateOf(false) }
    var viewingQuestionDetail by remember { mutableStateOf<QuestionWithOptions?>(null) }
    var showImportExport by remember { mutableStateOf(false) }
    var exportedJsonContent by remember { mutableStateOf("") }
    var showDeleteConfirmId by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()

    // Client-side quick filter
    val filteredQuestions = remember(questions, searchQuery, selectedSubjectId, selectedDifficulty, selectedType, selectedStatus) {
        questions.filter { item ->
            val q = item.question
            val matchesSearch = searchQuery.isBlank() ||
                q.title.contains(searchQuery, ignoreCase = true) ||
                q.content.contains(searchQuery, ignoreCase = true) ||
                q.tagsJson.contains(searchQuery, ignoreCase = true)

            val matchesSubject = selectedSubjectId == null || q.subjectId == selectedSubjectId
            val matchesDifficulty = selectedDifficulty == null || q.difficulty == selectedDifficulty
            val matchesType = selectedType == null || q.type == selectedType
            val matchesStatus = selectedStatus == null || q.status == selectedStatus

            matchesSearch && matchesSubject && matchesDifficulty && matchesType && matchesStatus
        }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Question Bank",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${filteredQuestions.size} questions available",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = {
                                kotlinx.coroutines.GlobalScope.let {
                                    // Trigger async export fetch
                                }
                                showImportExport = true
                            },
                            modifier = Modifier.testTag("open_import_export_button")
                        ) {
                            Icon(Icons.Default.ImportExport, contentDescription = "Import or Export JSON")
                        }

                        Button(
                            onClick = { isCreatingNew = true },
                            modifier = Modifier.testTag("create_question_button"),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (currentUser.role == UserRole.MEMBER) "Submit MCQ" else "New MCQ")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by concept, keywords, or tags...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear search")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_questions_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Filter Chips Row
                val filterScroll = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(filterScroll),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // All / Reset
                    FilterChip(
                        selected = selectedSubjectId == null && selectedDifficulty == null && selectedType == null && selectedStatus == null,
                        onClick = {
                            selectedSubjectId = null
                            selectedDifficulty = null
                            selectedType = null
                            selectedStatus = null
                        },
                        label = { Text("All") },
                        modifier = Modifier.testTag("filter_all")
                    )

                    // Subjects
                    subjects.forEach { s ->
                        FilterChip(
                            selected = selectedSubjectId == s.id,
                            onClick = {
                                selectedSubjectId = if (selectedSubjectId == s.id) null else s.id
                            },
                            label = { Text(s.code) },
                            modifier = Modifier.testTag("filter_subject_${s.code}")
                        )
                    }

                    // Difficulty
                    DifficultyLevel.values().forEach { d ->
                        FilterChip(
                            selected = selectedDifficulty == d,
                            onClick = {
                                selectedDifficulty = if (selectedDifficulty == d) null else d
                            },
                            label = { Text(d.label) },
                            modifier = Modifier.testTag("filter_diff_${d.name}")
                        )
                    }

                    // Question Type
                    QuestionType.values().forEach { t ->
                        FilterChip(
                            selected = selectedType == t,
                            onClick = {
                                selectedType = if (selectedType == t) null else t
                            },
                            label = { Text(t.title) },
                            modifier = Modifier.testTag("filter_type_${t.name}")
                        )
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        if (filteredQuestions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SearchOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "No questions match your filter",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Try clearing filters, searching for a different topic, or authoring a new MCQ.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(onClick = {
                        searchQuery = ""
                        selectedSubjectId = null
                        selectedDifficulty = null
                        selectedType = null
                        selectedStatus = null
                    }) {
                        Text("Reset Filters")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(filteredQuestions, key = { it.question.id }) { item ->
                    QuestionCard(
                        questionWithOptions = item,
                        currentUser = currentUser,
                        isBookmarked = bookmarkedIds.contains(item.question.id),
                        onBookmarkToggle = { onBookmarkToggle(item.question.id) },
                        onClick = { viewingQuestionDetail = item },
                        onEdit = { editingQuestion = item },
                        onDuplicate = { onDuplicateQuestion(item.question.id) },
                        onArchive = { onArchiveQuestion(item.question.id) },
                        onDelete = { showDeleteConfirmId = item.question.id }
                    )
                }
            }
        }
    }

    // Detail Dialog
    viewingQuestionDetail?.let { q ->
        QuestionDetailDialog(
            questionWithOptions = q,
            onDismiss = { viewingQuestionDetail = null }
        )
    }

    // Edit or Create Dialog
    if (isCreatingNew || editingQuestion != null) {
        QuestionEditorDialog(
            initialQuestion = editingQuestion,
            currentUser = currentUser,
            subjects = subjects,
            onSave = { q, opts ->
                onSaveQuestion(q, opts)
                isCreatingNew = false
                editingQuestion = null
            },
            onDismiss = {
                isCreatingNew = false
                editingQuestion = null
            }
        )
    }

    // Import/Export Dialog
    if (showImportExport) {
        LaunchedEffect(Unit) {
            exportedJsonContent = onExportJson()
        }
        ImportExportDialog(
            currentUser = currentUser,
            exportedJson = exportedJsonContent,
            onImportJson = { json ->
                onImportJson(json)
                showImportExport = false
            },
            onDismiss = { showImportExport = false }
        )
    }

    // Delete Confirmation
    showDeleteConfirmId?.let { delId ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirmId = null },
            title = { Text("Confirm Delete") },
            text = { Text("Are you sure you want to permanently remove this question from the question bank?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteQuestion(delId)
                        showDeleteConfirmId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmId = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
