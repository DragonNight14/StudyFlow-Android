package com.studyflow.tracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.studyflow.tracker.ui.components.AssignmentCard
import com.studyflow.tracker.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllAssignmentsScreen(
    viewModel: MainViewModel,
    onEditAssignment: (com.studyflow.tracker.data.model.Assignment) -> Unit = {}
) {
    val filteredAssignments by viewModel.filteredAssignments.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedSubject by viewModel.selectedSubject.collectAsState()
    val selectedPriority by viewModel.selectedPriority.collectAsState()
    val showCompleted by viewModel.showCompleted.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            label = { Text("Search assignments...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Filter Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Subject Filter
            var subjectExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = subjectExpanded,
                onExpandedChange = { subjectExpanded = !subjectExpanded },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = if (selectedSubject == "all") "All Subjects" else selectedSubject.replaceFirstChar { it.uppercase() },
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectExpanded) },
                    modifier = Modifier.menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = subjectExpanded,
                    onDismissRequest = { subjectExpanded = false }
                ) {
                    val subjects = listOf("all", "math", "science", "english", "history", "art", "music", "pe", "computer science", "foreign language", "other")
                    subjects.forEach { subject ->
                        DropdownMenuItem(
                            text = { Text(if (subject == "all") "All Subjects" else subject.replaceFirstChar { it.uppercase() }) },
                            onClick = {
                                viewModel.updateSubjectFilter(subject)
                                subjectExpanded = false
                            }
                        )
                    }
                }
            }
            
            // Priority Filter
            var priorityExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = priorityExpanded,
                onExpandedChange = { priorityExpanded = !priorityExpanded },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = if (selectedPriority == "all") "All Priorities" else selectedPriority.replaceFirstChar { it.uppercase() },
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = priorityExpanded) },
                    modifier = Modifier.menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = priorityExpanded,
                    onDismissRequest = { priorityExpanded = false }
                ) {
                    val priorities = listOf("all", "high", "medium", "low")
                    priorities.forEach { priority ->
                        DropdownMenuItem(
                            text = { Text(if (priority == "all") "All Priorities" else priority.replaceFirstChar { it.uppercase() }) },
                            onClick = {
                                viewModel.updatePriorityFilter(priority)
                                priorityExpanded = false
                            }
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Show Completed Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = showCompleted,
                onCheckedChange = { viewModel.toggleShowCompleted() }
            )
            Text(
                text = "Show completed assignments",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Results count
        Text(
            text = "${filteredAssignments.size} assignments found",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Assignments List
        if (filteredAssignments.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.SearchOff,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No assignments found",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Try adjusting your search or filters",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredAssignments) { assignment ->
                    AssignmentCard(
                        assignment = assignment,
                        onToggleComplete = { viewModel.toggleAssignmentCompletion(assignment) },
                        onDelete = { viewModel.deleteAssignment(assignment) },
                        onEdit = { onEditAssignment(assignment) }
                    )
                }
            }
        }
    }
}
