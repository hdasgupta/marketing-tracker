package com.example.marketingtracker.data

import com.example.marketingtracker.model.Category
import com.example.marketingtracker.model.Holiday
import com.example.marketingtracker.model.MarketingItem
import com.example.marketingtracker.model.UnitEntity
import kotlinx.coroutines.flow.Flow

class MarketingRepository(private val dao: MarketingDao) {

    fun getItemsForDate(date: String): Flow<List<MarketingItem>> =
        dao.getItemsForDate(date)

    suspend fun getUnboughtItemsForDate(date: String): List<MarketingItem> =
        dao.getUnboughtItemsForDate(date)

    fun getAllDistinctItemNames(): Flow<List<String>> =
        dao.getAllDistinctItemNamesFlow()

    suspend fun getAllDistinctItemNamesSync(): List<String> =
        dao.getAllDistinctItemNamesSync()

    suspend fun insertItem(item: MarketingItem): Long =
        dao.insertItem(item)

    suspend fun updateItem(item: MarketingItem) =
        dao.updateItem(item)

    suspend fun deleteItem(item: MarketingItem) =
        dao.deleteItem(item)

    suspend fun deleteItemById(id: Long) =
        dao.deleteItemById(id)

    suspend fun shiftItemDate(itemId: Long, newDate: String) =
        dao.shiftItemDate(itemId, newDate)

    suspend fun shiftAllUnboughtItems(oldDate: String, newDate: String): Int =
        dao.shiftAllUnboughtItems(oldDate, newDate)

    suspend fun markItemBought(itemId: Long, isBought: Boolean) {
        val boughtAt = if (isBought) System.currentTimeMillis() else null
        dao.markItemBought(itemId, isBought, boughtAt)
    }

    // Categories
    fun getAllCategories(): Flow<List<Category>> =
        dao.getAllCategories()

    suspend fun getAllCategoriesSync(): List<Category> =
        dao.getAllCategoriesSync()

    suspend fun insertCategory(category: Category): Long =
        dao.insertCategory(category)

    suspend fun deleteCategory(category: Category) =
        dao.deleteCategory(category)

    // Units
    fun getAllUnits(): Flow<List<UnitEntity>> =
        dao.getAllUnits()

    suspend fun getAllUnitsSync(): List<UnitEntity> =
        dao.getAllUnitsSync()

    suspend fun insertUnit(unit: UnitEntity): Long =
        dao.insertUnit(unit)

    suspend fun deleteUnit(unit: UnitEntity) =
        dao.deleteUnit(unit)

    // Holidays
    fun getAllHolidays(): Flow<List<Holiday>> =
        dao.getAllHolidays()

    suspend fun getAllHolidaysSync(): List<Holiday> =
        dao.getAllHolidaysSync()

    suspend fun insertHoliday(holiday: Holiday): Long =
        dao.insertHoliday(holiday)

    suspend fun deleteHoliday(holiday: Holiday) =
        dao.deleteHoliday(holiday)

    suspend fun isHoliday(date: String): Boolean {
        val parts = date.split("-")
        val monthDay = if (parts.size >= 3) "${parts[1]}-${parts[2]}" else date
        val holidays = dao.findHolidaysForDate(date, monthDay)
        return holidays.isNotEmpty()
    }
}
