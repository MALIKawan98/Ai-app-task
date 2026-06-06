package com.example.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.model.AppDatabase
import com.example.model.ReminderRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val database = AppDatabase.getDatabase(context)
            val repository = ReminderRepository(database.reminderDao())
            val alarmScheduler = AlarmScheduler(context)

            CoroutineScope(Dispatchers.IO).launch {
                val upcoming = repository.upcomingReminders.first()
                upcoming.forEach { reminder ->
                    alarmScheduler.scheduleAlarm(reminder)
                }
            }
        }
    }
}
