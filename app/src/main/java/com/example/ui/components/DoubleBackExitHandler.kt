package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import android.app.Activity

@Composable
fun DoubleBackExitHandler(onExit: () -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    var backPressedTime by remember { mutableStateOf(0L) }
    val context = LocalContext.current
    
    BackHandler {
        val currentTime = System.currentTimeMillis()
        if (currentTime - backPressedTime < 2000) {
            showDialog = true
        } else {
            backPressedTime = currentTime
            Toast.makeText(context, "Press back again to exit", Toast.LENGTH_SHORT).show()
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Exit Application") },
            text = { Text("Are you sure you want to exit Simple Reminder?") },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    onExit()
                    (context as? Activity)?.finish()
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("No")
                }
            }
        )
    }
}
