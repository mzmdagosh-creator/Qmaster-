package com.example.ui.screens.qbank

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
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
import java.util.UUID

data class EditableOption(
    var id: String = UUID.randomUUID().toString(),
    var text: String = "",
    var isCorrect: Boolean = false,
    var explanation: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionEditorDialog(
    initialQuestion: QuestionWithOptions?,
    currentUser: UserEntity,
    subjects: List<SubjectEntity>,
    onSave: (QuestionEntity, List<QuestionOptionEntity>) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(initialQuestion?.question?.title ?: "") }
    var content by remember { mutableStateOf(initialQuestion?.question?.content ?: "") }
    var selectedType by remember { mutableStateOf(initialQuestion?.question?.type ?: QuestionType.SINGLE_CHOICE) }
    var selectedDifficulty by remember { mutableStateOf(initialQuestion?.question?.difficulty ?: DifficultyLevel.MEDIUM) }
    var selectedSubjectId by remember { mutableStateOf(initialQuestion?.question?.subjectId ?: (subjects.firstOrNull()?.id ?: "subj_cs")) }
    var diagramType by remember { mutableStateOf(initialQuestion?.question?.diagramType ?: "") }
    var caseScenario by remember { mutableStateOf(initialQuestion?.question?.caseScenario ?: "") }
    var explanation by remember { mutableStateOf(initialQuestion?.question?.explanation ?: "") }
    var tags by remember { mutableStateOf(initialQuestion?.question?.tagsJson ?: "") }

    val options = remember {
        mutableStateListOf<EditableOption>().apply {
            if (initialQuestion != null && initialQuestion.options.isNotEmpty()) {
                addAll(initialQuestion.options.map {
                    EditableOption(it.id, it.text, it.isCorrect, it.explanation)
                })
            } else {
                add(EditableOption(text = "Option A", isCorrect = true, explanation = "Correct rationale"))
                add(EditableOption(text = "Option B", isCorrect = false, explanation = "Incorrect distractor"))
                add(EditableOption(text = "Option C", isCorrect = false, explanation = "Incorrect distractor"))
                add(EditableOption(text = "Option D", isCorrect = false, explanation = "Incorrect distractor"))
            }
        }
    }

    var errorMessage by remember { mutableStateOf<String?>(null) }
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
                    text = if (initialQuestion == null) "Create New Question" else "Edit Question",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Question Title / Concept") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("question_title_input"),
                    singleLine = true
                )

                // Content
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Question Stem / Prompt") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .testTag("question_content_input"),
                    maxLines = 4
                )

                // Subject & Difficulty Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Subject Dropdown
                    var subjectExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = subjectExpanded,
                        onExpandedChange = { subjectExpanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = subjects.firstOrNull { it.id == selectedSubjectId }?.name ?: "Subject",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Subject") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectExpanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = subjectExpanded,
                            onDismissRequest = { subjectExpanded = false }
                        ) {
                            subjects.forEach { s ->
                                DropdownMenuItem(
                                    text = { Text(s.name) },
                                    onClick = {
                                        selectedSubjectId = s.id
                                        subjectExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Difficulty
                    var diffExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = diffExpanded,
                        onExpandedChange = { diffExpanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedDifficulty.label,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Difficulty") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = diffExpanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = diffExpanded,
                            onDismissRequest = { diffExpanded = false }
                        ) {
                            DifficultyLevel.values().forEach { d ->
                                DropdownMenuItem(
                                    text = { Text(d.label) },
                                    onClick = {
                                        selectedDifficulty = d
                                        diffExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Question Type Selector
                Text(
                    text = "Question Type:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    QuestionType.values().take(3).forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = {
                                selectedType = type
                                if (type == QuestionType.TRUE_FALSE) {
                                    options.clear()
                                    options.add(EditableOption(text = "True", isCorrect = true))
                                    options.add(EditableOption(text = "False", isCorrect = false))
                                }
                            },
                            label = { Text(type.title, fontSize = 11.sp) }
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    QuestionType.values().drop(3).forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type.title, fontSize = 11.sp) }
                        )
                    }
                }

                // If Diagram Based
                if (selectedType == QuestionType.IMAGE_BASED) {
                    var diagramExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = diagramExpanded,
                        onExpandedChange = { diagramExpanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = if (diagramType.isNotBlank()) diagramType else "Select Visual Diagram",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Attached Diagram / Asset") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = diagramExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = diagramExpanded,
                            onDismissRequest = { diagramExpanded = false }
                        ) {
                            listOf("BINARY_TREE", "SYNAPSE", "LOGIC_GATE").forEach { d ->
                                DropdownMenuItem(
                                    text = { Text(d) },
                                    onClick = {
                                        diagramType = d
                                        diagramExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // If Case Based
                if (selectedType == QuestionType.CASE_BASED) {
                    OutlinedTextField(
                        value = caseScenario,
                        onValueChange = { caseScenario = it },
                        label = { Text("Case Vignette / Clinical or Engineering Scenario") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        maxLines = 3
                    )
                }

                // Options list
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Answer Options & Correctness:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (selectedType != QuestionType.TRUE_FALSE) {
                        TextButton(onClick = {
                            options.add(EditableOption(text = "Option ${('A' + options.size)}", isCorrect = false))
                        }) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Option")
                        }
                    }
                }

                options.forEachIndexed { index, opt ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Checkbox(
                                    checked = opt.isCorrect,
                                    onCheckedChange = { checked ->
                                        if (selectedType == QuestionType.SINGLE_CHOICE || selectedType == QuestionType.TRUE_FALSE) {
                                            options.indices.forEach { i ->
                                                options[i] = options[i].copy(isCorrect = (i == index && checked))
                                            }
                                        } else {
                                            options[index] = opt.copy(isCorrect = checked)
                                        }
                                    }
                                )
                                OutlinedTextField(
                                    value = opt.text,
                                    onValueChange = { newText ->
                                        options[index] = opt.copy(text = newText)
                                    },
                                    placeholder = { Text("Option text") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("option_text_input_$index"),
                                    singleLine = true
                                )
                                if (options.size > 2 && selectedType != QuestionType.TRUE_FALSE) {
                                    IconButton(onClick = { options.removeAt(index) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete option", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = opt.explanation,
                                onValueChange = { newExp ->
                                    options[index] = opt.copy(explanation = newExp)
                                },
                                placeholder = { Text("Rationale for this option (optional)") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp),
                                singleLine = true
                            )
                        }
                    }
                }

                // Overall Explanation
                OutlinedTextField(
                    value = explanation,
                    onValueChange = { explanation = it },
                    label = { Text("Comprehensive Academic Explanation / Solution") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .testTag("question_explanation_input"),
                    maxLines = 3
                )

                // Tags
                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("Tags (comma-separated, e.g. Algorithms, Big-O)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("question_tags_input"),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank() || content.isBlank()) {
                        errorMessage = "Title and Content cannot be empty."
                        return@Button
                    }
                    if (options.none { it.isCorrect }) {
                        errorMessage = "Please mark at least one correct option."
                        return@Button
                    }

                    val questionId = initialQuestion?.question?.id ?: "q_${UUID.randomUUID().toString().take(8)}"
                    val qEntity = QuestionEntity(
                        id = questionId,
                        subjectId = selectedSubjectId,
                        title = title.trim(),
                        content = content.trim(),
                        type = selectedType,
                        difficulty = selectedDifficulty,
                        explanation = explanation.trim(),
                        diagramType = if (selectedType == QuestionType.IMAGE_BASED && diagramType.isNotBlank()) diagramType else null,
                        caseScenario = if (selectedType == QuestionType.CASE_BASED && caseScenario.isNotBlank()) caseScenario else null,
                        tagsJson = tags.trim(),
                        authorUserId = initialQuestion?.question?.authorUserId ?: currentUser.id,
                        authorName = initialQuestion?.question?.authorName ?: currentUser.displayName
                    )

                    val optEntities = options.mapIndexed { idx, opt ->
                        QuestionOptionEntity(
                            id = if (opt.id.startsWith("opt_")) opt.id else "opt_${UUID.randomUUID().toString().take(8)}",
                            questionId = questionId,
                            text = opt.text.trim(),
                            isCorrect = opt.isCorrect,
                            explanation = opt.explanation.trim(),
                            orderIndex = idx
                        )
                    }

                    onSave(qEntity, optEntities)
                    onDismiss()
                },
                modifier = Modifier.testTag("save_question_button")
            ) {
                Text(if (currentUser.role == UserRole.MEMBER) "Submit for Review" else "Save Question")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
