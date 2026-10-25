package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "bookmarks",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = QuestionEntity::class,
            parentColumns = ["id"],
            childColumns = ["questionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userId", "questionId"], unique = true),
        Index(value = ["questionId"])
    ]
)
data class BookmarkEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val questionId: String,
    val collectionName: String = "Default",
    val notes: String = "",
    val bookmarkedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "collections",
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
data class CollectionEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val name: String,
    val description: String = "",
    val isCuratedByAdvisor: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
