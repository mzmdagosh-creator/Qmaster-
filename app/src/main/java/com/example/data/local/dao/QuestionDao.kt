package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.DifficultyLevel
import com.example.data.model.QuestionEntity
import com.example.data.model.QuestionOptionEntity
import com.example.data.model.QuestionStatus
import com.example.data.model.QuestionType
import com.example.data.model.QuestionWithOptions
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: QuestionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<QuestionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOptions(options: List<QuestionOptionEntity>)

    @Update
    suspend fun updateQuestion(question: QuestionEntity)

    @Delete
    suspend fun deleteQuestion(question: QuestionEntity)

    @Query("DELETE FROM questions WHERE id = :questionId")
    suspend fun deleteQuestionById(questionId: String)

    @Query("DELETE FROM question_options WHERE questionId = :questionId")
    suspend fun deleteOptionsForQuestion(questionId: String)

    @Transaction
    @Query("SELECT * FROM questions WHERE id = :id LIMIT 1")
    suspend fun getQuestionWithOptionsById(id: String): QuestionWithOptions?

    @Transaction
    @Query("SELECT * FROM questions ORDER BY createdAt DESC")
    fun observeAllQuestionsWithOptions(): Flow<List<QuestionWithOptions>>

    @Transaction
    @Query("SELECT * FROM questions WHERE status = 'APPROVED' ORDER BY createdAt DESC")
    fun observeApprovedQuestionsWithOptions(): Flow<List<QuestionWithOptions>>

    @Transaction
    @Query("SELECT * FROM questions WHERE status = 'APPROVED' ORDER BY createdAt DESC")
    suspend fun getApprovedQuestionsWithOptions(): List<QuestionWithOptions>

    @Transaction
    @Query("""
        SELECT * FROM questions 
        WHERE (:subjectId IS NULL OR subjectId = :subjectId)
        AND (:difficulty IS NULL OR difficulty = :difficulty)
        AND (:type IS NULL OR type = :type)
        AND (:status IS NULL OR status = :status)
        AND (:query = '' OR title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' OR tagsJson LIKE '%' || :query || '%')
        ORDER BY createdAt DESC
    """)
    fun searchQuestions(
        query: String,
        subjectId: String?,
        difficulty: DifficultyLevel?,
        type: QuestionType?,
        status: QuestionStatus?
    ): Flow<List<QuestionWithOptions>>

    @Transaction
    @Query("""
        SELECT * FROM questions 
        WHERE status = 'APPROVED'
        AND (:subjectId = '' OR subjectId = :subjectId)
        AND (:difficulty IS NULL OR difficulty = :difficulty)
        ORDER BY RANDOM() 
        LIMIT :limit
    """)
    suspend fun getRandomQuizQuestions(
        subjectId: String,
        difficulty: DifficultyLevel?,
        limit: Int
    ): List<QuestionWithOptions>

    @Query("UPDATE questions SET status = :newStatus, reviewerNotes = :notes, updatedAt = :updatedAt WHERE id = :questionId")
    suspend fun updateQuestionStatus(questionId: String, newStatus: QuestionStatus, notes: String?, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM questions")
    fun countTotalQuestions(): Flow<Int>

    @Query("SELECT COUNT(*) FROM questions WHERE status = 'APPROVED'")
    fun countApprovedQuestions(): Flow<Int>

    @Query("SELECT COUNT(*) FROM questions WHERE status = 'PENDING_REVIEW'")
    fun countPendingQuestions(): Flow<Int>
}
