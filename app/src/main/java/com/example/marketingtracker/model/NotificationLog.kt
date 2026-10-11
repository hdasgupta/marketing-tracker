package com.example.marketingtracker.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notification_logs",
    indices = [Index("timestamp"), Index("date")]
)
data class NotificationLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val notificationId: Int,
    val title: String,
    val message: String,
    val timestamp: Long,
    val date: String
)
