package com.example.marketingtracker.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.marketingtracker.MarketingApplication
import com.example.marketingtracker.util.DateUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

object NotificationScheduler {

    private const val TAG = "NotificationScheduler"
    const val ACTION_MARKETING_ALARM = "com.example.marketingtracker.ACTION_MARKETING_ALARM"
    private const val ALARM_REQUEST_CODE = 1001

    fun scheduleNextNotification(context: Context) {
        val app = context.applicationContext as MarketingApplication
        CoroutineScope(Dispatchers.IO).launch {
            val holidayManager = HolidayManager(context)
            val now = Calendar.getInstance()

            // 1. Determine if today's notification time has passed
            val isTodaySundayOrHoliday = holidayManager.isSundayOrHoliday(now)
            val todayTargetHour = if (isTodaySundayOrHoliday) 7 else 17 // 7 AM or 5 PM (17:00)

            val todayAlarm = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, todayTargetHour)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val targetCalendar: Calendar
            if (now.before(todayAlarm)) {
                // Today's alarm is still in the future!
                targetCalendar = todayAlarm
            } else {
                // Today's alarm has passed, calculate tomorrow's alarm
                val tomorrow = (now.clone() as Calendar).apply {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
                val isTomorrowSundayOrHoliday = holidayManager.isSundayOrHoliday(tomorrow)
                val tomorrowTargetHour = if (isTomorrowSundayOrHoliday) 7 else 17

                targetCalendar = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, 1)
                    set(Calendar.HOUR_OF_DAY, tomorrowTargetHour)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
            }

            val triggerMillis = targetCalendar.timeInMillis
            Log.d(TAG, "Scheduled next marketing reminder at: ${targetCalendar.time} (Hour: ${targetCalendar.get(Calendar.HOUR_OF_DAY)}:00)")

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, NotificationReceiver::class.java).apply {
                action = ACTION_MARKETING_ALARM
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                ALARM_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerMillis,
                            pendingIntent
                        )
                    } else {
                        alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerMillis,
                            pendingIntent
                        )
                    }
                } else {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerMillis,
                        pendingIntent
                    )
                }
            } catch (e: SecurityException) {
                Log.e(TAG, "SecurityException while scheduling exact alarm: ${e.message}")
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
            }
        }
    }

    /**
     * Manually triggers the notification receiver immediately for live testing
     */
    fun triggerImmediateCheck(context: Context) {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = ACTION_MARKETING_ALARM
            putExtra("EXTRA_IS_TEST", true)
        }
        context.sendBroadcast(intent)
    }
}
