package com.studyflow.tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.studyflow.tracker.ui.StudyFlowApp
import com.studyflow.tracker.ui.theme.StudyFlowTheme
import com.studyflow.tracker.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    
    private val viewModel: MainViewModel by viewModels {
        val database = (application as StudyFlowApplication).database
        MainViewModelFactory(database.assignmentDao(), this)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            StudyFlowTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    StudyFlowApp(viewModel = viewModel)
                }
            }
        }
        
        // Initialize app with real data only
        viewModel.initializeApp()
    }
}
