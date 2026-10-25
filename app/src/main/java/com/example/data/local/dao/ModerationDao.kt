package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ModerationAction
import com.example.data.model.ModerationTaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ModerationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: ModerationTaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<ModerationTaskEntity>)

    @Update
    suspend fun updateTask(task: ModerationTaskEntity)

    @Query("SELECT * FROM moderation_tasks WHERE action = 'SUBMITTED' ORDER BY createdAt ASC")
    fun observePendingTasks(): Flow<List<ModerationTaskEntity>>

    @Query("SELECT * FROM moderation_tasks ORDER BY createdAt DESC LIMIT 50")
    fun observeRecentTasks(): Flow<List<ModerationTaskEntity>>

    @Query("SELECT COUNT(*) FROM moderation_tasks WHERE action = 'SUBMITTED'")
    fun countPendingTasks(): Flow<Int>

    @Query("SELECT COUNT(*) FROM moderation_tasks WHERE action = 'APPROVED'")
    fun countApprovedTasks(): Flow<Int>

    @Query("SELECT COUNT(*) FROM moderation_tasks WHERE action = 'REJECTED'")
    fun countRejectedTasks(): Flow<Int>

    @Query("UPDATE moderation_tasks SET action = :action, reviewerUserId = :reviewerId, reviewerName = :reviewerName, feedbackNotes = :notes WHERE questionId = :questionId")
    suspend fun resolveTask(questionId: String, action: ModerationAction, reviewerId: String, reviewerName: String, notes: String)
}
