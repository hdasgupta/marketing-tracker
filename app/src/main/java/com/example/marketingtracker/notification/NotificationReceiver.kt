package com.example.marketingtracker.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.marketingtracker.MarketingApplication
import com.example.marketingtracker.R
import com.example.marketingtracker.model.NotificationLog
import com.example.marketingtracker.ui.MainActivity
import com.example.marketingtracker.util.DateUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "NotificationReceiver"
        const val CHANNEL_ID = "marketing_reminder_channel"
        const val CHANNEL_NAME = "Marketing Reminders"
        private const val ONE_WEEK_MILLIS = 7 * 24 * 60 * 60 * 1000L
    }

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val isTest = intent.getBooleanExtra("EXTRA_IS_TEST", false)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val app = context.applicationContext as MarketingApplication
                val dao = app.database.marketingDao()
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                createNotificationChannel(notificationManager)

                // 1. Requirement: "Auto remove 1 week's old notifications"
                val cutoff = System.currentTimeMillis() - ONE_WEEK_MILLIS
                val oldLogs = dao.getLogsOlderThan(cutoff)
                for (oldLog in oldLogs) {
                    notificationManager.cancel(oldLog.notificationId)
                    Log.d(TAG, "Cancelled expired notification ID: ${oldLog.notificationId} from ${oldLog.date}")
                }
                val deletedCount = dao.deleteLogsOlderThan(cutoff)
                if (deletedCount > 0) {
                    Log.d(TAG, "Purged $deletedCount old notification logs from database")
                }

                // 2. Query today's unbought items
                // Requirement: "Already buyed items will be removed from notifications."
                val todayDate = DateUtils.getTodayDateString()
                val unboughtItems = dao.getUnboughtItemsForDate(todayDate)

                // 3. Requirement: "If no item for a day no notification will be triggered."
                if (unboughtItems.isEmpty()) {
                    Log.d(TAG, "No unbought items for today ($todayDate). Skipping notification.")
                    if (isTest) {
                        Handler(Looper.getMainLooper()).post {
                            Toast.makeText(
                                context,
                                "No unbought marketing items for today ($todayDate)!",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                } else {
                    // 4. Requirement: "In notification today's item to buy and quantity will be shown."
                    val itemCount = unboughtItems.size
                    val title = "Items to Buy Today ($itemCount)"

                    val bigTextBuilder = StringBuilder()
                    bigTextBuilder.append("Marketing list for today:
")
                    for (item in unboughtItems) {
                        bigTextBuilder.append("• ${item.name} — ${item.formattedQuantity} ${item.unit}
")
                    }
                    val fullMessage = bigTextBuilder.toString().trimEnd()

                    val summaryText = unboughtItems.take(3).joinToString(", ") { "${it.name} (${it.formattedQuantity} ${it.unit})" } +
                            if (unboughtItems.size > 3) " and ${unboughtItems.size - 3} more" else ""

                    // Open MainActivity on tap
                    val tapIntent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra("EXTRA_TARGET_DATE", todayDate)
                    }
                    val tapPendingIntent = PendingIntent.getActivity(
                        context,
                        0,
                        tapIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    // Unique notification ID per date
                    val notificationId = todayDate.hashCode()

                    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_shopping_cart)
                        .setContentTitle(title)
                        .setContentText(summaryText)
                        .setStyle(
                            NotificationCompat.BigTextStyle()
                                .bigText(fullMessage)
                                .setBigContentTitle(title)
                                .setSummaryText("$itemCount pending items")
                        )
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(tapPendingIntent)
                        .setAutoCancel(true)
                        .build()

                    notificationManager.notify(notificationId, notification)

                    // Log notification in DB for auto-removal after 1 week
                    dao.insertNotificationLog(
                        NotificationLog(
                            notificationId = notificationId,
                            title = title,
                            message = fullMessage,
                            timestamp = System.currentTimeMillis(),
                            date = todayDate
                        )
                    )

                    // 5. Requirement: "New notifications will come as toast message."
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(
                            context,
                            "Marketing Alert: $itemCount items to buy today!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                // Reschedule next notification (unless this was just a manual test)
                if (!isTest) {
                    NotificationScheduler.scheduleNextNotification(context)
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error processing marketing notification: ${e.message}", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val existing = notificationManager.getNotificationChannel(CHANNEL_ID)
            if (existing == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Reminders for scheduled marketing items to buy"
                    enableLights(true)
                    lightColor = Color.GREEN
                    enableVibration(true)
                }
                notificationManager.createNotificationChannel(channel)
            }
        }
    }
}
