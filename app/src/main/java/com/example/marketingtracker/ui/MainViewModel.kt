package com.example.marketingtracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.marketingtracker.data.MarketingRepository
import com.example.marketingtracker.model.Category
import com.example.marketingtracker.model.Holiday
import com.example.marketingtracker.model.MarketingItem
import com.example.marketingtracker.model.UnitEntity
import com.example.marketingtracker.util.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(private val repository: MarketingRepository) : ViewModel() {

    private val _selectedDate = MutableStateFlow(DateUtils.getTodayDateString())
    val selectedDate: StateFlow<String> = _selectedDate

    private val _selectedCategoryId = MutableStateFlow<Long?>(null)
    val selectedCategoryId: StateFlow<Long?> = _selectedCategoryId

    val categories: StateFlow<List<Category>> = repository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val units: StateFlow<List<UnitEntity>> = repository.getAllUnits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val holidays: StateFlow<List<Holiday>> = repository.getAllHolidays()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val itemSuggestions: StateFlow<List<String>> = repository.getAllDistinctItemNames()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val itemsForSelectedDate: StateFlow<List<MarketingItem>> = _selectedDate
        .flatMapLatest { date ->
            repository.getItemsForDate(date)
        }
        .combine(_selectedCategoryId) { items, categoryId ->
            if (categoryId == null) {
                items
            } else {
                items.filter { it.categoryId == categoryId }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSelectedDate(date: String) {
        _selectedDate.value = date
    }

    fun shiftDateByDays(days: Int) {
        _selectedDate.value = DateUtils.addDays(_selectedDate.value, days)
    }

    fun selectCategory(categoryId: Long?) {
        _selectedCategoryId.value = categoryId
    }

    fun toggleItemBought(item: MarketingItem) {
        viewModelScope.launch {
            repository.markItemBought(item.id, !item.isBought)
        }
    }

    fun insertItem(item: MarketingItem) {
        viewModelScope.launch {
            repository.insertItem(item)
        }
    }

    fun updateItem(item: MarketingItem) {
        viewModelScope.launch {
            repository.updateItem(item)
        }
    }

    fun deleteItem(item: MarketingItem) {
        viewModelScope.launch {
            repository.deleteItem(item)
        }
    }

    fun shiftItemDate(itemId: Long, newDate: String) {
        viewModelScope.launch {
            repository.shiftItemDate(itemId, newDate)
        }
    }

    fun shiftAllUnboughtItems(oldDate: String, newDate: String, onComplete: ((Int) -> Unit)? = null) {
        viewModelScope.launch {
            val count = repository.shiftAllUnboughtItems(oldDate, newDate)
            withContext(Dispatchers.Main) {
                onComplete?.invoke(count)
            }
        }
    }

    fun addCategory(name: String, colorHex: String = "#10B981", onCreated: ((Category) -> Unit)? = null) {
        viewModelScope.launch {
            val cat = Category(name = name, colorHex = colorHex)
            val newId = repository.insertCategory(cat)
            val created = cat.copy(id = newId)
            withContext(Dispatchers.Main) {
                onCreated?.invoke(created)
            }
        }
    }

    fun deleteCategory(category: Category, onComplete: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch {
            val count = repository.getAllCategoriesSync().size
            if (count <= 1) {
                withContext(Dispatchers.Main) {
                    onComplete?.invoke(false)
                }
                return@launch
            }
            repository.deleteCategory(category)
            if (_selectedCategoryId.value == category.id) {
                _selectedCategoryId.value = null
            }
            withContext(Dispatchers.Main) {
                onComplete?.invoke(true)
            }
        }
    }

    fun addUnit(name: String, symbol: String, onCreated: ((UnitEntity) -> Unit)? = null) {
        viewModelScope.launch {
            val u = UnitEntity(name = name, symbol = symbol)
            val newId = repository.insertUnit(u)
            val created = u.copy(id = newId)
            withContext(Dispatchers.Main) {
                onCreated?.invoke(created)
            }
        }
    }

    fun deleteUnit(unit: UnitEntity, onComplete: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch {
            val count = repository.getAllUnitsSync().size
            if (count <= 1) {
                withContext(Dispatchers.Main) {
                    onComplete?.invoke(false)
                }
                return@launch
            }
            repository.deleteUnit(unit)
            withContext(Dispatchers.Main) {
                onComplete?.invoke(true)
            }
        }
    }

    fun addHoliday(date: String, name: String, isRecurringYearly: Boolean = true) {
        viewModelScope.launch {
            repository.insertHoliday(Holiday(date = date, name = name, isRecurringYearly = isRecurringYearly))
        }
    }

    fun deleteHoliday(holiday: Holiday) {
        viewModelScope.launch {
            repository.deleteHoliday(holiday)
        }
    }

    class Factory(private val repository: MarketingRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                return MainViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
