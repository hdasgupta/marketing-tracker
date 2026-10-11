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
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun marketingDao(): MarketingDao

    suspend fun ensureDefaultsPopulated() {
        val dao = marketingDao()
        if (dao.getCategoryCount() == 0) {
            getDefaultCategories().forEach { dao.insertCategory(it) }
        }
        if (dao.getUnitCount() == 0) {
            getDefaultUnits().forEach { dao.insertUnit(it) }
        }
        if (dao.getHolidayCount() == 0) {
            getDefaultHolidays().forEach { dao.insertHoliday(it) }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDefaultCategories(): List<Category> = listOf(
            Category(name = "Medicines", colorHex = "#EF4444", isDefault = true),
            Category(name = "Vegetables", colorHex = "#10B981", isDefault = true),
            Category(name = "Groceries", colorHex = "#8B5CF6", isDefault = true),
            Category(name = "Diary and Bakery", colorHex = "#F59E0B", isDefault = true),
            Category(name = "Rice", colorHex = "#0D9488", isDefault = true),
            Category(name = "Poultry", colorHex = "#F97316", isDefault = true),
            Category(name = "Meat and seafood", colorHex = "#DC2626", isDefault = true),
            Category(name = "Water", colorHex = "#06B6D4", isDefault = true),
            Category(name = "Others", colorHex = "#6B7280", isDefault = true)
        )

        fun getDefaultUnits(): List<UnitEntity> = listOf(
            UnitEntity(name = "Gram", symbol = "g"),
            UnitEntity(name = "Jar", symbol = "jar"),
            UnitEntity(name = "Kilo Gram", symbol = "kg"),
            UnitEntity(name = "Liter", symbol = "L"),
            UnitEntity(name = "Packet", symbol = "pkt"),
            UnitEntity(name = "Piece", symbol = "pc"),
            UnitEntity(name = "Plate", symbol = "plate"),
            UnitEntity(name = "pound", symbol = "lb"),
            UnitEntity(name = "Bottle", symbol = "btl"),
            UnitEntity(name = "Milli liter", symbol = "ml"),
            UnitEntity(name = "Thali", symbol = "thali")
        )

        fun getDefaultHolidays(): List<Holiday> = listOf(
            Holiday(date = "01-01", name = "New Year's Day", isRecurringYearly = true),
            Holiday(date = "01-26", name = "Republic Day", isRecurringYearly = true),
            Holiday(date = "05-01", name = "Labor Day", isRecurringYearly = true),
            Holiday(date = "08-15", name = "Independence Day", isRecurringYearly = true),
            Holiday(date = "10-02", name = "Gandhi Jayanti", isRecurringYearly = true),
            Holiday(date = "12-25", name = "Christmas Day", isRecurringYearly = true)
        )

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "marketing_tracker_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Synchronous database pre-population during table creation
                            getDefaultCategories().forEach {
                                db.execSQL("INSERT OR IGNORE INTO categories (name, colorHex, iconName, isDefault) VALUES ('${it.name}', '${it.colorHex}', '${it.iconName}', 1);")
                            }
                            getDefaultUnits().forEach {
                                db.execSQL("INSERT OR IGNORE INTO units (name, symbol) VALUES ('${it.name}', '${it.symbol}');")
                            }
                            getDefaultHolidays().forEach {
                                db.execSQL("INSERT OR IGNORE INTO holidays (date, name, isRecurringYearly) VALUES ('${it.date}', '${it.name}', 1);")
                            }
                        }
                    })
                    .build()
                INSTANCE = instance

                // Double check in coroutine scope
                scope.launch(Dispatchers.IO) {
                    instance.ensureDefaultsPopulated()
                }

                instance
            }
        }
    }
}
