package com.example.marketingtracker.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "marketing_items",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("categoryId"),
        Index("date"),
        Index("isBought"),
        Index("name")
    ]
)
data class MarketingItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val categoryId: Long,
    val quantity: Double = 1.0,
    val unit: String = "pc",
    val date: String, // ISO format: YYYY-MM-DD
    val isBought: Boolean = false,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val boughtAt: Long? = null
) {
    val formattedQuantity: String
        get() = if (quantity % 1.0 == 0.0) {
            quantity.toInt().toString()
        } else {
            "%.2f".format(quantity).trimEnd('0').trimEnd('.')
        }
}
