package com.studyflow.tracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studyflow.tracker.data.model.Assignment
import com.studyflow.tracker.ui.components.AssignmentCard
import com.studyflow.tracker.ui.components.StatCard
import com.studyflow.tracker.ui.theme.*
import com.studyflow.tracker.ui.viewmodel.AssignmentStats
import com.studyflow.tracker.ui.viewmodel.MainViewModel

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onEditAssignment: (Assignment) -> Unit = {}
) {
    val stats by viewModel.stats.collectAsState()
    val overdueAssignments by viewModel.overdueAssignments.collectAsState()
    val highPriorityAssignments by viewModel.highPriorityAssignments.collectAsState()
    val comingUpAssignments by viewModel.comingUpAssignments.collectAsState()
    val longTermAssignments by viewModel.longTermAssignments.collectAsState()
    val completedAssignments by viewModel.completedAssignments.collectAsState()
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Statistics Dashboard
        item {
            StatsSection(stats = stats)
        }
        
        // Progress Bar
        item {
            ProgressSection(stats = stats)
        }
        
        // Overdue Section (highest priority)
        if (overdueAssignments.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "🚨 Overdue",
                    subtitle = "These assignments are past due - handle immediately!"
                )
            }
            items(overdueAssignments) { assignment ->
                AssignmentCard(
                    assignment = assignment,
                    onToggleComplete = { viewModel.toggleAssignmentCompletion(assignment) },
                    onDelete = { viewModel.deleteAssignment(assignment) },
                    onEdit = { onEditAssignment(assignment) }
                )
            }
        }
        
        // High Priority Section
        if (highPriorityAssignments.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "🔥 High Priority",
                    subtitle = "Due in ≤ 4 days"
                )
            }
            items(highPriorityAssignments) { assignment ->
                AssignmentCard(
                    assignment = assignment,
                    onToggleComplete = { viewModel.toggleAssignmentCompletion(assignment) },
                    onDelete = { viewModel.deleteAssignment(assignment) },
                    onEdit = { onEditAssignment(assignment) }
                )
            }
        }
        
        // Coming Up Section
        if (comingUpAssignments.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "⏰ Coming Up",
                    subtitle = "Due in 5–20 days"
                )
            }
            items(comingUpAssignments) { assignment ->
                AssignmentCard(
                    assignment = assignment,
                    onToggleComplete = { viewModel.toggleAssignmentCompletion(assignment) },
                    onDelete = { viewModel.deleteAssignment(assignment) },
                    onEdit = { onEditAssignment(assignment) }
                )
            }
        }
        
        // Long Term Section
        if (longTermAssignments.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "📅 Worry About It Later",
                    subtitle = "Due ≥ 21 days"
                )
            }
            items(longTermAssignments) { assignment ->
                AssignmentCard(
                    assignment = assignment,
                    onToggleComplete = { viewModel.toggleAssignmentCompletion(assignment) },
                    onDelete = { viewModel.deleteAssignment(assignment) },
                    onEdit = { onEditAssignment(assignment) }
                )
            }
        }
        
        // Completed Section
        if (completedAssignments.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "✅ Completed",
                    subtitle = "Great job! Keep up the momentum"
                )
            }
            items(completedAssignments.take(5)) { assignment ->
                AssignmentCard(
                    assignment = assignment,
                    onToggleComplete = { viewModel.toggleAssignmentCompletion(assignment) },
                    onDelete = { viewModel.deleteAssignment(assignment) },
                    onEdit = { onEditAssignment(assignment) }
                )
            }
        }
        
        // Empty state
        if (overdueAssignments.isEmpty() &&
            highPriorityAssignments.isEmpty() && 
            comingUpAssignments.isEmpty() && 
            longTermAssignments.isEmpty()) {
            item {
                EmptyStateCard()
            }
        }
    }
}

@Composable
fun StatsSection(stats: AssignmentStats) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            title = "Active Tasks",
            value = stats.totalActive.toString(),
            icon = Icons.Default.Assignment,
            color = StudyFlowPrimary
        )
        StatCard(
            modifier = Modifier.weight(1f),
            title = "Completed",
            value = stats.completed.toString(),
            icon = Icons.Default.CheckCircle,
            color = LowPriority
        )
        StatCard(
            modifier = Modifier.weight(1f),
            title = "Overdue",
            value = stats.overdue.toString(),
            icon = Icons.Default.Warning,
            color = HighPriority
        )
        StatCard(
            modifier = Modifier.weight(1f),
            title = "Day Streak",
            value = stats.streak.toString(),
            icon = Icons.Default.LocalFire,
            color = MediumPriority
        )
    }
}

@Composable
fun ProgressSection(stats: AssignmentStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Overall Progress",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${stats.completionPercentage.toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = StudyFlowPrimary
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = stats.completionPercentage / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = StudyFlowPrimary,
                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
fun SectionHeader(title: String, subtitle: String) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun EmptyStateCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Assignment,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No assignments yet!",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Tap the + button to add your first assignment",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
