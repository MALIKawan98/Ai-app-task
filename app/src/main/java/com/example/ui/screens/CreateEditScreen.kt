package com.example.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.TimeToLeave
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.model.Category
import com.example.model.Reminder
import com.example.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditScreen(
    navController: NavController,
    viewModel: MainViewModel,
    reminderId: Long?
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val allReminders by viewModel.allReminders.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()

    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var dateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    // set default time to next hour
    val cal = Calendar.getInstance()
    cal.add(Calendar.HOUR_OF_DAY, 1)
    cal.set(Calendar.MINUTE, 0)
    var timeMillis by remember { mutableStateOf(cal.timeInMillis) }
    
    var showCategoryDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val isEdit = reminderId != null
    var existingReminder by remember { mutableStateOf<Reminder?>(null) }

    LaunchedEffect(reminderId, allReminders, categories) {
        if (isEdit && existingReminder == null && allReminders.isNotEmpty() && categories.isNotEmpty()) {
            val reminder = allReminders.find { it.id == reminderId }
            if (reminder != null) {
                existingReminder = reminder
                title = reminder.title
                notes = reminder.notes
                selectedCategory = categories.find { it.id == reminder.categoryId }
                dateMillis = reminder.dateMillis
                
                val timeCal = Calendar.getInstance().apply { timeInMillis = reminder.timeMillis }
                val dateCal = Calendar.getInstance().apply { timeInMillis = reminder.dateMillis }
                dateCal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY))
                dateCal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE))
                timeMillis = dateCal.timeInMillis
            }
        } else if (!isEdit && selectedCategory == null && categories.isNotEmpty()) {
            selectedCategory = categories.first()
        }
    }

    val timeCalStr = remember(timeMillis) {
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(timeMillis))
    }
    val dateCalStr = remember(dateMillis) {
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(dateMillis))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "Edit Reminder" else "New Reminder") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isEdit) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                if (title.isBlank()) {
                    Toast.makeText(context, "Title is required", Toast.LENGTH_SHORT).show()
                    return@FloatingActionButton
                }
                if (selectedCategory == null) {
                    Toast.makeText(context, "Category is required", Toast.LENGTH_SHORT).show()
                    return@FloatingActionButton
                }
                val reminder = Reminder(
                    id = existingReminder?.id ?: 0L,
                    title = title.trim(),
                    notes = notes.trim(),
                    categoryId = selectedCategory!!.id,
                    dateMillis = dateMillis,
                    timeMillis = timeMillis,
                    isCompleted = existingReminder?.isCompleted ?: false,
                    createdAt = existingReminder?.createdAt ?: System.currentTimeMillis()
                )
                viewModel.saveReminder(reminder, isEdit = isEdit)
                navController.popBackStack()
            }) {
                Text(if (isEdit) "Save" else "Create", modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                singleLine = true
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (optional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                maxLines = 5
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showCategoryDialog = true },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Category, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(16.dp))
                    Text(
                        text = selectedCategory?.name ?: "Select Category",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            val c = Calendar.getInstance().apply { timeInMillis = dateMillis }
                            DatePickerDialog(
                                context,
                                { _, y, m, d ->
                                    val newDate = Calendar.getInstance().apply {
                                        set(y, m, d, 0, 0, 0)
                                    }
                                    dateMillis = newDate.timeInMillis
                                },
                                c.get(Calendar.YEAR),
                                c.get(Calendar.MONTH),
                                c.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                        Spacer(Modifier.width(8.dp))
                        Text(dateCalStr, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            val c = Calendar.getInstance().apply { timeInMillis = timeMillis }
                            TimePickerDialog(
                                context,
                                { _, h, m ->
                                    val newTime = Calendar.getInstance().apply {
                                        timeInMillis = dateMillis
                                        set(Calendar.HOUR_OF_DAY, h)
                                        set(Calendar.MINUTE, m)
                                    }
                                    timeMillis = newTime.timeInMillis
                                },
                                c.get(Calendar.HOUR_OF_DAY),
                                c.get(Calendar.MINUTE),
                                false
                            ).show()
                        },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                        Spacer(Modifier.width(8.dp))
                        Text(timeCalStr, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }

    if (showCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showCategoryDialog = false },
            title = { Text("Select Category") },
            text = {
                LazyColumn {
                    items(categories) { cat ->
                        Text(
                            text = cat.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedCategory = cat
                                    showCategoryDialog = false
                                }
                                .padding(16.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCategoryDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Reminder") },
            text = { Text("Are you sure you want to delete this reminder?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        existingReminder?.let { viewModel.deleteReminder(it) }
                        showDeleteDialog = false
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
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
