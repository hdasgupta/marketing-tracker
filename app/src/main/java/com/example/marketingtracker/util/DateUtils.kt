package com.example.marketingtracker.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {

    private val ISO_DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val DISPLAY_DATE_FORMAT = SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault())
    private val SHORT_DATE_FORMAT = SimpleDateFormat("MMM d", Locale.getDefault())

    fun getTodayDateString(): String {
        return ISO_DATE_FORMAT.format(Date())
    }

    fun getTomorrowDateString(): String {
        val calendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
        }
        return ISO_DATE_FORMAT.format(calendar.time)
    }

    fun formatDate(calendar: Calendar): String {
        return ISO_DATE_FORMAT.format(calendar.time)
    }

    fun parseDate(dateString: String): Calendar {
        val calendar = Calendar.getInstance()
        try {
            val date = ISO_DATE_FORMAT.parse(dateString)
            if (date != null) {
                calendar.time = date
            }
        } catch (e: Exception) {
            // Keep current calendar
        }
        return calendar
    }

    fun addDays(dateString: String, days: Int): String {
        val calendar = parseDate(dateString)
        calendar.add(Calendar.DAY_OF_YEAR, days)
        return ISO_DATE_FORMAT.format(calendar.time)
    }

    fun formatDisplayDate(dateString: String): String {
        val today = getTodayDateString()
        val tomorrow = getTomorrowDateString()
        val yesterday = addDays(today, -1)

        return when (dateString) {
            today -> "Today (${SHORT_DATE_FORMAT.format(Date())})"
            tomorrow -> "Tomorrow (${SHORT_DATE_FORMAT.format(parseDate(tomorrow).time)})"
            yesterday -> "Yesterday (${SHORT_DATE_FORMAT.format(parseDate(yesterday).time)})"
            else -> {
                val calendar = parseDate(dateString)
                DISPLAY_DATE_FORMAT.format(calendar.time)
            }
        }
    }

    fun isSunday(calendar: Calendar): Boolean {
        return calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
    }

    fun isSunday(dateString: String): Boolean {
        val cal = parseDate(dateString)
        return isSunday(cal)
    }
}
