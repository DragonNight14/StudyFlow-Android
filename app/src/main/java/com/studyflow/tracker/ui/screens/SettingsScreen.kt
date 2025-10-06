package com.studyflow.tracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.studyflow.tracker.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(viewModel: MainViewModel? = null) {
    var isDarkMode by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var showCompletedByDefault by remember { mutableStateOf(false) }
    var defaultPriority by remember { mutableStateOf("Medium") }
    var canvasUrl by remember { mutableStateOf("") }
    var canvasToken by remember { mutableStateOf("") }
    var showCanvasDialog by remember { mutableStateOf(false) }
    var canvasConnected by remember { mutableStateOf(viewModel?.getCanvasConnectionStatus() ?: false) }
    var autoSync by remember { mutableStateOf(viewModel?.getAutoSyncStatus() ?: true) }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "Appearance",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        item {
            SettingsItem(
                icon = Icons.Default.DarkMode,
                title = "Dark Mode",
                subtitle = "Switch between light and dark themes",
                trailing = {
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { isDarkMode = it }
                    )
                }
            )
        }
        
        item {
            Divider(modifier = Modifier.padding(vertical = 8.dp))
        }
        
        item {
            Text(
                text = "Notifications",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        item {
            SettingsItem(
                icon = Icons.Default.Notifications,
                title = "Enable Notifications",
                subtitle = "Get reminded about upcoming assignments",
                trailing = {
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it }
                    )
                }
            )
        }
        
        item {
            SettingsItem(
                icon = Icons.Default.Schedule,
                title = "Reminder Time",
                subtitle = "1 day before due date",
                onClick = { /* TODO: Implement time picker */ }
            )
        }
        
        item {
            Divider(modifier = Modifier.padding(vertical = 8.dp))
        }
        
        item {
            Text(
                text = "Assignments",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        item {
            SettingsItem(
                icon = Icons.Default.Visibility,
                title = "Show Completed by Default",
                subtitle = "Include completed assignments in main view",
                trailing = {
                    Switch(
                        checked = showCompletedByDefault,
                        onCheckedChange = { showCompletedByDefault = it }
                    )
                }
            )
        }
        
        item {
            var expanded by remember { mutableStateOf(false) }
            SettingsItem(
                icon = Icons.Default.Flag,
                title = "Default Priority",
                subtitle = defaultPriority,
                trailing = {
                    TextButton(onClick = { expanded = true }) {
                        Text("Change")
                    }
                }
            )
            
            if (expanded) {
                AlertDialog(
                    onDismissRequest = { expanded = false },
                    title = { Text("Default Priority") },
                    text = {
                        Column {
                            val priorities = listOf("Low", "Medium", "High")
                            priorities.forEach { priority ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = defaultPriority == priority,
                                        onClick = { 
                                            defaultPriority = priority
                                            expanded = false
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(priority)
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { expanded = false }) {
                            Text("Done")
                        }
                    }
                )
            }
        }
        
        item {
            Divider(modifier = Modifier.padding(vertical = 8.dp))
        }
        
        item {
            Divider(modifier = Modifier.padding(vertical = 8.dp))
        }
        
        item {
            Text(
                text = "Integrations",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        item {
            val scope = rememberCoroutineScope()
            SettingsItem(
                icon = Icons.Default.School,
                title = "Canvas LMS",
                subtitle = if (canvasConnected) "Connected" else "Not connected",
                trailing = {
                    Switch(
                        checked = canvasConnected,
                        onCheckedChange = { enabled ->
                            if (enabled && !canvasConnected) {
                                showCanvasDialog = true
                            } else if (!enabled) {
                                canvasConnected = false
                                viewModel?.setCanvasUrl("")
                                viewModel?.setCanvasToken("")
                            }
                        }
                    )
                }
            )
        }
        
        item {
            SettingsItem(
                icon = Icons.Default.Link,
                title = "Canvas URL",
                subtitle = "Set your school's Canvas URL",
                onClick = { /* TODO: Implement Canvas URL setup */ }
            )
        }
        
        item {
            SettingsItem(
                icon = Icons.Default.Sync,
                title = "Auto Sync",
                subtitle = "Automatically sync assignments from Canvas",
                trailing = {
                    Switch(
                        checked = autoSync,
                        onCheckedChange = { enabled ->
                            autoSync = enabled
                            viewModel?.setAutoSync(enabled)
                        }
                    )
                }
            )
        }
        
        item {
            Divider(modifier = Modifier.padding(vertical = 8.dp))
        }
        
        item {
            Text(
                text = "Data",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        item {
            SettingsItem(
                icon = Icons.Default.CloudUpload,
                title = "Export Data",
                subtitle = "Export your assignments to a file",
                onClick = { /* TODO: Implement export */ }
            )
        }
        
        item {
            SettingsItem(
                icon = Icons.Default.CloudDownload,
                title = "Import Data",
                subtitle = "Import assignments from a file",
                onClick = { /* TODO: Implement import */ }
            )
        }
        
        item {
            SettingsItem(
                icon = Icons.Default.DeleteSweep,
                title = "Clear All Completed",
                subtitle = "Remove all completed assignments",
                onClick = { /* TODO: Implement clear completed */ }
            )
        }
        
        item {
            Divider(modifier = Modifier.padding(vertical = 8.dp))
        }
        
        item {
            Text(
                text = "About",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        item {
            SettingsItem(
                icon = Icons.Default.Info,
                title = "Version",
                subtitle = "StudyFlow v1.0",
                onClick = { }
            )
        }
        
        item {
            SettingsItem(
                icon = Icons.Default.Help,
                title = "Help & Support",
                subtitle = "Get help with using StudyFlow",
                onClick = { /* TODO: Implement help */ }
            )
        }
        
        item {
            SettingsItem(
                icon = Icons.Default.Star,
                title = "Rate App",
                subtitle = "Rate StudyFlow on the Play Store",
                onClick = { /* TODO: Implement rating */ }
            )
        }
    }
    
    // Canvas Setup Dialog
    if (showCanvasDialog) {
        val scope = rememberCoroutineScope()
        AlertDialog(
            onDismissRequest = { showCanvasDialog = false },
            title = { Text("Setup Canvas Integration") },
            text = {
                Column {
                    OutlinedTextField(
                        value = canvasUrl,
                        onValueChange = { canvasUrl = it },
                        label = { Text("Canvas URL") },
                        placeholder = { Text("https://school.instructure.com") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = canvasToken,
                        onValueChange = { canvasToken = it },
                        label = { Text("API Token (Optional)") },
                        placeholder = { Text("Your Canvas API token") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "API token is optional but recommended for full sync functionality.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            if (canvasUrl.isNotBlank()) {
                                viewModel?.setCanvasUrl(canvasUrl)
                                viewModel?.setCanvasToken(canvasToken)
                                val connected = viewModel?.testCanvasConnection() ?: false
                                canvasConnected = connected
                                showCanvasDialog = false
                                
                                if (connected) {
                                    // Trigger initial sync
                                    viewModel?.refreshData()
                                }
                            }
                        }
                    }
                ) {
                    Text("Connect")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCanvasDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Card(
        onClick = onClick ?: {},
        enabled = onClick != null,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (trailing != null) {
                trailing()
            } else if (onClick != null) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
