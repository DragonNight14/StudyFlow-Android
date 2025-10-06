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

class CanvasService(private val context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("canvas_prefs", Context.MODE_PRIVATE)
    
    companion object {
        private const val PREF_CANVAS_URL = "canvas_url"
        private const val PREF_CANVAS_TOKEN = "canvas_token"
        private const val PREF_CANVAS_CONNECTED = "canvas_connected"
        private const val PREF_AUTO_SYNC = "auto_sync"
    }
    
    var canvasUrl: String
        get() = prefs.getString(PREF_CANVAS_URL, "") ?: ""
        set(value) = prefs.edit().putString(PREF_CANVAS_URL, value).apply()
    
    var canvasToken: String
        get() = prefs.getString(PREF_CANVAS_TOKEN, "") ?: ""
        set(value) = prefs.edit().putString(PREF_CANVAS_TOKEN, value).apply()
    
    var isConnected: Boolean
        get() = prefs.getBoolean(PREF_CANVAS_CONNECTED, false)
        set(value) = prefs.edit().putBoolean(PREF_CANVAS_CONNECTED, value).apply()
    
    var autoSync: Boolean
        get() = prefs.getBoolean(PREF_AUTO_SYNC, true)
        set(value) = prefs.edit().putBoolean(PREF_AUTO_SYNC, value).apply()
    
    suspend fun testConnection(): Boolean = withContext(Dispatchers.IO) {
        try {
            if (canvasUrl.isBlank()) return@withContext false
            
            val url = URL("$canvasUrl/api/v1/users/self")
            val connection = url.openConnection() as HttpURLConnection
            
            connection.requestMethod = "GET"
            if (canvasToken.isNotBlank()) {
                connection.setRequestProperty("Authorization", "Bearer $canvasToken")
            }
            
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
            if (!isConnected || canvasUrl.isBlank()) return@withContext emptyList()
            
            val assignments = mutableListOf<Assignment>()
            
            // Fetch courses first
            val courses = fetchCourses()
            
            // Fetch assignments for each course
            courses.forEach { course ->
                val courseAssignments = fetchAssignmentsForCourse(course.first, course.second)
                assignments.addAll(courseAssignments)
            }
            
            assignments
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private suspend fun fetchCourses(): List<Pair<String, String>> = withContext(Dispatchers.IO) {
        try {
            val url = URL("$canvasUrl/api/v1/courses?enrollment_state=active")
            val connection = url.openConnection() as HttpURLConnection
            
            connection.requestMethod = "GET"
            if (canvasToken.isNotBlank()) {
                connection.setRequestProperty("Authorization", "Bearer $canvasToken")
            }
            
            val response = BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }
            connection.disconnect()
            
            val jsonArray = JSONArray(response)
            val courses = mutableListOf<Pair<String, String>>()
            
            for (i in 0 until jsonArray.length()) {
                val course = jsonArray.getJSONObject(i)
                val id = course.getString("id")
                val name = course.getString("name")
                courses.add(Pair(id, name))
            }
            
            courses
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private suspend fun fetchAssignmentsForCourse(courseId: String, courseName: String): List<Assignment> = withContext(Dispatchers.IO) {
        try {
            val url = URL("$canvasUrl/api/v1/courses/$courseId/assignments")
            val connection = url.openConnection() as HttpURLConnection
            
            connection.requestMethod = "GET"
            if (canvasToken.isNotBlank()) {
                connection.setRequestProperty("Authorization", "Bearer $canvasToken")
            }
            
            val response = BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }
            connection.disconnect()
            
            val jsonArray = JSONArray(response)
            val assignments = mutableListOf<Assignment>()
            
            for (i in 0 until jsonArray.length()) {
                val assignmentJson = jsonArray.getJSONObject(i)
                val assignment = parseCanvasAssignment(assignmentJson, courseName)
                if (assignment != null) {
                    assignments.add(assignment)
                }
            }
            
            assignments
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private fun parseCanvasAssignment(json: JSONObject, courseName: String): Assignment? {
        try {
            val title = json.getString("name")
            val description = json.optString("description", "")
            val dueDateString = json.optString("due_at", null)
            
            if (dueDateString.isNullOrBlank()) return null
            
            val dueDate = parseCanvasDate(dueDateString) ?: return null
            val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
            val dueTime = timeFormatter.format(dueDate)
            
            // Determine subject based on course name
            val subject = determineSubject(courseName)
            
            // Determine priority based on due date
            val priority = when {
                isWithinDays(dueDate, 3) -> Priority.HIGH
                isWithinDays(dueDate, 7) -> Priority.MEDIUM
                else -> Priority.LOW
            }
            
            return Assignment(
                title = title,
                description = description,
                subject = subject,
                courseName = courseName,
                dueDate = dueDate,
                dueTime = dueTime,
                priority = priority,
                completed = false,
                createdAt = Date(),
                customColor = getSubjectColor(subject),
                source = AssignmentSource.CANVAS
            )
        } catch (e: Exception) {
            return null
        }
    }
    
    private fun parseCanvasDate(dateString: String): Date? {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            format.timeZone = TimeZone.getTimeZone("UTC")
            format.parse(dateString)
        } catch (e: Exception) {
            null
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
}
