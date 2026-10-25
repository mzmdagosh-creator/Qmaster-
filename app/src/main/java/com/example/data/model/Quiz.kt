package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class QuizMode(val label: String, val description: String) {
    PRACTICE("Practice Mode", "Instant feedback, immediate explanations, and retry option"),
    EXAMINATION("Examination Mode", "Timed assessment, question palette, and comprehensive final review")
}

@Entity(
    tableName = "quiz_attempts",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"])]
)
data class QuizAttemptEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val title: String,
    val subjectId: String = "",
    val subjectName: String = "",
    val mode: QuizMode,
    val totalQuestions: Int,
    val correctCount: Int,
    val timeSpentSeconds: Long,
    val scorePercentage: Float,
    val completedAt: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "quiz_answers",
    foreignKeys = [
        ForeignKey(
            entity = QuizAttemptEntity::class,
            parentColumns = ["id"],
            childColumns = ["quizAttemptId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["quizAttemptId"]),
        Index(value = ["questionId"])
    ]
)
data class QuizAnswerEntity(
    @PrimaryKey
    val id: String,
    val quizAttemptId: String,
    val questionId: String,
    val selectedOptionIdsJson: String, // comma-separated or json array
    val isCorrect: Boolean,
    val timeSpentSeconds: Int = 0
)
