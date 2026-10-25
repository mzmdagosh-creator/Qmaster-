package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.AdvisorRecommendationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AdvisorDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecommendation(rec: AdvisorRecommendationEntity)

    @Query("SELECT * FROM advisor_recommendations WHERE targetUserId = :userId ORDER BY createdAt DESC")
    fun observeRecommendationsForUser(userId: String): Flow<List<AdvisorRecommendationEntity>>

    @Query("SELECT * FROM advisor_recommendations ORDER BY createdAt DESC LIMIT 50")
    fun observeAllRecommendations(): Flow<List<AdvisorRecommendationEntity>>

    @Query("UPDATE advisor_recommendations SET isCompleted = 1 WHERE id = :id")
    suspend fun markCompleted(id: String)
}
