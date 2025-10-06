package com.studyflow.tracker.data.service

import android.content.Context
import android.content.SharedPreferences
import com.studyflow.tracker.data.model.Assignment
import com.studyflow.tracker.data.model.AssignmentSource
import com.studyflow.tracker.data.model.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class GoogleClassroomService(private val context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("classroom_prefs", Context.MODE_PRIVATE)
    
    companion object {
        private const val PREF_CLASSROOM_TOKEN = "classroom_token"
        private const val PREF_CLASSROOM_CONNECTED = "classroom_connected"
        private const val PREF_AUTO_SYNC = "auto_sync"
        private const val CLASSROOM_API_BASE = "https://classroom.googleapis.com/v1"
    }
    
    var accessToken: String
        get() = prefs.getString(PREF_CLASSROOM_TOKEN, "") ?: ""
        set(value) = prefs.edit().putString(PREF_CLASSROOM_TOKEN, value).apply()
    
    var isConnected: Boolean
        get() = prefs.getBoolean(PREF_CLASSROOM_CONNECTED, false)
        set(value) = prefs.edit().putBoolean(PREF_CLASSROOM_CONNECTED, value).apply()
    
    var autoSync: Boolean
        get() = prefs.getBoolean(PREF_AUTO_SYNC, true)
        set(value) = prefs.edit().putBoolean(PREF_AUTO_SYNC, value).apply()
    
    suspend fun testConnection(): Boolean = withContext(Dispatchers.IO) {
        try {
            if (accessToken.isBlank()) return@withContext false
            
            val url = URL("$CLASSROOM_API_BASE/courses")
            val connection = url.openConnection() as HttpURLConnection
            
            connection.requestMethod = "GET"
            connection.setRequestProperty("Authorization", "Bearer $accessToken")
            
            val responseCode = connection.responseCode
            connection.disconnect()
            
            val connected = responseCode == 200
            isConnected = connected
            connected
        } catch (e: Exception) {
            isConnected = false
            false
        }
    }
    
    suspend fun fetchAssignments(): List<Assignment> = withContext(Dispatchers.IO) {
        try {
            if (!isConnected || accessToken.isBlank()) return@withContext emptyList()
            
            val assignments = mutableListOf<Assignment>()
            
            // Fetch courses first
            val courses = fetchCourses()
            
            // Fetch coursework (assignments) for each course
            courses.forEach { course ->
                val courseAssignments = fetchCourseworkForCourse(course.first, course.second)
                assignments.addAll(courseAssignments)
            }
            
            assignments
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private suspend fun fetchCourses(): List<Pair<String, String>> = withContext(Dispatchers.IO) {
        try {
            val url = URL("$CLASSROOM_API_BASE/courses?courseStates=ACTIVE")
            val connection = url.openConnection() as HttpURLConnection
            
            connection.requestMethod = "GET"
            connection.setRequestProperty("Authorization", "Bearer $accessToken")
            
            val response = BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }
            connection.disconnect()
            
            val jsonResponse = JSONObject(response)
            val coursesArray = jsonResponse.optJSONArray("courses") ?: JSONArray()
            val courses = mutableListOf<Pair<String, String>>()
            
            for (i in 0 until coursesArray.length()) {
                val course = coursesArray.getJSONObject(i)
                val id = course.getString("id")
                val name = course.getString("name")
                courses.add(Pair(id, name))
            }
            
            courses
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private suspend fun fetchCourseworkForCourse(courseId: String, courseName: String): List<Assignment> = withContext(Dispatchers.IO) {
        try {
            val url = URL("$CLASSROOM_API_BASE/courses/$courseId/courseWork")
            val connection = url.openConnection() as HttpURLConnection
            
            connection.requestMethod = "GET"
            connection.setRequestProperty("Authorization", "Bearer $accessToken")
            
            val response = BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }
            connection.disconnect()
            
            val jsonResponse = JSONObject(response)
            val courseworkArray = jsonResponse.optJSONArray("courseWork") ?: JSONArray()
            val assignments = mutableListOf<Assignment>()
            
            for (i in 0 until courseworkArray.length()) {
                val coursework = courseworkArray.getJSONObject(i)
                val assignment = parseClassroomAssignment(coursework, courseName)
                if (assignment != null) {
                    assignments.add(assignment)
                }
            }
            
            assignments
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private fun parseClassroomAssignment(json: JSONObject, courseName: String): Assignment? {
        try {
            val title = json.getString("title")
            val description = json.optString("description", "")
            val dueDate = json.optJSONObject("dueDate")
            val dueTime = json.optJSONObject("dueTime")
            
            if (dueDate == null) return null // Skip assignments without due dates
            
            val year = dueDate.getInt("year")
            val month = dueDate.getInt("month") - 1 // Calendar months are 0-based
            val day = dueDate.getInt("day")
            
            val hour = dueTime?.optInt("hours", 23) ?: 23
            val minute = dueTime?.optInt("minutes", 59) ?: 59
            
            val assignmentDueDate = Calendar.getInstance().apply {
                set(year, month, day, hour, minute, 0)
            }.time
            
            val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
            val dueTimeString = timeFormatter.format(assignmentDueDate)
            
            // Determine subject based on course name
            val subject = determineSubject(courseName)
            
            // Determine priority based on due date
            val priority = when {
                isWithinDays(assignmentDueDate, 4) -> Priority.HIGH
                isWithinDays(assignmentDueDate, 20) -> Priority.MEDIUM
                else -> Priority.LOW
            }
            
            return Assignment(
                title = title,
                description = description,
                subject = subject,
                courseName = courseName,
                dueDate = assignmentDueDate,
                dueTime = dueTimeString,
                priority = priority,
                completed = false,
                createdAt = Date(),
                customColor = getSubjectColor(subject),
                source = AssignmentSource.GOOGLE_CLASSROOM
            )
        } catch (e: Exception) {
            return null
        }
    }
    
    private fun determineSubject(courseName: String): String {
        val lowerCourseName = courseName.lowercase()
        return when {
            lowerCourseName.contains("math") || lowerCourseName.contains("algebra") || 
            lowerCourseName.contains("calculus") || lowerCourseName.contains("geometry") -> "math"
            lowerCourseName.contains("science") || lowerCourseName.contains("biology") || 
            lowerCourseName.contains("chemistry") || lowerCourseName.contains("physics") -> "science"
            lowerCourseName.contains("english") || lowerCourseName.contains("literature") || 
            lowerCourseName.contains("writing") -> "english"
            lowerCourseName.contains("history") || lowerCourseName.contains("social") -> "history"
            lowerCourseName.contains("art") || lowerCourseName.contains("music") || 
            lowerCourseName.contains("drama") -> "art"
            lowerCourseName.contains("computer") || lowerCourseName.contains("programming") || 
            lowerCourseName.contains("coding") -> "computer"
            else -> "other"
        }
    }
    
    private fun getSubjectColor(subject: String): String {
        return when (subject) {
            "math" -> "#ef4444"
            "science" -> "#10b981"
            "english" -> "#8b5cf6"
            "history" -> "#f59e0b"
            "art" -> "#ec4899"
            "computer" -> "#06b6d4"
            else -> "#667eea"
        }
    }
    
    private fun isWithinDays(date: Date, days: Int): Boolean {
        val now = Date()
        val diffInMillis = date.time - now.time
        val diffInDays = diffInMillis / (1000 * 60 * 60 * 24)
        return diffInDays <= days && diffInDays >= 0
    }
    
    fun logout() {
        accessToken = ""
        isConnected = false
        
        val prefs = context.getSharedPreferences("oauth_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .remove("classroom_user_name")
            .remove("classroom_user_email")
            .apply()
    }
}
