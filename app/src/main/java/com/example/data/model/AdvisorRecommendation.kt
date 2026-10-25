package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "advisor_recommendations",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["targetUserId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["targetUserId"]),
        Index(value = ["advisorUserId"])
    ]
)
data class AdvisorRecommendationEntity(
    @PrimaryKey
    val id: String,
    val advisorUserId: String,
    val advisorName: String,
    val targetUserId: String,
    val targetUserName: String,
    val subjectName: String,
    val topicName: String,
    val title: String,
    val note: String,
    val priority: String = "HIGH", // HIGH, MEDIUM, LOW
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
