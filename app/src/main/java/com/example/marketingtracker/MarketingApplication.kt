package com.example.marketingtracker

import android.app.Application
import com.example.marketingtracker.data.AppDatabase
import com.example.marketingtracker.data.MarketingRepository
import com.example.marketingtracker.notification.NotificationScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class MarketingApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy { AppDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { MarketingRepository(database.marketingDao()) }

    override fun onCreate() {
        super.onCreate()
        // Initialize notifications on app launch
        NotificationScheduler.scheduleNextNotification(this)
    }
}
