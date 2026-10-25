package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class ModerationAction(val label: String) {
    SUBMITTED("Submitted for Review"),
    APPROVED("Approved"),
    REJECTED("Rejected"),
    REVISION_REQUESTED("Revision Requested")
}

@Entity(
    tableName = "moderation_tasks",
    foreignKeys = [
        ForeignKey(
            entity = QuestionEntity::class,
            parentColumns = ["id"],
            childColumns = ["questionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["questionId"]),
        Index(value = ["action"])
    ]
)
data class ModerationTaskEntity(
    @PrimaryKey
    val id: String,
    val questionId: String,
    val questionTitle: String,
    val submittedByUserId: String,
    val submittedByName: String,
    val reviewerUserId: String? = null,
    val reviewerName: String? = null,
    val action: ModerationAction,
    val feedbackNotes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
