package com.studyflow.tracker.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.studyflow.tracker.ui.screens.*
import com.studyflow.tracker.ui.viewmodel.MainViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object AllAssignments : Screen("all", "All", Icons.Default.List)
    object Calendar : Screen("calendar", "Calendar", Icons.Default.DateRange)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyFlowApp(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val items = listOf(
        Screen.Home,
        Screen.AllAssignments,
        Screen.Calendar,
        Screen.Settings
    )
    
    var showAddAssignmentDialog by remember { mutableStateOf(false) }
    var editingAssignment by remember { mutableStateOf<Assignment?>(null) }
    
    Scaffold(
        topBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            
            TopAppBar(
                title = { 
                    Text(
                        text = when(currentRoute) {
                            Screen.Home.route -> "StudyFlow"
                            Screen.AllAssignments.route -> "All Assignments"
                            Screen.Calendar.route -> "Calendar"
                            Screen.Settings.route -> "Settings"
                            else -> "StudyFlow"
                        },
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    // Add search icon for All Assignments screen
                    if (currentRoute == Screen.AllAssignments.route) {
                        IconButton(onClick = { /* TODO: Implement search */ }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                    }
                    
                    // Add refresh/sync icon
                    IconButton(onClick = { 
                        // Refresh data
                        viewModel.refreshData()
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { 
                            Icon(
                                screen.icon, 
                                contentDescription = screen.title,
                                modifier = Modifier.size(24.dp)
                            ) 
                        },
                        label = { 
                            Text(
                                screen.title,
                                style = MaterialTheme.typography.labelSmall
                            ) 
                        },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddAssignmentDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Assignment")
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = viewModel,
                    onEditAssignment = { assignment -> editingAssignment = assignment }
                )
            }
            composable(Screen.AllAssignments.route) {
                AllAssignmentsScreen(
                    viewModel = viewModel,
                    onEditAssignment = { assignment -> editingAssignment = assignment }
                )
            }
            composable(Screen.Calendar.route) {
                CalendarScreen(
                    viewModel = viewModel,
                    onEditAssignment = { assignment -> editingAssignment = assignment }
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(viewModel = viewModel)
            }
        }
    }
    
    if (showAddAssignmentDialog) {
        AddAssignmentDialog(
            onDismiss = { showAddAssignmentDialog = false },
            onSave = { assignment ->
                viewModel.addAssignment(assignment)
                showAddAssignmentDialog = false
            }
        )
    }
    
    editingAssignment?.let { assignment ->
        AddAssignmentDialog(
            assignment = assignment,
            onDismiss = { editingAssignment = null },
            onSave = { updatedAssignment ->
                viewModel.updateAssignment(updatedAssignment)
                editingAssignment = null
            }
        )
    }
}
