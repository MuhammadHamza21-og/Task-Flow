package com.example.data.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.local.entity.TaskEntity
import java.util.Calendar

/**
 * TaskAlarmScheduler handles scheduling and canceling alarms for task due-date notifications.
 */
class TaskAlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /**
     * Schedules an alarm reminder for the given task if it has a reminder enabled
     * and a valid future due date.
     */
    fun schedule(task: TaskEntity) {
        if (!task.hasReminder || task.dueDate == null) {
            cancel(task)
            return
        }

        val triggerTime = computeTriggerTime(task)
        // If trigger time has already passed, don't schedule an alarm
        if (triggerTime <= System.currentTimeMillis()) {
            return
        }

        val intent = Intent(context, TaskReminderReceiver::class.java).apply {
            putExtra(TaskReminderReceiver.EXTRA_TASK_ID, task.id)
            putExtra(TaskReminderReceiver.EXTRA_TASK_TITLE, task.title)
            putExtra(TaskReminderReceiver.EXTRA_TASK_DESC, task.description)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
            Log.d("TaskAlarmScheduler", "Alarm scheduled for task ${task.id} at $triggerTime")
        } catch (e: SecurityException) {
            Log.w("TaskAlarmScheduler", "Exact alarm permission not granted, falling back", e)
            try {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } catch (ex: Exception) {
                Log.e("TaskAlarmScheduler", "Failed to schedule alarm", ex)
            }
        }
    }

    /**
     * Cancels an existing alarm for the specified task ID.
     */
    fun cancel(task: TaskEntity) {
        val intent = Intent(context, TaskReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d("TaskAlarmScheduler", "Alarm cancelled for task ${task.id}")
        }
    }

    /**
     * Calculates the millisecond epoch timestamp combining dueDate and dueTime.
     */
    private fun computeTriggerTime(task: TaskEntity): Long {
        val calendar = Calendar.getInstance()
        task.dueDate?.let { calendar.timeInMillis = it }

        val hour = task.dueTimeHour ?: 9 // Default reminder at 9:00 AM
        val minute = task.dueTimeMinute ?: 0

        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        return calendar.timeInMillis
    }
}
