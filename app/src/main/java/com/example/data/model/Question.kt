package com.example.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

enum class QuestionType(val title: String) {
    SINGLE_CHOICE("Single Choice"),
    MULTI_CHOICE("Multiple Choice"),
    TRUE_FALSE("True / False"),
    CASE_BASED("Case-Based"),
    IMAGE_BASED("Diagram / Image")
}

enum class DifficultyLevel(val label: String, val points: Int) {
    EASY("Easy", 1),
    MEDIUM("Medium", 2),
    HARD("Hard", 3)
}

enum class QuestionStatus(val label: String) {
    APPROVED("Approved"),
    PENDING_REVIEW("Pending Review"),
    REJECTED("Rejected"),
    REVISION_REQUESTED("Revision Requested"),
    ARCHIVED("Archived")
}

@Entity(
    tableName = "questions",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["subjectId"]),
        Index(value = ["status"]),
        Index(value = ["difficulty"]),
        Index(value = ["type"])
    ]
)
data class QuestionEntity(
    @PrimaryKey
    val id: String,
    val subjectId: String,
    val chapterId: String = "",
    val topicId: String = "",
    val title: String,
    val content: String,
    val type: QuestionType,
    val difficulty: DifficultyLevel,
    val status: QuestionStatus = QuestionStatus.APPROVED,
    val explanation: String = "",
    val caseScenario: String? = null,
    val diagramType: String? = null, // e.g. "SYNAPSE", "BINARY_TREE", "LOGIC_GATE"
    val tagsJson: String = "",       // Comma-separated or JSON array string
    val authorUserId: String,
    val authorName: String = "",
    val reviewerNotes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "question_options",
    foreignKeys = [
        ForeignKey(
            entity = QuestionEntity::class,
            parentColumns = ["id"],
            childColumns = ["questionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["questionId"])]
)
data class QuestionOptionEntity(
    @PrimaryKey
    val id: String,
    val questionId: String,
    val text: String,
    val isCorrect: Boolean,
    val explanation: String = "",
    val orderIndex: Int = 0
)

data class QuestionWithOptions(
    @Embedded
    val question: QuestionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "questionId"
    )
    val options: List<QuestionOptionEntity>
)
