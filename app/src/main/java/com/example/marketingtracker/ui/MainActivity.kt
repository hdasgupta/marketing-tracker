package com.example.marketingtracker.ui

import android.Manifest
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.marketingtracker.MarketingApplication
import com.example.marketingtracker.R
import com.example.marketingtracker.model.Category
import com.example.marketingtracker.model.MarketingItem
import com.example.marketingtracker.model.UnitEntity
import com.example.marketingtracker.notification.NotificationScheduler
import com.example.marketingtracker.ui.adapter.CategoryChipAdapter
import com.example.marketingtracker.ui.adapter.MarketingItemAdapter
import com.example.marketingtracker.ui.dialog.AddCategoryDialog
import com.example.marketingtracker.ui.dialog.AddEditItemDialog
import com.example.marketingtracker.ui.dialog.AddUnitDialog
import com.example.marketingtracker.ui.dialog.ManageCategoriesDialog
import com.example.marketingtracker.ui.dialog.ManageHolidaysDialog
import com.example.marketingtracker.ui.dialog.ManageUnitsDialog
import com.example.marketingtracker.ui.dialog.ShiftDateDialog
import com.example.marketingtracker.util.DateUtils
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels {
        MainViewModel.Factory((application as MarketingApplication).repository)
    }

    private lateinit var itemAdapter: MarketingItemAdapter
    private lateinit var categoryChipAdapter: CategoryChipAdapter

    private lateinit var tvDateDisplay: TextView
    private lateinit var tvSummaryCounts: TextView
    private lateinit var emptyView: View
    private lateinit var btnShiftAll: TextView

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
                NotificationScheduler.scheduleNextNotification(this)
            } else {
                Toast.makeText(this, "Notifications are disabled. Enable in Settings.", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        initViews()
        initRecyclerViews()
        observeData()
        checkPermissions()

        intent?.getStringExtra("EXTRA_TARGET_DATE")?.let { targetDate ->
            viewModel.setSelectedDate(targetDate)
        }
    }

    private fun initViews() {
        tvDateDisplay = findViewById(R.id.tvDateDisplay)
        tvSummaryCounts = findViewById(R.id.tvSummaryCounts)
        emptyView = findViewById(R.id.layoutEmptyState)
        btnShiftAll = findViewById(R.id.btnShiftAllUnbought)

        val btnPrevDay = findViewById<ImageButton>(R.id.btnPrevDay)
        val btnNextDay = findViewById<ImageButton>(R.id.btnNextDay)
        val btnToday = findViewById<TextView>(R.id.btnToday)
        val fabAdd = findViewById<FloatingActionButton>(R.id.fabAddItem)

        btnPrevDay.setOnClickListener { viewModel.shiftDateByDays(-1) }
        btnNextDay.setOnClickListener { viewModel.shiftDateByDays(1) }
        btnToday.setOnClickListener { viewModel.setSelectedDate(DateUtils.getTodayDateString()) }

        tvDateDisplay.setOnClickListener {
            val currentCal = DateUtils.parseDate(viewModel.selectedDate.value)
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    val chosen = Calendar.getInstance().apply { set(year, month, dayOfMonth) }
                    viewModel.setSelectedDate(DateUtils.formatDate(chosen))
                },
                currentCal.get(Calendar.YEAR),
                currentCal.get(Calendar.MONTH),
                currentCal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        fabAdd.setOnClickListener {
            showAddEditDialog(null)
        }

        btnShiftAll.setOnClickListener {
            val currentDate = viewModel.selectedDate.value
            val tomorrow = DateUtils.getTomorrowDateString()
            viewModel.shiftAllUnboughtItems(currentDate, tomorrow) { count ->
                if (count > 0) {
                    Toast.makeText(this, "Shifted $count unbought items to Tomorrow!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "No unbought items to shift!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun initRecyclerViews() {
        val rvItems = findViewById<RecyclerView>(R.id.rvMarketingItems)
        val rvCategories = findViewById<RecyclerView>(R.id.rvCategories)

        itemAdapter = MarketingItemAdapter(
            onToggleBought = { item -> viewModel.toggleItemBought(item) },
            onShiftDate = { item -> showShiftDateDialog(item) },
            onEditItem = { item -> showAddEditDialog(item) },
            onDeleteItem = { item -> viewModel.deleteItem(item) }
        )
        rvItems.layoutManager = LinearLayoutManager(this)
        rvItems.adapter = itemAdapter

        categoryChipAdapter = CategoryChipAdapter { categoryId ->
            viewModel.selectCategory(categoryId)
        }
        rvCategories.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvCategories.adapter = categoryChipAdapter
    }

    private fun observeData() {
        lifecycleScope.launch {
            viewModel.selectedDate.collectLatest { date ->
                tvDateDisplay.text = DateUtils.formatDisplayDate(date)
            }
        }

        lifecycleScope.launch {
            viewModel.categories.collectLatest { cats ->
                itemAdapter.updateCategories(cats)
                categoryChipAdapter.submitList(cats, viewModel.selectedCategoryId.value)
            }
        }

        lifecycleScope.launch {
            viewModel.itemsForSelectedDate.collectLatest { items ->
                itemAdapter.submitList(items)
                updateSummary(items)
            }
        }
    }

    private fun updateSummary(items: List<MarketingItem>) {
        if (items.isEmpty()) {
            emptyView.visibility = View.VISIBLE
            tvSummaryCounts.text = "No items"
            btnShiftAll.visibility = View.GONE
        } else {
            emptyView.visibility = View.GONE
            val unbought = items.count { !it.isBought }
            val bought = items.count { it.isBought }
            tvSummaryCounts.text = "$unbought to buy • $bought bought"

            if (unbought > 0) {
                btnShiftAll.visibility = View.VISIBLE
                btnShiftAll.text = "Shift $unbought unbought to Tomorrow"
            } else {
                btnShiftAll.visibility = View.GONE
            }
        }
    }

    private fun showAddEditDialog(item: MarketingItem?) {
        AddEditItemDialog(
            context = this,
            initialDate = viewModel.selectedDate.value,
            existingItem = item,
            initialCategories = viewModel.categories.value,
            initialUnits = viewModel.units.value,
            suggestions = viewModel.itemSuggestions.value,
            onSave = { savedItem ->
                if (item == null) {
                    viewModel.insertItem(savedItem)
                } else {
                    viewModel.updateItem(savedItem)
                }
            },
            onAddCategoryRequest = { name, colorHex, onAdded ->
                viewModel.addCategory(name, colorHex) { createdCat ->
                    onAdded(createdCat)
                    Toast.makeText(this, "Category '${createdCat.name}' added", Toast.LENGTH_SHORT).show()
                }
            },
            onAddUnitRequest = { name, symbol, onAdded ->
                viewModel.addUnit(name, symbol) { createdUnit ->
                    onAdded(createdUnit)
                    Toast.makeText(this, "Unit '${createdUnit.symbol}' added", Toast.LENGTH_SHORT).show()
                }
            },
            onManageCategoriesRequest = { currentList, onUpdated ->
                showManageCategoriesDialog(currentList, onUpdated)
            },
            onManageUnitsRequest = { currentList, onUpdated ->
                showManageUnitsDialog(currentList, onUpdated)
            }
        ).show()
    }

    private fun showShiftDateDialog(item: MarketingItem) {
        ShiftDateDialog(
            context = this,
            item = item,
            onShiftSingle = { shiftItem, newDate ->
                viewModel.shiftItemDate(shiftItem.id, newDate)
                Toast.makeText(this, "Shifted to ${DateUtils.formatDisplayDate(newDate)}", Toast.LENGTH_SHORT).show()
            },
            onShiftAllUnbought = { oldDate, newDate ->
                viewModel.shiftAllUnboughtItems(oldDate, newDate) { count ->
                    Toast.makeText(this, "Shifted $count unbought items to ${DateUtils.formatDisplayDate(newDate)}", Toast.LENGTH_SHORT).show()
                }
            }
        ).show()
    }

    private fun showAddCategoryDialog() {
        AddCategoryDialog(this) { name, colorHex ->
            viewModel.addCategory(name, colorHex)
            Toast.makeText(this, "Category '$name' added", Toast.LENGTH_SHORT).show()
        }.show()
    }

    private fun showAddUnitDialog() {
        AddUnitDialog(this) { name, symbol ->
            viewModel.addUnit(name, symbol)
            Toast.makeText(this, "Unit '$name ($symbol)' added", Toast.LENGTH_SHORT).show()
        }.show()
    }

    private fun showManageCategoriesDialog(
        currentCategories: List<Category> = viewModel.categories.value,
        onUpdated: ((List<Category>) -> Unit)? = null
    ) {
        val dialog = ManageCategoriesDialog(
            context = this,
            categories = currentCategories,
            onAddCategoryClicked = { showAddCategoryDialog() },
            onDeleteCategory = { category, onComplete ->
                viewModel.deleteCategory(category, onComplete)
            }
        )
        dialog.setOnDismissListener {
            onUpdated?.invoke(viewModel.categories.value)
        }
        dialog.show()
    }

    private fun showManageUnitsDialog(
        currentUnits: List<UnitEntity> = viewModel.units.value,
        onUpdated: ((List<UnitEntity>) -> Unit)? = null
    ) {
        val dialog = ManageUnitsDialog(
            context = this,
            units = currentUnits,
            onAddUnitClicked = { showAddUnitDialog() },
            onDeleteUnit = { unit, onComplete ->
                viewModel.deleteUnit(unit, onComplete)
            }
        )
        dialog.setOnDismissListener {
            onUpdated?.invoke(viewModel.units.value)
        }
        dialog.show()
    }

    private fun showManageHolidaysDialog() {
        ManageHolidaysDialog(
            this,
            holidays = viewModel.holidays.value,
            onAddHoliday = { date, name ->
                viewModel.addHoliday(date, name)
                Toast.makeText(this, "Holiday '$name' added for $date", Toast.LENGTH_SHORT).show()
            },
            onDeleteHoliday = { holiday ->
                viewModel.deleteHoliday(holiday)
                Toast.makeText(this, "Holiday removed", Toast.LENGTH_SHORT).show()
            }
        ).show()
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(0, 1, 0, "Manage Categories")
        menu.add(0, 2, 0, "Manage Units")
        menu.add(0, 3, 0, "Manage Holidays")
        menu.add(0, 4, 0, "Test Notification & Toast Now")
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            1 -> {
                showManageCategoriesDialog()
                true
            }
            2 -> {
                showManageUnitsDialog()
                true
            }
            3 -> {
                showManageHolidaysDialog()
                true
            }
            4 -> {
                Toast.makeText(this, "Triggering test notification check...", Toast.LENGTH_SHORT).show()
                NotificationScheduler.triggerImmediateCheck(this)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
