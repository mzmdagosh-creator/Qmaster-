package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.BookmarkEntity
import com.example.data.model.CollectionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Delete
    suspend fun deleteBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE userId = :userId AND questionId = :questionId")
    suspend fun removeBookmark(userId: String, questionId: String)

    @Query("SELECT * FROM bookmarks WHERE userId = :userId ORDER BY bookmarkedAt DESC")
    fun observeBookmarksForUser(userId: String): Flow<List<BookmarkEntity>>

    @Query("SELECT questionId FROM bookmarks WHERE userId = :userId")
    fun observeBookmarkedQuestionIds(userId: String): Flow<List<String>>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE userId = :userId AND questionId = :questionId)")
    suspend fun isBookmarked(userId: String, questionId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollection(collection: CollectionEntity)

    @Query("SELECT * FROM collections WHERE userId = :userId OR isCuratedByAdvisor = 1 ORDER BY createdAt DESC")
    fun observeCollections(userId: String): Flow<List<CollectionEntity>>
}
