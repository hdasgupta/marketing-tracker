package com.example.marketingtracker.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "units",
    indices = [Index(value = ["name"], unique = true), Index(value = ["symbol"], unique = true)]
)
data class UnitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val symbol: String
)
