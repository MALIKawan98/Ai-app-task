package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.model.ThemeMode
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: MainViewModel
) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    var showCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            SettingsCategory("Appearance")
            
            ListItem(
                headlineContent = { Text("Theme") },
                supportingContent = { Text(themeMode.name.lowercase().replaceFirstChar { it.uppercase() }) },
                leadingContent = {
                    Icon(
                        if (themeMode == ThemeMode.DARK) Icons.Default.DarkMode else if (themeMode == ThemeMode.LIGHT) Icons.Default.LightMode else Icons.Default.SettingsSuggest,
                        contentDescription = "Theme"
                    )
                },
                modifier = Modifier.clickable { 
                    val nextMode = when(themeMode) {
                        ThemeMode.SYSTEM -> ThemeMode.LIGHT
                        ThemeMode.LIGHT -> ThemeMode.DARK
                        ThemeMode.DARK -> ThemeMode.SYSTEM
                    }
                    viewModel.setThemeMode(nextMode)
                }
            )
            
            Divider()
            SettingsCategory("Categories")
            
            categories.forEach { cat ->
                ListItem(
                    headlineContent = { Text(cat.name) },
                    supportingContent = { if (cat.isSystem) Text("Default Category") else Text("Custom Category") }
                )
            }
            
            ListItem(
                headlineContent = { Text("Add Category", color = MaterialTheme.colorScheme.primary) },
                leadingContent = { Icon(Icons.Default.Add, contentDescription = "Add", tint = MaterialTheme.colorScheme.primary) },
                modifier = Modifier.clickable { showCategoryDialog = true }
            )
            
            Divider()
            SettingsCategory("About")
            ListItem(
                headlineContent = { Text("Version") },
                supportingContent = { Text("1.0.0") },
                leadingContent = { Icon(Icons.Default.Info, contentDescription = "Info") }
            )
        }
    }
    
    if (showCategoryDialog) {
        AlertDialog(
            onDismissRequest = { 
                showCategoryDialog = false
                newCategoryName = ""
            },
            title = { Text("New Category") },
            text = {
                OutlinedTextField(
                    value = newCategoryName,
                    onValueChange = { newCategoryName = it },
                    label = { Text("Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newCategoryName.isNotBlank()) {
                        viewModel.addCategory(newCategoryName.trim())
                    }
                    showCategoryDialog = false
                    newCategoryName = ""
                }) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showCategoryDialog = false
                    newCategoryName = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SettingsCategory(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp, end = 16.dp)
    )
}
