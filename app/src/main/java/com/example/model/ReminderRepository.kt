package com.example.model

import kotlinx.coroutines.flow.Flow

class ReminderRepository(private val reminderDao: ReminderDao) {

    val upcomingReminders: Flow<List<Reminder>> = reminderDao.getUpcomingReminders()
    val completedReminders: Flow<List<Reminder>> = reminderDao.getCompletedReminders()
    val allReminders: Flow<List<Reminder>> = reminderDao.getAllReminders()
    val allCategories: Flow<List<Category>> = reminderDao.getAllCategories()

    fun getReminderById(id: Long): Flow<Reminder?> {
        return reminderDao.getReminderById(id)
    }
    
    fun searchReminders(query: String): Flow<List<Reminder>> {
        return reminderDao.searchReminders(query)
    }

    suspend fun insertReminder(reminder: Reminder): Long {
        return reminderDao.insertReminder(reminder)
    }

    suspend fun updateReminder(reminder: Reminder) {
        reminderDao.updateReminder(reminder)
    }

    suspend fun deleteReminderById(id: Long) {
        reminderDao.deleteReminderById(id)
    }

    suspend fun updateCompletionStatus(id: Long, isCompleted: Boolean) {
        reminderDao.updateCompletionStatus(id, isCompleted)
    }
    
    suspend fun initializeCategories() {
        if (reminderDao.getCategoryCount() == 0) {
            reminderDao.insertCategory(Category(name = "Personal", isSystem = true))
            reminderDao.insertCategory(Category(name = "Work", isSystem = true))
            reminderDao.insertCategory(Category(name = "Study", isSystem = true))
            reminderDao.insertCategory(Category(name = "Health", isSystem = true))
        }
    }

    suspend fun insertCategory(category: Category): Long {
        return reminderDao.insertCategory(category)
    }
}
