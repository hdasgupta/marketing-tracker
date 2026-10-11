package com.example.marketingtracker.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "holidays",
    indices = [Index("date")]
)
data class Holiday(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // YYYY-MM-DD or MM-DD for recurring
    val name: String,
    val isRecurringYearly: Boolean = true
)
