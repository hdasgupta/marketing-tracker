package com.example.marketingtracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.marketingtracker.model.Category
import com.example.marketingtracker.model.Holiday
import com.example.marketingtracker.model.MarketingItem
import com.example.marketingtracker.model.NotificationLog
import com.example.marketingtracker.model.UnitEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        MarketingItem::class,
        Category::class,
        UnitEntity::class,
        Holiday::class,
        NotificationLog::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun marketingDao(): MarketingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "marketing_tracker_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.marketingDao())
                    }
                }
            }

            suspend fun populateInitialData(dao: MarketingDao) {
                // Default Categories
                val defaultCategories = listOf(
                    Category(name = "Vegetables", colorHex = "#10B981", isDefault = true),
                    Category(name = "Fruits", colorHex = "#F59E0B", isDefault = true),
                    Category(name = "Dairy & Eggs", colorHex = "#3B82F6", isDefault = true),
                    Category(name = "Groceries & Staples", colorHex = "#8B5CF6", isDefault = true),
                    Category(name = "Meat & Seafood", colorHex = "#EF4444", isDefault = true),
                    Category(name = "Bakery & Bread", colorHex = "#D97706", isDefault = true),
                    Category(name = "Beverages", colorHex = "#06B6D4", isDefault = true),
                    Category(name = "Snacks & Sweets", colorHex = "#EC4899", isDefault = true),
                    Category(name = "Spices & Condiments", colorHex = "#F97316", isDefault = true),
                    Category(name = "Household & Cleaning", colorHex = "#64748B", isDefault = true),
                    Category(name = "Personal Care", colorHex = "#14B8A6", isDefault = true),
                    Category(name = "Others", colorHex = "#6B7280", isDefault = true)
                )
                defaultCategories.forEach { dao.insertCategory(it) }

                // Default Units
                val defaultUnits = listOf(
                    UnitEntity(name = "Kilogram", symbol = "kg"),
                    UnitEntity(name = "Gram", symbol = "g"),
                    UnitEntity(name = "Litre", symbol = "L"),
                    UnitEntity(name = "Millilitre", symbol = "ml"),
                    UnitEntity(name = "Piece", symbol = "pc"),
                    UnitEntity(name = "Packet", symbol = "pkt"),
                    UnitEntity(name = "Dozen", symbol = "doz"),
                    UnitEntity(name = "Box", symbol = "box"),
                    UnitEntity(name = "Bottle", symbol = "btl"),
                    UnitEntity(name = "Bunch", symbol = "bunch"),
                    UnitEntity(name = "Can", symbol = "can")
                )
                defaultUnits.forEach { dao.insertUnit(it) }

                // Default Standard Holidays (Recurring MM-DD format)
                val defaultHolidays = listOf(
                    Holiday(date = "01-01", name = "New Year's Day", isRecurringYearly = true),
                    Holiday(date = "01-26", name = "Republic Day", isRecurringYearly = true),
                    Holiday(date = "05-01", name = "Labor Day", isRecurringYearly = true),
                    Holiday(date = "08-15", name = "Independence Day", isRecurringYearly = true),
                    Holiday(date = "10-02", name = "Gandhi Jayanti", isRecurringYearly = true),
                    Holiday(date = "12-25", name = "Christmas Day", isRecurringYearly = true)
                )
                defaultHolidays.forEach { dao.insertHoliday(it) }
            }
        }
    }
}
