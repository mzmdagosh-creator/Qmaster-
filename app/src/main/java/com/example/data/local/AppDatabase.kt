package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.AdvisorDao
import com.example.data.local.dao.AuditDao
import com.example.data.local.dao.BookmarkDao
import com.example.data.local.dao.ModerationDao
import com.example.data.local.dao.QuestionDao
import com.example.data.local.dao.QuizDao
import com.example.data.local.dao.SubjectDao
import com.example.data.local.dao.UserDao
import com.example.data.model.AdvisorRecommendationEntity
import com.example.data.model.AuditLogEntity
import com.example.data.model.BookmarkEntity
import com.example.data.model.ChapterEntity
import com.example.data.model.CollectionEntity
import com.example.data.model.ModerationTaskEntity
import com.example.data.model.QuestionEntity
import com.example.data.model.QuestionOptionEntity
import com.example.data.model.QuizAnswerEntity
import com.example.data.model.QuizAttemptEntity
import com.example.data.model.SubjectEntity
import com.example.data.model.TopicEntity
import com.example.data.model.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        SubjectEntity::class,
        ChapterEntity::class,
        TopicEntity::class,
        QuestionEntity::class,
        QuestionOptionEntity::class,
        QuizAttemptEntity::class,
        QuizAnswerEntity::class,
        BookmarkEntity::class,
        CollectionEntity::class,
        ModerationTaskEntity::class,
        AuditLogEntity::class,
        AdvisorRecommendationEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun subjectDao(): SubjectDao
    abstract fun questionDao(): QuestionDao
    abstract fun quizDao(): QuizDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun moderationDao(): ModerationDao
    abstract fun auditDao(): AuditDao
    abstract fun advisorDao(): AdvisorDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "edubank_qbank.db"
                )
                .fallbackToDestructiveMigration()
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Seed database asynchronously on creation
                        CoroutineScope(Dispatchers.IO).launch {
                            InitialDataSeeder.seedInitialData(getInstance(context))
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
