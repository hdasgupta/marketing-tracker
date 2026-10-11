package com.example.marketingtracker.notification

import android.content.Context
import com.example.marketingtracker.MarketingApplication
import com.example.marketingtracker.util.DateUtils
import java.util.Calendar

class HolidayManager(private val context: Context) {

    suspend fun isSundayOrHoliday(calendar: Calendar): Boolean {
        if (DateUtils.isSunday(calendar)) {
            return true
        }

        val dateString = DateUtils.formatDate(calendar)
        val app = context.applicationContext as MarketingApplication
        return app.repository.isHoliday(dateString)
    }
}
