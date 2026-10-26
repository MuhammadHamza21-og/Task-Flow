package com.example.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.TaskDao
import com.example.data.local.entity.TaskEntity
import com.example.data.model.TaskPriority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Room Database definition for TaskFlow.
 * Provides singleton access to the underlying SQLite database and TaskDao.
 */
@Database(
    entities = [TaskEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "taskflow_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        /**
         * Pre-populates the database with helpful starter tasks on initial creation.
         */
        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch {
                        populateInitialTasks(database.taskDao())
                    }
                }
            }

            private suspend fun populateInitialTasks(taskDao: TaskDao) {
                val currentTime = System.currentTimeMillis()
                val tomorrow = currentTime + (24 * 60 * 60 * 1000)

                taskDao.insertTask(
                    TaskEntity(
                        title = "Welcome to TaskFlow! 🎉",
                        description = "Tap the checkbox on the left to mark this task complete, or tap anywhere to edit.",
                        isCompleted = false,
                        priority = TaskPriority.HIGH.name,
                        category = "Personal",
                        dueDate = currentTime,
                        hasReminder = false,
                        createdAt = currentTime
                    )
                )

                taskDao.insertTask(
                    TaskEntity(
                        title = "Organize project priorities",
                        description = "Sort tasks by High, Medium, or Low priority to stay focused.",
                        isCompleted = false,
                        priority = TaskPriority.HIGH.name,
                        category = "Work",
                        dueDate = tomorrow,
                        hasReminder = true,
                        createdAt = currentTime
                    )
                )

                taskDao.insertTask(
                    TaskEntity(
                        title = "Review study notes",
                        description = "Prepare chapter summary for the upcoming presentation.",
                        isCompleted = false,
                        priority = TaskPriority.MEDIUM.name,
                        category = "Study",
                        dueDate = tomorrow + (24 * 60 * 60 * 1000),
                        hasReminder = false,
                        createdAt = currentTime
                    )
                )

                taskDao.insertTask(
                    TaskEntity(
                        title = "Grocery shopping list 🛒",
                        description = "Milk, fresh fruits, vegetables, whole grain bread.",
                        isCompleted = true,
                        priority = TaskPriority.LOW.name,
                        category = "Shopping",
                        dueDate = currentTime - (24 * 60 * 60 * 1000),
                        hasReminder = false,
                        createdAt = currentTime - (48 * 60 * 60 * 1000)
                    )
                )
            }
        }
    }
}
