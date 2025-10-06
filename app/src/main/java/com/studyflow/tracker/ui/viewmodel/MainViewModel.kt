package com.studyflow.tracker.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyflow.tracker.data.model.Assignment
import com.studyflow.tracker.data.model.Priority
import com.studyflow.tracker.data.repository.AssignmentRepository
import com.studyflow.tracker.data.service.CanvasService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Calendar

data class AssignmentStats(
    val totalActive: Int = 0,
    val completed: Int = 0,
    val overdue: Int = 0,
    val highPriority: Int = 0,
    val completionPercentage: Float = 0f,
    val streak: Int = 0
)

class MainViewModel(
    private val repository: AssignmentRepository,
    private val context: Context
) : ViewModel() {
    
    private val canvasService = CanvasService(context)
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _selectedSubject = MutableStateFlow("all")
    val selectedSubject: StateFlow<String> = _selectedSubject.asStateFlow()
    
    private val _selectedPriority = MutableStateFlow("all")
    val selectedPriority: StateFlow<String> = _selectedPriority.asStateFlow()
    
    private val _showCompleted = MutableStateFlow(false)
    val showCompleted: StateFlow<Boolean> = _showCompleted.asStateFlow()
    
    // Statistics
    val stats: StateFlow<AssignmentStats> = combine(
        repository.getPendingCount(),
        repository.getCompletedCount(),
        repository.getOverdueCount(),
        repository.getHighPriorityCount()
    ) { pending, completed, overdue, highPriority ->
        val total = pending + completed
        val completionPercentage = if (total > 0) (completed.toFloat() / total) * 100 else 0f
        
        AssignmentStats(
            totalActive = pending,
            completed = completed,
            overdue = overdue,
            highPriority = highPriority,
            completionPercentage = completionPercentage,
            streak = calculateStreak() // TODO: Implement streak calculation
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AssignmentStats()
    )
    
    // High Priority: due in ≤ 4 days
    val highPriorityAssignments: StateFlow<List<Assignment>> = repository.getPendingAssignments()
        .map { assignments ->
            val now = Date()
            val fourDaysFromNow = Calendar.getInstance().apply {
                time = now
                add(Calendar.DAY_OF_YEAR, 4)
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
            }.time
            
            assignments.filter { assignment ->
                !assignment.completed && assignment.dueDate.time <= fourDaysFromNow.time
            }.sortedBy { it.dueDate }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // Coming Up: due in 5–20 days (≈ 1½ weeks to 3 weeks)
    val comingUpAssignments: StateFlow<List<Assignment>> = repository.getPendingAssignments()
        .map { assignments ->
            val now = Date()
            val fiveDaysFromNow = Calendar.getInstance().apply {
                time = now
                add(Calendar.DAY_OF_YEAR, 5)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }.time
            val twentyDaysFromNow = Calendar.getInstance().apply {
                time = now
                add(Calendar.DAY_OF_YEAR, 20)
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
            }.time
            
            assignments.filter { assignment ->
                !assignment.completed && 
                assignment.dueDate.time >= fiveDaysFromNow.time &&
                assignment.dueDate.time <= twentyDaysFromNow.time
            }.sortedBy { it.dueDate }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // Worry About It Later: due ≥ 21 days
    val longTermAssignments: StateFlow<List<Assignment>> = repository.getPendingAssignments()
        .map { assignments ->
            val now = Date()
            val twentyOneDaysFromNow = Calendar.getInstance().apply {
                time = now
                add(Calendar.DAY_OF_YEAR, 21)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }.time
            
            assignments.filter { assignment ->
                !assignment.completed && assignment.dueDate.time >= twentyOneDaysFromNow.time
            }.sortedBy { it.dueDate }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // Overdue assignments
    val overdueAssignments: StateFlow<List<Assignment>> = repository.getPendingAssignments()
        .map { assignments ->
            val now = Date()
            
            assignments.filter { assignment ->
                !assignment.completed && assignment.dueDate.before(now)
            }.sortedBy { it.dueDate }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // Completed assignments
    val completedAssignments: StateFlow<List<Assignment>> = repository.getCompletedAssignments()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // All assignments with filters applied
    val filteredAssignments: StateFlow<List<Assignment>> = combine(
        repository.getAllAssignments(),
        searchQuery,
        selectedSubject,
        selectedPriority,
        showCompleted
    ) { assignments, query, subject, priority, includeCompleted ->
        assignments.filter { assignment ->
            val matchesQuery = if (query.isBlank()) true else {
                assignment.title.contains(query, ignoreCase = true) ||
                assignment.description.contains(query, ignoreCase = true) ||
                assignment.subject.contains(query, ignoreCase = true)
            }
            
            val matchesSubject = subject == "all" || assignment.subject == subject
            
            val matchesPriority = priority == "all" || assignment.priority.name.lowercase() == priority.lowercase()
            
            val matchesCompletion = if (includeCompleted) true else !assignment.completed
            
            matchesQuery && matchesSubject && matchesPriority && matchesCompletion
        }.sortedBy { it.dueDate }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun updateSubjectFilter(subject: String) {
        _selectedSubject.value = subject
    }
    
    fun updatePriorityFilter(priority: String) {
        _selectedPriority.value = priority
    }
    
    fun toggleShowCompleted() {
        _showCompleted.value = !_showCompleted.value
    }
    
    fun toggleAssignmentCompletion(assignment: Assignment) {
        viewModelScope.launch {
            repository.toggleCompletion(assignment)
        }
    }
    
    fun deleteAssignment(assignment: Assignment) {
        viewModelScope.launch {
            repository.deleteAssignment(assignment)
        }
    }
    
    fun deleteAllCompleted() {
        viewModelScope.launch {
            repository.deleteAllCompleted()
        }
    }
    
    fun addAssignment(assignment: Assignment) {
        viewModelScope.launch {
            repository.insertAssignment(assignment)
        }
    }
    
    fun updateAssignment(assignment: Assignment) {
        viewModelScope.launch {
            repository.updateAssignment(assignment)
        }
    }
    
    fun refreshData() {
        viewModelScope.launch {
            if (canvasService.isConnected && canvasService.autoSync) {
                syncWithCanvas()
            }
        }
    }
    
    suspend fun syncWithCanvas(): Boolean {
        return try {
            val canvasAssignments = canvasService.fetchAssignments()
            canvasAssignments.forEach { assignment ->
                repository.insertAssignment(assignment)
            }
            true
        } catch (e: Exception) {
            false
        }
    }
    
    fun setCanvasUrl(url: String) {
        canvasService.canvasUrl = url
    }
    
    fun setCanvasToken(token: String) {
        canvasService.canvasToken = token
    }
    
    suspend fun testCanvasConnection(): Boolean {
        return canvasService.testConnection()
    }
    
    fun getCanvasConnectionStatus(): Boolean {
        return canvasService.isConnected
    }
    
    fun setAutoSync(enabled: Boolean) {
        canvasService.autoSync = enabled
    }
    
    fun getAutoSyncStatus(): Boolean {
        return canvasService.autoSync
    }
    
    private fun calculateStreak(): Int {
        // TODO: Implement streak calculation based on completion history
        return 0
    }
    
    // Initialize with real data only - no mock data
    fun initializeApp() {
        viewModelScope.launch {
            // Only sync if Canvas is connected and auto-sync is enabled
            if (canvasService.isConnected && canvasService.autoSync) {
                syncWithCanvas()
            }
        }
    }
}
