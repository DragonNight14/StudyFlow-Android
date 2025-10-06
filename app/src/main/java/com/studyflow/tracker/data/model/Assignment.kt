package com.studyflow.tracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import android.os.Parcelable
import java.util.Date

@Entity(tableName = "assignments")
@Parcelize
data class Assignment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val subject: String,
    val courseName: String = "",
    val dueDate: Date,
    val dueTime: String = "23:59",
    val completed: Boolean = false,
    val priority: Priority = Priority.MEDIUM,
    val customColor: String = "#667eea",
    val source: AssignmentSource = AssignmentSource.MANUAL,
    val createdAt: Date = Date(),
    val completedAt: Date? = null,
    val estimatedHours: Float = 0f,
    val actualHours: Float = 0f,
    val tags: List<String> = emptyList(),
    val attachments: List<String> = emptyList(),
    val notes: String = ""
) : Parcelable

enum class Priority(val displayName: String, val color: String) {
    LOW("Low", "#10b981"),
    MEDIUM("Medium", "#f59e0b"),
    HIGH("High", "#ef4444")
}

enum class AssignmentSource(val displayName: String) {
    MANUAL("Manual"),
    CANVAS("Canvas LMS"),
    GOOGLE_CLASSROOM("Google Classroom"),
    BLACKBOARD("Blackboard"),
    MOODLE("Moodle")
}

enum class Subject(val displayName: String, val emoji: String, val color: String) {
    MATH("Math", "📐", "#3b82f6"),
    SCIENCE("Science", "🔬", "#10b981"),
    ENGLISH("English", "📚", "#8b5cf6"),
    HISTORY("History", "🏛️", "#f59e0b"),
    ART("Art", "🎨", "#ec4899"),
    MUSIC("Music", "🎵", "#06b6d4"),
    PE("Physical Education", "⚽", "#84cc16"),
    COMPUTER_SCIENCE("Computer Science", "💻", "#6366f1"),
    FOREIGN_LANGUAGE("Foreign Language", "🌍", "#f97316"),
    OTHER("Other", "📝", "#6b7280")
}
