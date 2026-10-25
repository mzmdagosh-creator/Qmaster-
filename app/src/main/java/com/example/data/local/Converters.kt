package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.DifficultyLevel
import com.example.data.model.ModerationAction
import com.example.data.model.QuestionStatus
import com.example.data.model.QuestionType
import com.example.data.model.QuizMode
import com.example.data.model.UserRole
import com.example.data.model.UserStatus

class Converters {
    @TypeConverter
    fun fromUserRole(value: UserRole): String = value.name

    @TypeConverter
    fun toUserRole(value: String): UserRole = runCatching { UserRole.valueOf(value) }.getOrDefault(UserRole.MEMBER)

    @TypeConverter
    fun fromUserStatus(value: UserStatus): String = value.name

    @TypeConverter
    fun toUserStatus(value: String): UserStatus = runCatching { UserStatus.valueOf(value) }.getOrDefault(UserStatus.ACTIVE)

    @TypeConverter
    fun fromQuestionType(value: QuestionType): String = value.name

    @TypeConverter
    fun toQuestionType(value: String): QuestionType = runCatching { QuestionType.valueOf(value) }.getOrDefault(QuestionType.SINGLE_CHOICE)

    @TypeConverter
    fun fromDifficulty(value: DifficultyLevel): String = value.name

    @TypeConverter
    fun toDifficulty(value: String): DifficultyLevel = runCatching { DifficultyLevel.valueOf(value) }.getOrDefault(DifficultyLevel.MEDIUM)

    @TypeConverter
    fun fromQuestionStatus(value: QuestionStatus): String = value.name

    @TypeConverter
    fun toQuestionStatus(value: String): QuestionStatus = runCatching { QuestionStatus.valueOf(value) }.getOrDefault(QuestionStatus.APPROVED)

    @TypeConverter
    fun fromQuizMode(value: QuizMode): String = value.name

    @TypeConverter
    fun toQuizMode(value: String): QuizMode = runCatching { QuizMode.valueOf(value) }.getOrDefault(QuizMode.PRACTICE)

    @TypeConverter
    fun fromModerationAction(value: ModerationAction): String = value.name

    @TypeConverter
    fun toModerationAction(value: String): ModerationAction = runCatching { ModerationAction.valueOf(value) }.getOrDefault(ModerationAction.SUBMITTED)
}
