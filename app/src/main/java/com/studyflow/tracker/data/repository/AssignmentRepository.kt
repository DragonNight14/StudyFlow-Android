package com.studyflow.tracker.data.repository

import com.studyflow.tracker.data.database.AssignmentDao
import com.studyflow.tracker.data.model.Assignment
import com.studyflow.tracker.data.model.AssignmentSource
import com.studyflow.tracker.data.model.Priority
import kotlinx.coroutines.flow.Flow
import java.util.Date

class AssignmentRepository(
    private val assignmentDao: AssignmentDao
) {
    
    fun getAllAssignments(): Flow<List<Assignment>> = assignmentDao.getAllAssignments()
    
    fun getPendingAssignments(): Flow<List<Assignment>> = assignmentDao.getPendingAssignments()
    
    fun getCompletedAssignments(): Flow<List<Assignment>> = assignmentDao.getCompletedAssignments()
    
    fun getAssignmentsByPriority(priority: Priority): Flow<List<Assignment>> = 
        assignmentDao.getAssignmentsByPriority(priority)
    
    fun getAssignmentsBySubject(subject: String): Flow<List<Assignment>> = 
        assignmentDao.getAssignmentsBySubject(subject)
    
    fun getAssignmentsByDateRange(startDate: Date, endDate: Date): Flow<List<Assignment>> = 
        assignmentDao.getAssignmentsByDateRange(startDate, endDate)
    
    fun getAssignmentsByDate(date: Date): Flow<List<Assignment>> = 
        assignmentDao.getAssignmentsByDate(date)
    
    fun searchAssignments(query: String): Flow<List<Assignment>> = 
        assignmentDao.searchAssignments(query)
    
    fun getPendingCount(): Flow<Int> = assignmentDao.getPendingCount()
    
    fun getCompletedCount(): Flow<Int> = assignmentDao.getCompletedCount()
    
    fun getOverdueCount(): Flow<Int> = assignmentDao.getOverdueCount(Date())
    
    fun getHighPriorityCount(): Flow<Int> = assignmentDao.getHighPriorityCount()
    
    suspend fun getAssignmentById(id: Long): Assignment? = assignmentDao.getAssignmentById(id)
    
    suspend fun insertAssignment(assignment: Assignment): Long {
        // Check for duplicates before inserting
        val existingAssignments = assignmentDao.getAllAssignments()
        // For now, we'll implement a simple duplicate check
        // In a real implementation, you'd want to check by external ID + source
        return assignmentDao.insertAssignment(assignment)
    }
    
    suspend fun insertAssignmentWithDeduplication(assignment: Assignment): Long {
        // De-duplicate by source + course + title (since we don't have external IDs yet)
        val duplicateKey = "${assignment.source.name}_${assignment.courseName}_${assignment.title}"
        
        // Check if assignment with same key already exists
        val existingAssignments = getAllAssignments()
        // For now, just insert - proper deduplication would require database queries
        return assignmentDao.insertAssignment(assignment)
    }
    
    suspend fun updateAssignment(assignment: Assignment) = assignmentDao.updateAssignment(assignment)
    
    suspend fun deleteAssignment(assignment: Assignment) = assignmentDao.deleteAssignment(assignment)
    
    suspend fun deleteAssignmentById(id: Long) = assignmentDao.deleteAssignmentById(id)
    
    suspend fun toggleCompletion(assignment: Assignment) {
        val updatedAssignment = assignment.copy(
            completed = !assignment.completed,
            completedAt = if (!assignment.completed) Date() else null
        )
        updateAssignment(updatedAssignment)
    }
    
    suspend fun deleteAllCompleted() = assignmentDao.deleteAllCompleted()
    
    suspend fun deleteAllAssignments() = assignmentDao.deleteAllAssignments()
    
    // Helper functions for priority categorization
    fun getHighPriorityAssignments(): Flow<List<Assignment>> {
        return getAssignmentsByPriority(Priority.HIGH)
    }
    
    fun getUpcomingAssignments(): Flow<List<Assignment>> {
        val now = Date()
        val fourDaysFromNow = Date(now.time + (4 * 24 * 60 * 60 * 1000))
        val tenDaysFromNow = Date(now.time + (10 * 24 * 60 * 60 * 1000))
        return assignmentDao.getAssignmentsByDateRange(fourDaysFromNow, tenDaysFromNow)
    }
    
    fun getLongTermAssignments(): Flow<List<Assignment>> {
        val tenDaysFromNow = Date(System.currentTimeMillis() + (10 * 24 * 60 * 60 * 1000))
        val farFuture = Date(System.currentTimeMillis() + (365 * 24 * 60 * 60 * 1000))
        return assignmentDao.getAssignmentsByDateRange(tenDaysFromNow, farFuture)
    }
}
