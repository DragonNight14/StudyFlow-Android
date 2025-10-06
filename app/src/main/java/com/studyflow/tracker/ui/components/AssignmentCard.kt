package com.studyflow.tracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.studyflow.tracker.data.model.Assignment
import com.studyflow.tracker.data.model.AssignmentSource
import com.studyflow.tracker.data.model.Priority
import com.studyflow.tracker.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentCard(
    assignment: Assignment,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (assignment.completed) 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Priority indicator
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(getPriorityColor(assignment.priority))
                )
                
                // Actions
                Row {
                    IconButton(
                        onClick = onToggleComplete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (assignment.completed) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = if (assignment.completed) "Mark incomplete" else "Mark complete",
                            tint = if (assignment.completed) LowPriority else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    // Only show edit button for manual assignments
                    if (assignment.source.name == "MANUAL") {
                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit assignment",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete assignment",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Title
            Text(
                text = assignment.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textDecoration = if (assignment.completed) TextDecoration.LineThrough else TextDecoration.None,
                color = if (assignment.completed) 
                    MaterialTheme.colorScheme.onSurfaceVariant 
                else MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            // Description
            if (assignment.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = assignment.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Course, Subject, and Source Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    if (assignment.courseName.isNotBlank()) {
                        Text(
                            text = assignment.courseName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = getSubjectEmoji(assignment.subject) + " " + assignment.subject.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        // Source Badge
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = getSourceColor(assignment.source)
                            ),
                            modifier = Modifier
                        ) {
                            Text(
                                text = getSourceDisplayName(assignment.source),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                
                // Due date and time remaining
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = formatDueDate(assignment.dueDate),
                        style = MaterialTheme.typography.bodySmall,
                        color = getDueDateColor(assignment.dueDate, assignment.completed),
                        fontWeight = FontWeight.Medium
                    )
                    
                    if (!assignment.completed) {
                        Text(
                            text = getTimeRemaining(assignment.dueDate),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Assignment") },
            text = { Text("Are you sure you want to delete \"${assignment.title}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun getPriorityColor(priority: Priority): Color {
    return when (priority) {
        Priority.HIGH -> HighPriority
        Priority.MEDIUM -> MediumPriority
        Priority.LOW -> LowPriority
    }
}

@Composable
private fun getDueDateColor(dueDate: Date, completed: Boolean): Color {
    if (completed) return MaterialTheme.colorScheme.onSurfaceVariant
    
    val now = Date()
    val diffInDays = (dueDate.time - now.time) / (1000 * 60 * 60 * 24)
    
    return when {
        diffInDays < 0 -> HighPriority // Overdue
        diffInDays < 2 -> MediumPriority // Due soon
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

private fun formatDueDate(dueDate: Date): String {
    val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return formatter.format(dueDate)
}

private fun getTimeRemaining(dueDate: Date): String {
    val now = Date()
    val diffInMillis = dueDate.time - now.time
    val diffInDays = diffInMillis / (1000 * 60 * 60 * 24)
    
    return when {
        diffInDays < 0 -> "${abs(diffInDays)} days overdue"
        diffInDays == 0L -> "Due today"
        diffInDays == 1L -> "Due tomorrow"
        diffInDays < 7 -> "Due in ${diffInDays} days"
        else -> "Due in ${diffInDays / 7} weeks"
    }
}

private fun getSubjectEmoji(subject: String): String {
    return when (subject.lowercase()) {
        "math" -> "📐"
        "science" -> "🔬"
        "english" -> "📚"
        "history" -> "🏛️"
        "art" -> "🎨"
        "music" -> "🎵"
        "pe", "physical education" -> "⚽"
        "computer science" -> "💻"
        "foreign language" -> "🌍"
        else -> "📝"
    }
}

@Composable
private fun getSourceColor(source: AssignmentSource): Color {
    return when (source) {
        AssignmentSource.MANUAL -> Color(0xFF6366F1) // Indigo
        AssignmentSource.CANVAS -> Color(0xFFE11D48) // Rose
        AssignmentSource.GOOGLE_CLASSROOM -> Color(0xFF059669) // Emerald
        AssignmentSource.BLACKBOARD -> Color(0xFF7C2D12) // Orange
        AssignmentSource.MOODLE -> Color(0xFF7C3AED) // Violet
    }
}

private fun getSourceDisplayName(source: AssignmentSource): String {
    return when (source) {
        AssignmentSource.MANUAL -> "Manual"
        AssignmentSource.CANVAS -> "Canvas"
        AssignmentSource.GOOGLE_CLASSROOM -> "Classroom"
        AssignmentSource.BLACKBOARD -> "Blackboard"
        AssignmentSource.MOODLE -> "Moodle"
    }
}
