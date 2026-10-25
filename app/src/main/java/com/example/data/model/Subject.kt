package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val code: String,
    val description: String,
    val iconName: String,
    val colorHex: String,
    val chapterCount: Int = 0,
    val questionCount: Int = 0
)

@Entity(
    tableName = "chapters",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["subjectId"])]
)
data class ChapterEntity(
    @PrimaryKey
    val id: String,
    val subjectId: String,
    val name: String,
    val orderIndex: Int = 0,
    val description: String = ""
)

@Entity(
    tableName = "topics",
    foreignKeys = [
        ForeignKey(
            entity = ChapterEntity::class,
            parentColumns = ["id"],
            childColumns = ["chapterId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["chapterId"])]
)
data class TopicEntity(
    @PrimaryKey
    val id: String,
    val chapterId: String,
    val name: String,
    val description: String = ""
)
