package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.security.Permission
import com.example.security.SecurityGuard
import kotlinx.coroutines.flow.Flow
import java.util.UUID

data class ModerationStats(
    val pendingCount: Int,
    val approvedCount: Int,
    val rejectedCount: Int
)

class ModerationRepository(private val database: AppDatabase) {
    private val moderationDao = database.moderationDao()
    private val questionDao = database.questionDao()
    private val auditDao = database.auditDao()

    fun observePendingTasks(): Flow<List<ModerationTaskEntity>> =
        moderationDao.observePendingTasks()

    fun observeRecentTasks(): Flow<List<ModerationTaskEntity>> =
        moderationDao.observeRecentTasks()

    fun countPendingTasks(): Flow<Int> = moderationDao.countPendingTasks()
    fun countApprovedTasks(): Flow<Int> = moderationDao.countApprovedTasks()
    fun countRejectedTasks(): Flow<Int> = moderationDao.countRejectedTasks()

    suspend fun resolveTask(
        moderator: UserEntity,
        questionId: String,
        action: ModerationAction,
        feedbackNotes: String
    ) {
        SecurityGuard.assertPermission(moderator, Permission.MODERATE_QUESTIONS)

        val targetStatus = when (action) {
            ModerationAction.APPROVED -> QuestionStatus.APPROVED
            ModerationAction.REJECTED -> QuestionStatus.REJECTED
            ModerationAction.REVISION_REQUESTED -> QuestionStatus.REVISION_REQUESTED
            ModerationAction.SUBMITTED -> QuestionStatus.PENDING_REVIEW
        }

        questionDao.updateQuestionStatus(questionId, targetStatus, feedbackNotes)
        moderationDao.resolveTask(questionId, action, moderator.id, moderator.displayName, feedbackNotes)

        auditDao.insertLog(
            AuditLogEntity(
                id = UUID.randomUUID().toString(),
                userId = moderator.id,
                userRole = moderator.role.name,
                action = "MODERATION_${action.name}",
                entityType = "QUESTION",
                entityId = questionId,
                details = "Decision: ${action.label}. Notes: $feedbackNotes"
            )
        )
    }
}
