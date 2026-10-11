package com.example.marketingtracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.marketingtracker.model.Category
import com.example.marketingtracker.model.Holiday
import com.example.marketingtracker.model.MarketingItem
import com.example.marketingtracker.model.NotificationLog
import com.example.marketingtracker.model.UnitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MarketingDao {

    // --- Marketing Items ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: MarketingItem): Long

    @Update
    suspend fun updateItem(item: MarketingItem)

    @Delete
    suspend fun deleteItem(item: MarketingItem)

    @Query("DELETE FROM marketing_items WHERE id = :id")
    suspend fun deleteItemById(id: Long)

    @Query("SELECT * FROM marketing_items WHERE date = :date ORDER BY isBought ASC, categoryId ASC, name ASC")
    fun getItemsForDate(date: String): Flow<List<MarketingItem>>

    @Query("SELECT * FROM marketing_items WHERE date = :date AND isBought = 0 ORDER BY categoryId ASC, name ASC")
    suspend fun getUnboughtItemsForDate(date: String): List<MarketingItem>

    @Query("SELECT * FROM marketing_items WHERE date = :date AND isBought = 0")
    fun getUnboughtItemsForDateFlow(date: String): Flow<List<MarketingItem>>

    @Query("SELECT DISTINCT name FROM marketing_items WHERE name LIKE '%' || :query || '%' ORDER BY name ASC LIMIT 20")
    suspend fun searchItemSuggestions(query: String): List<String>

    @Query("SELECT DISTINCT name FROM marketing_items ORDER BY name ASC")
    fun getAllDistinctItemNamesFlow(): Flow<List<String>>

    @Query("SELECT DISTINCT name FROM marketing_items ORDER BY name ASC")
    suspend fun getAllDistinctItemNamesSync(): List<String>

    @Query("UPDATE marketing_items SET date = :newDate WHERE id = :itemId")
    suspend fun shiftItemDate(itemId: Long, newDate: String)

    @Query("UPDATE marketing_items SET date = :newDate WHERE date = :oldDate AND isBought = 0")
    suspend fun shiftAllUnboughtItems(oldDate: String, newDate: String): Int

    @Query("UPDATE marketing_items SET isBought = :isBought, boughtAt = :boughtAt WHERE id = :itemId")
    suspend fun markItemBought(itemId: Long, isBought: Boolean, boughtAt: Long?)

    @Query("SELECT * FROM marketing_items WHERE id = :id")
    suspend fun getItemById(id: Long): MarketingItem?

    // --- Categories ---
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<Category>>

    @Query("SELECT * FROM categories ORDER BY name ASC")
    suspend fun getAllCategoriesSync(): List<Category>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategory(category: Category): Long

    @Delete
    suspend fun deleteCategory(category: Category)

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: Long): Category?

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCategoryCount(): Int

    // --- Units ---
    @Query("SELECT * FROM units ORDER BY name ASC")
    fun getAllUnits(): Flow<List<UnitEntity>>

    @Query("SELECT * FROM units ORDER BY name ASC")
    suspend fun getAllUnitsSync(): List<UnitEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUnit(unit: UnitEntity): Long

    @Delete
    suspend fun deleteUnit(unit: UnitEntity)

    @Query("SELECT COUNT(*) FROM units")
    suspend fun getUnitCount(): Int

    // --- Holidays ---
    @Query("SELECT * FROM holidays ORDER BY date ASC")
    fun getAllHolidays(): Flow<List<Holiday>>

    @Query("SELECT * FROM holidays ORDER BY date ASC")
    suspend fun getAllHolidaysSync(): List<Holiday>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHoliday(holiday: Holiday): Long

    @Delete
    suspend fun deleteHoliday(holiday: Holiday)

    @Query("SELECT * FROM holidays WHERE date = :date OR (isRecurringYearly = 1 AND date LIKE '%' || :monthDay)")
    suspend fun findHolidaysForDate(date: String, monthDay: String): List<Holiday>

    @Query("SELECT COUNT(*) FROM holidays")
    suspend fun getHolidayCount(): Int

    // --- Notification Logs ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotificationLog(log: NotificationLog): Long

    @Query("SELECT * FROM notification_logs WHERE timestamp < :cutoffTimestamp")
    suspend fun getLogsOlderThan(cutoffTimestamp: Long): List<NotificationLog>

    @Query("DELETE FROM notification_logs WHERE timestamp < :cutoffTimestamp")
    suspend fun deleteLogsOlderThan(cutoffTimestamp: Long): Int

    @Query("SELECT * FROM notification_logs ORDER BY timestamp DESC")
    fun getAllNotificationLogs(): Flow<List<NotificationLog>>
}
