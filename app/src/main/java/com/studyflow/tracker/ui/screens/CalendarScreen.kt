package com.studyflow.tracker.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.studyflow.tracker.data.model.Assignment
import com.studyflow.tracker.ui.components.AssignmentCard
import com.studyflow.tracker.ui.theme.*
import com.studyflow.tracker.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CalendarScreen(
    viewModel: MainViewModel,
    onEditAssignment: (Assignment) -> Unit = {}
) {
    var currentMonth by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedDate by remember { mutableStateOf<Date?>(null) }
    var showPip by remember { mutableStateOf(false) }
    var pipDate by remember { mutableStateOf<Date?>(null) }
    val allAssignments by viewModel.filteredAssignments.collectAsState()
    val haptic = LocalHapticFeedback.current
    
    // Get assignments for selected date
    val selectedDateAssignments = selectedDate?.let { date ->
        allAssignments.filter { assignment ->
            isSameDay(assignment.dueDate, date)
        }
    } ?: emptyList()
    
    // Get assignments for pip date
    val pipAssignments = pipDate?.let { date ->
        allAssignments.filter { assignment ->
            isSameDay(assignment.dueDate, date)
        }
    } ?: emptyList()
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Calendar Header
            CalendarHeader(
                currentMonth = currentMonth,
                onPreviousMonth = {
                    currentMonth = Calendar.getInstance().apply {
                        time = currentMonth.time
                        add(Calendar.MONTH, -1)
                    }
                },
                onNextMonth = {
                    currentMonth = Calendar.getInstance().apply {
                        time = currentMonth.time
                        add(Calendar.MONTH, 1)
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Monthly Calendar Grid
            MonthlyCalendarGrid(
                currentMonth = currentMonth,
                assignments = allAssignments,
                selectedDate = selectedDate,
                onDateClick = { date -> selectedDate = date },
                onDateLongPress = { date ->
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    pipDate = date
                    showPip = true
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Assignments for selected date
            if (selectedDate != null) {
                Text(
                    text = "Assignments for ${SimpleDateFormat("EEEE, MMM dd", Locale.getDefault()).format(selectedDate!!)}:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                if (selectedDateAssignments.isNotEmpty()) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(selectedDateAssignments) { assignment ->
                            AssignmentCard(
                                assignment = assignment,
                                onToggleComplete = { viewModel.toggleAssignmentCompletion(assignment) },
                                onDelete = { viewModel.deleteAssignment(assignment) },
                                onEdit = { onEditAssignment(assignment) }
                            )
                        }
                    }
                } else {
                    EmptyDateCard()
                }
            } else {
                SelectDatePromptCard()
            }
        }
        
        // Floating Pip for Long Press
        if (showPip && pipDate != null) {
            Popup(
                onDismissRequest = { showPip = false },
                properties = PopupProperties(focusable = true)
            ) {
                AssignmentPip(
                    date = pipDate!!,
                    assignments = pipAssignments,
                    onDismiss = { showPip = false }
                )
            }
        }
    }
}

@Composable
fun CalendarHeader(
    currentMonth: Calendar,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous month")
        }
        
        Text(
            text = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(currentMonth.time),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        IconButton(onClick = onNextMonth) {
            Icon(Icons.Default.ChevronRight, contentDescription = "Next month")
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MonthlyCalendarGrid(
    currentMonth: Calendar,
    assignments: List<Assignment>,
    selectedDate: Date?,
    onDateClick: (Date) -> Unit,
    onDateLongPress: (Date) -> Unit
) {
    val monthStart = Calendar.getInstance().apply {
        time = currentMonth.time
        set(Calendar.DAY_OF_MONTH, 1)
    }
    val monthEnd = Calendar.getInstance().apply {
        time = currentMonth.time
        set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
    }
    
    val firstDayOfWeek = monthStart.get(Calendar.DAY_OF_WEEK) - 1
    val daysInMonth = monthEnd.get(Calendar.DAY_OF_MONTH)
    
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Day headers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Calendar grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.height(300.dp)
            ) {
                // Empty cells for days before month starts
                items(firstDayOfWeek) {
                    Spacer(modifier = Modifier.aspectRatio(1f))
                }
                
                // Days of the month
                items(daysInMonth) { dayIndex ->
                    val dayNumber = dayIndex + 1
                    val dayDate = Calendar.getInstance().apply {
                        time = currentMonth.time
                        set(Calendar.DAY_OF_MONTH, dayNumber)
                    }.time
                    
                    val dayAssignments = assignments.filter { assignment ->
                        isSameDay(assignment.dueDate, dayDate)
                    }
                    
                    val isToday = isSameDay(dayDate, Date())
                    val isSelected = selectedDate?.let { isSameDay(it, dayDate) } ?: false
                    
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isSelected -> MaterialTheme.colorScheme.primary
                                    isToday -> MaterialTheme.colorScheme.primaryContainer
                                    else -> Color.Transparent
                                }
                            )
                            .combinedClickable(
                                onClick = { onDateClick(dayDate) },
                                onLongClick = { onDateLongPress(dayDate) }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = dayNumber.toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = when {
                                    isSelected -> MaterialTheme.colorScheme.onPrimary
                                    isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                                    else -> MaterialTheme.colorScheme.onSurface
                                },
                                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                            
                            if (dayAssignments.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.onPrimary
                                            else MaterialTheme.colorScheme.primary
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AssignmentPip(
    date: Date,
    assignments: List<Assignment>,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(300.dp)
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
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
                    text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
            
            if (assignments.isNotEmpty()) {
                assignments.take(3).forEach { assignment ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(assignment.customColor)))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = assignment.title,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1
                        )
                    }
                }
                if (assignments.size > 3) {
                    Text(
                        text = "and ${assignments.size - 3} more...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Text(
                    text = "No assignments due",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun EmptyDateCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Assignment,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "No assignments for this date",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun SelectDatePromptCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.TouchApp,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tap a date to view assignments\nLong press for quick preview",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

fun isSameDay(date1: Date, date2: Date): Boolean {
    val cal1 = Calendar.getInstance().apply { time = date1 }
    val cal2 = Calendar.getInstance().apply { time = date2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
           cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

