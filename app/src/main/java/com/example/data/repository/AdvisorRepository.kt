package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.AdvisorRecommendationEntity
import com.example.data.model.UserEntity
import com.example.security.Permission
import com.example.security.SecurityGuard
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class AdvisorRepository(private val database: AppDatabase) {
    private val advisorDao = database.advisorDao()
    private val auditDao = database.auditDao()

    fun observeRecommendationsForUser(userId: String): Flow<List<AdvisorRecommendationEntity>> =
        advisorDao.observeRecommendationsForUser(userId)

    fun observeAllRecommendations(): Flow<List<AdvisorRecommendationEntity>> =
        advisorDao.observeAllRecommendations()

    suspend fun createRecommendation(
        advisor: UserEntity,
        targetUserId: String,
        targetUserName: String,
        subjectName: String,
        topicName: String,
        title: String,
        note: String,
        priority: String
    ) {
        SecurityGuard.assertPermission(advisor, Permission.CREATE_RECOMMENDATIONS)

        val entity = AdvisorRecommendationEntity(
            id = "rec_${UUID.randomUUID().toString().take(8)}",
            advisorUserId = advisor.id,
            advisorName = advisor.displayName,
            targetUserId = targetUserId,
            targetUserName = targetUserName,
            subjectName = subjectName,
            topicName = topicName,
            title = title,
            note = note,
            priority = priority
        )
        advisorDao.insertRecommendation(entity)

        auditDao.insertLog(
            com.example.data.model.AuditLogEntity(
                id = UUID.randomUUID().toString(),
                userId = advisor.id,
                userRole = advisor.role.name,
                action = "ADVISOR_RECOMMENDATION_SENT",
                entityType = "RECOMMENDATION",
                entityId = entity.id,
                details = "To $targetUserName on $topicName"
            )
        )
    }

    suspend fun markRecommendationComplete(id: String) {
        advisorDao.markCompleted(id)
    }
}
