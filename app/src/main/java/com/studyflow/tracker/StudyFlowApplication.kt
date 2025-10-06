package com.studyflow.tracker

import android.app.Application
import com.studyflow.tracker.data.database.StudyFlowDatabase

class StudyFlowApplication : Application() {
    
    val database by lazy { StudyFlowDatabase.getDatabase(this) }
    
    override fun onCreate() {
        super.onCreate()
    }
}
