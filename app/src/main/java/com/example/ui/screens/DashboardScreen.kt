package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

import androidx.navigation.NavController
import com.example.model.Reminder
import com.example.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: MainViewModel,
    onExit: () -> Unit
) {
    val upcomings by viewModel.upcomingReminders.collectAsStateWithLifecycle()
    val completeds by viewModel.completedReminders.collectAsStateWithLifecycle()
    val allReminders by viewModel.allReminders.collectAsStateWithLifecycle()
    
    com.example.ui.components.DoubleBackExitHandler(onExit)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Simple Reminder", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { navController.navigate("search") }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("create") }) {
                Icon(Icons.Default.Add, contentDescription = "Add Reminder")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                DashboardStats(
                    total = allReminders.size,
                    today = upcomings.count { isToday(it.dateMillis) },
                    upcoming = upcomings.size,
                    completed = completeds.size,
                    navController = navController
                )
            }

            item {
                Text(
                    "Upcoming",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (upcomings.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No upcoming reminders. Enjoy your day!", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                items(upcomings, key = { it.id }) { reminder ->
                    ReminderItem(
                        reminder = reminder,
                        category = viewModel.categories.collectAsStateWithLifecycle().value.find { it.id == reminder.categoryId },
                        onCheck = { viewModel.toggleReminderCompletion(it) },
                        onClick = { navController.navigate("edit/${reminder.id}") }
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardStats(total: Int, today: Int, upcoming: Int, completed: Int, navController: NavController) {
    val isDark = isSystemInDarkTheme()
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            title = "Total",
            value = total.toString(),
            icon = Icons.Outlined.TaskAlt,
            bgColor = if (isDark) com.example.ui.theme.DarkTotalCardBg else com.example.ui.theme.TotalCardBg,
            contentColor = if (isDark) com.example.ui.theme.TotalCardBg else com.example.ui.theme.TotalCardText,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "Today",
            value = today.toString(),
            icon = Icons.Default.Search, // Using Search as placeholder or maybe a better one? Let's fix icon import if needed. We can just use Circle for now if today is missing
            bgColor = if (isDark) com.example.ui.theme.DarkTodayCardBg else com.example.ui.theme.TodayCardBg,
            contentColor = if (isDark) com.example.ui.theme.TodayCardBg else com.example.ui.theme.TodayCardText,
            modifier = Modifier.weight(1f)
        )
    }
    Spacer(Modifier.height(12.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            title = "Upcoming",
            value = upcoming.toString(),
            icon = Icons.Default.Add, // Placeholder icon
            bgColor = if (isDark) com.example.ui.theme.DarkUpcomingCardBg else com.example.ui.theme.UpcomingCardBg,
            contentColor = if (isDark) com.example.ui.theme.UpcomingCardBg else com.example.ui.theme.UpcomingCardText,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "Done",
            value = completed.toString(),
            icon = Icons.Filled.CheckCircle,
            bgColor = if (isDark) com.example.ui.theme.DarkDoneCardBg else com.example.ui.theme.DoneCardBg,
            contentColor = if (isDark) com.example.ui.theme.DoneCardBg else com.example.ui.theme.DoneCardText,
            modifier = Modifier.weight(1f),
            onClick = { navController.navigate("completed") }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatCard(title: String, value: String, icon: ImageVector, bgColor: Color, contentColor: Color, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor, contentColor = contentColor),
        onClick = { onClick?.invoke() },
        enabled = onClick != null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = contentColor)
                Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = contentColor)
            }
            Text(title.uppercase(), style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp), color = contentColor.copy(alpha = 0.7f), fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun ReminderItem(reminder: Reminder, category: com.example.model.Category?, onCheck: (Reminder) -> Unit, onClick: () -> Unit) {
    val isDark = isSystemInDarkTheme()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon/Circle
            val catName = category?.name?.lowercase() ?: ""
            val (iconBg, iconTint) = when {
                catName.contains("work") -> (if (isDark) com.example.ui.theme.DarkCategoryWorkBg else com.example.ui.theme.CategoryWorkBg) to (if (isDark) com.example.ui.theme.CategoryWorkBg else com.example.ui.theme.CategoryWorkText)
                catName.contains("health") -> (if (isDark) com.example.ui.theme.DarkCategoryHealthBg else com.example.ui.theme.CategoryHealthBg) to (if (isDark) com.example.ui.theme.CategoryHealthBg else com.example.ui.theme.CategoryHealthText)
                catName.contains("study") -> (if (isDark) com.example.ui.theme.DarkCategoryStudyBg else com.example.ui.theme.CategoryStudyBg) to (if (isDark) com.example.ui.theme.CategoryStudyBg else com.example.ui.theme.CategoryStudyText)
                else -> (if (isDark) com.example.ui.theme.DarkCategoryPersonalBg else com.example.ui.theme.CategoryPersonalBg) to (if (isDark) com.example.ui.theme.CategoryPersonalBg else com.example.ui.theme.CategoryPersonalText)
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = { onCheck(reminder) }) {
                   Icon(
                       imageVector = if (reminder.isCompleted) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                       contentDescription = "Complete",
                       tint = if (reminder.isCompleted) iconTint else iconTint.copy(alpha = 0.5f)
                   )
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reminder.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = formatDateTime(reminder.dateMillis, reminder.timeMillis),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
            if (category != null) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = category.name.uppercase(),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

fun isToday(dateMillis: Long): Boolean {
    val cal1 = Calendar.getInstance()
    val cal2 = Calendar.getInstance().apply { timeInMillis = dateMillis }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
           cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

fun formatDateTime(dateMillis: Long, timeMillis: Long): String {
    val date = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(dateMillis))
    val time = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(timeMillis))
    return "$date at $time"
}
