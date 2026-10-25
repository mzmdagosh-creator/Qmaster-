package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.security.Permission
import com.example.security.SecurityGuard
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class QuestionRepository(private val database: AppDatabase) {
    private val questionDao = database.questionDao()
    private val moderationDao = database.moderationDao()
    private val auditDao = database.auditDao()

    fun searchQuestions(
        query: String = "",
        subjectId: String? = null,
        difficulty: DifficultyLevel? = null,
        type: QuestionType? = null,
        status: QuestionStatus? = null
    ): Flow<List<QuestionWithOptions>> {
        return questionDao.searchQuestions(query, subjectId, difficulty, type, status)
    }

    fun observeAllQuestions(): Flow<List<QuestionWithOptions>> =
        questionDao.observeAllQuestionsWithOptions()

    fun observeApprovedQuestions(): Flow<List<QuestionWithOptions>> =
        questionDao.observeApprovedQuestionsWithOptions()

    suspend fun getQuestionWithOptions(id: String): QuestionWithOptions? =
        questionDao.getQuestionWithOptionsById(id)

    suspend fun saveQuestion(
        actor: UserEntity,
        question: QuestionEntity,
        options: List<QuestionOptionEntity>
    ) {
        SecurityGuard.assertPermission(actor, Permission.CREATE_OR_SUBMIT_QUESTION, question.authorUserId)

        val isDirectApproval = actor.role == UserRole.ADMIN || actor.role == UserRole.MODERATOR
        val statusToSet = if (isDirectApproval) QuestionStatus.APPROVED else QuestionStatus.PENDING_REVIEW

        val savedQuestion = question.copy(
            status = statusToSet,
            updatedAt = System.currentTimeMillis()
        )

        questionDao.insertQuestion(savedQuestion)
        // Refresh options
        questionDao.deleteOptionsForQuestion(savedQuestion.id)
        questionDao.insertOptions(options.map { it.copy(questionId = savedQuestion.id) })

        if (!isDirectApproval) {
            // Queue for moderation
            moderationDao.insertTask(
                ModerationTaskEntity(
                    id = "mod_${UUID.randomUUID().toString().take(8)}",
                    questionId = savedQuestion.id,
                    questionTitle = savedQuestion.title,
                    submittedByUserId = actor.id,
                    submittedByName = actor.displayName,
                    action = ModerationAction.SUBMITTED,
                    feedbackNotes = "Submitted by user ${actor.username}"
                )
            )
        }

        auditDao.insertLog(
            AuditLogEntity(
                id = UUID.randomUUID().toString(),
                userId = actor.id,
                userRole = actor.role.name,
                action = if (isDirectApproval) "QUESTION_SAVED" else "QUESTION_SUBMITTED",
                entityType = "QUESTION",
                entityId = savedQuestion.id,
                details = "Title: ${savedQuestion.title}, Status: ${savedQuestion.status.name}"
            )
        )
    }

    suspend fun duplicateQuestion(actor: UserEntity, sourceId: String): String {
        SecurityGuard.assertPermission(actor, Permission.CREATE_OR_SUBMIT_QUESTION)
        val source = questionDao.getQuestionWithOptionsById(sourceId)
            ?: throw IllegalArgumentException("Question not found")

        val newId = "q_${UUID.randomUUID().toString().take(8)}"
        val dupQuestion = source.question.copy(
            id = newId,
            title = "${source.question.title} (Copy)",
            authorUserId = actor.id,
            authorName = actor.displayName,
            status = if (actor.role == UserRole.ADMIN || actor.role == UserRole.MODERATOR) QuestionStatus.APPROVED else QuestionStatus.PENDING_REVIEW,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val dupOptions = source.options.map {
            it.copy(
                id = "opt_${UUID.randomUUID().toString().take(8)}",
                questionId = newId
            )
        }
        questionDao.insertQuestion(dupQuestion)
        questionDao.insertOptions(dupOptions)

        auditDao.insertLog(
            AuditLogEntity(
                id = UUID.randomUUID().toString(),
                userId = actor.id,
                userRole = actor.role.name,
                action = "QUESTION_DUPLICATED",
                entityType = "QUESTION",
                entityId = newId,
                details = "Duplicated from $sourceId"
            )
        )
        return newId
    }

    suspend fun archiveQuestion(actor: UserEntity, questionId: String) {
        val q = questionDao.getQuestionWithOptionsById(questionId)
            ?: throw IllegalArgumentException("Question not found")
        SecurityGuard.assertPermission(actor, Permission.EDIT_ANY_QUESTION, q.question.authorUserId)

        questionDao.updateQuestionStatus(questionId, QuestionStatus.ARCHIVED, "Archived by ${actor.username}")
        auditDao.insertLog(
            AuditLogEntity(
                id = UUID.randomUUID().toString(),
                userId = actor.id,
                userRole = actor.role.name,
                action = "QUESTION_ARCHIVED",
                entityType = "QUESTION",
                entityId = questionId,
                details = "Archived question $questionId"
            )
        )
    }

    suspend fun deleteQuestion(actor: UserEntity, questionId: String) {
        val q = questionDao.getQuestionWithOptionsById(questionId)
            ?: throw IllegalArgumentException("Question not found")
        SecurityGuard.assertPermission(actor, Permission.DELETE_ANY_QUESTION, q.question.authorUserId)

        questionDao.deleteOptionsForQuestion(questionId)
        questionDao.deleteQuestionById(questionId)
        auditDao.insertLog(
            AuditLogEntity(
                id = UUID.randomUUID().toString(),
                userId = actor.id,
                userRole = actor.role.name,
                action = "QUESTION_DELETED",
                entityType = "QUESTION",
                entityId = questionId,
                details = "Deleted question: ${q.question.title}"
            )
        )
    }

    fun countTotalQuestions(): Flow<Int> = questionDao.countTotalQuestions()
    fun countApprovedQuestions(): Flow<Int> = questionDao.countApprovedQuestions()
    fun countPendingQuestions(): Flow<Int> = questionDao.countPendingQuestions()

    suspend fun exportQuestionsToJson(): String {
        val questions = questionDao.getApprovedQuestionsWithOptions()
        val root = JSONObject()
        val array = JSONArray()

        for (item in questions) {
            val q = item.question
            val qObj = JSONObject()
            qObj.put("id", q.id)
            qObj.put("subjectId", q.subjectId)
            qObj.put("title", q.title)
            qObj.put("content", q.content)
            qObj.put("type", q.type.name)
            qObj.put("difficulty", q.difficulty.name)
            qObj.put("explanation", q.explanation)
            qObj.put("tags", q.tagsJson)
            qObj.put("diagramType", q.diagramType ?: "")
            qObj.put("caseScenario", q.caseScenario ?: "")

            val optArray = JSONArray()
            for (opt in item.options) {
                val optObj = JSONObject()
                optObj.put("text", opt.text)
                optObj.put("isCorrect", opt.isCorrect)
                optObj.put("explanation", opt.explanation)
                optArray.put(optObj)
            }
            qObj.put("options", optArray)
            array.put(qObj)
        }

        root.put("version", "1.0")
        root.put("platform", "EduBank MCQ")
        root.put("exportedAt", System.currentTimeMillis())
        root.put("questions", array)
        return root.toString(2)
    }

    suspend fun importQuestionsFromJson(actor: UserEntity, jsonString: String): Int {
        SecurityGuard.assertPermission(actor, Permission.IMPORT_DATA)
        val root = JSONObject(jsonString)
        val array = root.getJSONArray("questions")
        var count = 0

        for (i in 0 until array.length()) {
            val qObj = array.getJSONObject(i)
            val newId = "q_imp_${UUID.randomUUID().toString().take(6)}"
            val type = runCatching { QuestionType.valueOf(qObj.getString("type")) }.getOrDefault(QuestionType.SINGLE_CHOICE)
            val diff = runCatching { DifficultyLevel.valueOf(qObj.getString("difficulty")) }.getOrDefault(DifficultyLevel.MEDIUM)

            val question = QuestionEntity(
                id = newId,
                subjectId = qObj.optString("subjectId", "subj_cs"),
                title = qObj.getString("title"),
                content = qObj.getString("content"),
                type = type,
                difficulty = diff,
                status = QuestionStatus.APPROVED,
                explanation = qObj.optString("explanation", ""),
                diagramType = qObj.optString("diagramType").ifBlank { null },
                caseScenario = qObj.optString("caseScenario").ifBlank { null },
                tagsJson = qObj.optString("tags", ""),
                authorUserId = actor.id,
                authorName = actor.displayName
            )

            val optionsList = mutableListOf<QuestionOptionEntity>()
            val optArray = qObj.optJSONArray("options") ?: JSONArray()
            for (j in 0 until optArray.length()) {
                val optObj = optArray.getJSONObject(j)
                optionsList.add(
                    QuestionOptionEntity(
                        id = "opt_${UUID.randomUUID().toString().take(8)}",
                        questionId = newId,
                        text = optObj.getString("text"),
                        isCorrect = optObj.getBoolean("isCorrect"),
                        explanation = optObj.optString("explanation", ""),
                        orderIndex = j
                    )
                )
            }

            questionDao.insertQuestion(question)
            questionDao.insertOptions(optionsList)
            count++
        }

        auditDao.insertLog(
            AuditLogEntity(
                id = UUID.randomUUID().toString(),
                userId = actor.id,
                userRole = actor.role.name,
                action = "QUESTIONS_IMPORTED",
                entityType = "QUESTION",
                entityId = "BULK",
                details = "Imported $count questions from JSON"
            )
        )
        return count
    }
}
