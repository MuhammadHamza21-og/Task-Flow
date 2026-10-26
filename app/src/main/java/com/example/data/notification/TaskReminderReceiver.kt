package com.example.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R

/**
 * TaskReminderReceiver triggers local user notifications when a scheduled task alarm fires.
 */
class TaskReminderReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "taskflow_reminders_channel"
        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_TASK_TITLE = "extra_task_title"
        const val EXTRA_TASK_DESC = "extra_task_desc"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, 0L)
        val title = intent.getStringExtra(EXTRA_TASK_TITLE) ?: context.getString(R.string.reminder_notification_channel)
        val desc = intent.getStringExtra(EXTRA_TASK_DESC) ?: ""

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel for Android 8.0 (Oreo) and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = context.getString(R.string.reminder_notification_channel)
            val channelDescription = context.getString(R.string.reminder_notification_channel_desc)
            val importance = NotificationManager.IMPORTANCE_HIGH

            val channel = NotificationChannel(CHANNEL_ID, channelName, importance).apply {
                description = channelDescription
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Tap action: open MainActivity
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val contentPendingIntent = PendingIntent.getActivity(
            context,
            taskId.toInt(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Task Due: $title")
            .setContentText(desc.ifBlank { "You have a task scheduled for now." })
            .setStyle(NotificationCompat.BigTextStyle().bigText(desc.ifBlank { "You have a task scheduled for now." }))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        // Check POST_NOTIFICATIONS permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                notificationManager.notify(taskId.toInt(), notification)
            }
        } else {
            notificationManager.notify(taskId.toInt(), notification)
        }
    }
}
