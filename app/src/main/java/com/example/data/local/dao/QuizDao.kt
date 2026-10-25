package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.QuizAnswerEntity
import com.example.data.model.QuizAttemptEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempt(attempt: QuizAttemptEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnswers(answers: List<QuizAnswerEntity>)

    @Query("SELECT * FROM quiz_attempts WHERE userId = :userId ORDER BY completedAt DESC")
    fun observeAttemptsForUser(userId: String): Flow<List<QuizAttemptEntity>>

    @Query("SELECT * FROM quiz_attempts WHERE id = :attemptId LIMIT 1")
    suspend fun getAttemptById(attemptId: String): QuizAttemptEntity?

    @Query("SELECT * FROM quiz_answers WHERE quizAttemptId = :attemptId")
    suspend fun getAnswersForAttempt(attemptId: String): List<QuizAnswerEntity>

    @Query("SELECT COUNT(*) FROM quiz_attempts")
    fun countTotalAttempts(): Flow<Int>

    @Query("SELECT AVG(scorePercentage) FROM quiz_attempts WHERE userId = :userId")
    fun observeUserAverageScore(userId: String): Flow<Float?>

    @Query("SELECT * FROM quiz_attempts ORDER BY completedAt DESC LIMIT 50")
    fun observeAllRecentAttempts(): Flow<List<QuizAttemptEntity>>
}
