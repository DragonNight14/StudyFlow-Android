package com.studyflow.tracker.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.studyflow.tracker.data.model.Assignment

@Database(
    entities = [Assignment::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class StudyFlowDatabase : RoomDatabase() {
    
    abstract fun assignmentDao(): AssignmentDao
    
    companion object {
        @Volatile
        private var INSTANCE: StudyFlowDatabase? = null
        
        fun getDatabase(context: Context): StudyFlowDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StudyFlowDatabase::class.java,
                    "studyflow_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
