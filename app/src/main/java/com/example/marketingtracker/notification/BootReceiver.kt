package com.example.marketingtracker.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            Log.d("BootReceiver", "Device reboot detected. Re-arming marketing reminder notifications.")
            NotificationScheduler.scheduleNextNotification(context)
        }
    }
}
