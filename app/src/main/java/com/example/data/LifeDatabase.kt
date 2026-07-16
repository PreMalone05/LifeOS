package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserProfileEntity::class,
        TaskEntity::class,
        HabitEntity::class,
        GoalEntity::class,
        MilestoneEntity::class,
        SubTaskEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class LifeDatabase : RoomDatabase() {
    abstract fun lifeDao(): LifeDao

    companion object {
        @Volatile
        private var INSTANCE: LifeDatabase? = null

        fun getDatabase(context: Context): LifeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LifeDatabase::class.java,
                    "life_os_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
