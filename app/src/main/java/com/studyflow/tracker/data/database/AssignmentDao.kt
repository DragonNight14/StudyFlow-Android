package com.studyflow.tracker.data.database

import androidx.room.*
import com.studyflow.tracker.data.model.Assignment
import com.studyflow.tracker.data.model.Priority
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface AssignmentDao {
    
    @Query("SELECT * FROM assignments ORDER BY dueDate ASC")
    fun getAllAssignments(): Flow<List<Assignment>>
    
    @Query("SELECT * FROM assignments WHERE completed = 0 ORDER BY dueDate ASC")
    fun getPendingAssignments(): Flow<List<Assignment>>
    
    @Query("SELECT * FROM assignments WHERE completed = 1 ORDER BY completedAt DESC")
    fun getCompletedAssignments(): Flow<List<Assignment>>
    
    @Query("SELECT * FROM assignments WHERE priority = :priority AND completed = 0 ORDER BY dueDate ASC")
    fun getAssignmentsByPriority(priority: Priority): Flow<List<Assignment>>
    
    @Query("SELECT * FROM assignments WHERE subject = :subject ORDER BY dueDate ASC")
    fun getAssignmentsBySubject(subject: String): Flow<List<Assignment>>
    
    @Query("SELECT * FROM assignments WHERE dueDate BETWEEN :startDate AND :endDate ORDER BY dueDate ASC")
    fun getAssignmentsByDateRange(startDate: Date, endDate: Date): Flow<List<Assignment>>
    
    @Query("SELECT * FROM assignments WHERE DATE(dueDate/1000, 'unixepoch') = DATE(:date/1000, 'unixepoch') ORDER BY dueDate ASC")
    fun getAssignmentsByDate(date: Date): Flow<List<Assignment>>
    
    @Query("SELECT * FROM assignments WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR subject LIKE '%' || :query || '%'")
    fun searchAssignments(query: String): Flow<List<Assignment>>
    
    @Query("SELECT COUNT(*) FROM assignments WHERE completed = 0")
    fun getPendingCount(): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM assignments WHERE completed = 1")
    fun getCompletedCount(): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM assignments WHERE completed = 0 AND dueDate < :currentDate")
    fun getOverdueCount(currentDate: Date): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM assignments WHERE completed = 0 AND priority = 'HIGH'")
    fun getHighPriorityCount(): Flow<Int>
    
    @Query("SELECT * FROM assignments WHERE id = :id")
    suspend fun getAssignmentById(id: Long): Assignment?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignment(assignment: Assignment): Long
    
    @Update
    suspend fun updateAssignment(assignment: Assignment)
    
    @Delete
    suspend fun deleteAssignment(assignment: Assignment)
    
    @Query("DELETE FROM assignments WHERE id = :id")
    suspend fun deleteAssignmentById(id: Long)
    
    @Query("UPDATE assignments SET completed = :completed, completedAt = :completedAt WHERE id = :id")
    suspend fun updateCompletionStatus(id: Long, completed: Boolean, completedAt: Date?)
    
    @Query("DELETE FROM assignments WHERE completed = 1")
    suspend fun deleteAllCompleted()
    
    @Query("DELETE FROM assignments")
    suspend fun deleteAllAssignments()
}
