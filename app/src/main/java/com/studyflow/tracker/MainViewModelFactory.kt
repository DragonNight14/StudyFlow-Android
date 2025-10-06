package com.studyflow.tracker

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.studyflow.tracker.data.database.AssignmentDao
import com.studyflow.tracker.data.repository.AssignmentRepository
import com.studyflow.tracker.ui.viewmodel.MainViewModel

class MainViewModelFactory(
    private val assignmentDao: AssignmentDao,
    private val context: Context
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(AssignmentRepository(assignmentDao), context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
