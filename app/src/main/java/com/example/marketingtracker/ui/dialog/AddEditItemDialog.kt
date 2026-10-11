package com.example.marketingtracker.ui.dialog

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import com.example.marketingtracker.R
import com.example.marketingtracker.model.Category
import com.example.marketingtracker.model.MarketingItem
import com.example.marketingtracker.model.UnitEntity
import com.example.marketingtracker.util.DateUtils
import java.util.Calendar

class AddEditItemDialog(
    context: Context,
    private val initialDate: String,
    private val existingItem: MarketingItem? = null,
    initialCategories: List<Category>,
    initialUnits: List<UnitEntity>,
    private val suggestions: List<String>,
    private val onSave: (MarketingItem) -> Unit,
    private val onAddCategoryRequest: (name: String, colorHex: String, onAdded: (Category) -> Unit) -> Unit,
    private val onAddUnitRequest: (name: String, symbol: String, onAdded: (UnitEntity) -> Unit) -> Unit,
    private val onManageCategoriesRequest: (currentCategories: List<Category>, onUpdated: (List<Category>) -> Unit) -> Unit,
    private val onManageUnitsRequest: (currentUnits: List<UnitEntity>, onUpdated: (List<UnitEntity>) -> Unit) -> Unit
) : Dialog(context) {

    private var selectedDate: String = existingItem?.date ?: initialDate

    // Built-in fallbacks ensure the dropdowns are never empty even if Room is still initializing
    private val defaultCategories = listOf(
        Category(1, "Vegetables", "#10B981"),
        Category(2, "Fruits", "#F59E0B"),
        Category(3, "Dairy & Eggs", "#3B82F6"),
        Category(4, "Groceries & Staples", "#8B5CF6"),
        Category(5, "Meat & Seafood", "#EF4444"),
        Category(6, "Bakery & Bread", "#D97706"),
        Category(7, "Beverages", "#06B6D4"),
        Category(8, "Snacks & Sweets", "#EC4899"),
        Category(9, "Spices & Condiments", "#F97316"),
        Category(10, "Household & Cleaning", "#64748B"),
        Category(11, "Personal Care", "#14B8A6"),
        Category(12, "Others", "#6B7280")
    )

    private val defaultUnits = listOf(
        UnitEntity(1, "Kilogram", "kg"),
        UnitEntity(2, "Gram", "g"),
        UnitEntity(3, "Litre", "L"),
        UnitEntity(4, "Millilitre", "ml"),
        UnitEntity(5, "Piece", "pc"),
        UnitEntity(6, "Packet", "pkt"),
        UnitEntity(7, "Dozen", "doz"),
        UnitEntity(8, "Box", "box"),
        UnitEntity(9, "Bottle", "btl"),
        UnitEntity(10, "Bunch", "bunch"),
        UnitEntity(11, "Can", "can")
    )

    private val categoriesList = (if (initialCategories.isNotEmpty()) initialCategories else defaultCategories).toMutableList()
    private val unitsList = (if (initialUnits.isNotEmpty()) initialUnits else defaultUnits).toMutableList()

    private lateinit var categoryAdapter: ArrayAdapter<String>
    private lateinit var unitAdapter: ArrayAdapter<String>
    private lateinit var spCategory: Spinner
    private lateinit var spUnit: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_add_edit_item, null)
        setContentView(view)

        window?.setLayout(
            (context.resources.displayMetrics.widthPixels * 0.94).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        window?.setBackgroundDrawableResource(android.R.color.transparent)

        val tvDialogTitle = view.findViewById<TextView>(R.id.tvDialogTitle)
        val actvName = view.findViewById<AutoCompleteTextView>(R.id.actvItemName)
        spCategory = view.findViewById(R.id.spCategory)
        val btnAddCategory = view.findViewById<ImageButton>(R.id.btnAddCategory)
        val btnManageCategories = view.findViewById<ImageButton>(R.id.btnManageCategories)
        val etQuantity = view.findViewById<EditText>(R.id.etQuantity)
        spUnit = view.findViewById(R.id.spUnit)
        val btnAddUnit = view.findViewById<ImageButton>(R.id.btnAddUnit)
        val btnManageUnits = view.findViewById<ImageButton>(R.id.btnManageUnits)
        val btnDate = view.findViewById<Button>(R.id.btnDate)
        val etNotes = view.findViewById<EditText>(R.id.etNotes)
        val btnCancel = view.findViewById<Button>(R.id.btnCancel)
        val btnSave = view.findViewById<Button>(R.id.btnSave)

        tvDialogTitle.text = if (existingItem == null) "Add Marketing Item" else "Edit Marketing Item"

        // Autocomplete suggestions from previous history
        val allSuggestions = (suggestions + listOf(
            "Milk", "Bread", "Eggs", "Tomatoes", "Potatoes", "Onions", "Rice",
            "Apples", "Bananas", "Cooking Oil", "Tea", "Coffee", "Sugar", "Salt",
            "Chicken", "Wheat Flour", "Butter", "Cheese", "Garlic", "Ginger",
            "Toothpaste", "Dish Soap", "Laundry Detergent", "Biscuits"
        )).distinct()

        val suggestionAdapter = ArrayAdapter(
            context,
            android.R.layout.simple_dropdown_item_1line,
            allSuggestions
        )
        actvName.setAdapter(suggestionAdapter)
        actvName.threshold = 1

        // Initialize Category Spinner
        refreshCategoryAdapter()

        // Initialize Unit Spinner
        refreshUnitAdapter()

        // Populate fields if editing
        if (existingItem != null) {
            actvName.setText(existingItem.name)
            etQuantity.setText(existingItem.formattedQuantity)
            etNotes.setText(existingItem.notes ?: "")

            val catIndex = categoriesList.indexOfFirst { it.id == existingItem.categoryId }
            if (catIndex >= 0) spCategory.setSelection(catIndex)

            val unitIndex = unitsList.indexOfFirst {
                it.symbol.equals(existingItem.unit, ignoreCase = true) ||
                it.name.equals(existingItem.unit, ignoreCase = true)
            }
            if (unitIndex >= 0) spUnit.setSelection(unitIndex)
        } else {
            etQuantity.setText("1")
        }

        btnDate.text = DateUtils.formatDisplayDate(selectedDate)

        btnDate.setOnClickListener {
            val cal = DateUtils.parseDate(selectedDate)
            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    val chosenCal = Calendar.getInstance().apply {
                        set(year, month, dayOfMonth)
                    }
                    selectedDate = DateUtils.formatDate(chosenCal)
                    btnDate.text = DateUtils.formatDisplayDate(selectedDate)
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Add Category: DO NOT DISMISS THIS DIALOG
        btnAddCategory.setOnClickListener {
            AddCategoryDialog(context) { name, colorHex ->
                onAddCategoryRequest(name, colorHex) { newCat ->
                    val existingIdx = categoriesList.indexOfFirst { it.name.equals(newCat.name, true) }
                    if (existingIdx >= 0) {
                        spCategory.setSelection(existingIdx)
                    } else {
                        categoriesList.add(newCat)
                        refreshCategoryAdapter()
                        spCategory.setSelection(categoriesList.size - 1)
                    }
                }
            }.show()
        }

        // Manage/Remove Categories: DO NOT DISMISS THIS DIALOG
        btnManageCategories.setOnClickListener {
            onManageCategoriesRequest(categoriesList) { updatedList ->
                categoriesList.clear()
                categoriesList.addAll(updatedList)
                refreshCategoryAdapter()
            }
        }

        // Add Unit: DO NOT DISMISS THIS DIALOG
        btnAddUnit.setOnClickListener {
            AddUnitDialog(context) { name, symbol ->
                onAddUnitRequest(name, symbol) { newUnit ->
                    val existingIdx = unitsList.indexOfFirst { it.symbol.equals(newUnit.symbol, true) }
                    if (existingIdx >= 0) {
                        spUnit.setSelection(existingIdx)
                    } else {
                        unitsList.add(newUnit)
                        refreshUnitAdapter()
                        spUnit.setSelection(unitsList.size - 1)
                    }
                }
            }.show()
        }

        // Manage/Remove Units: DO NOT DISMISS THIS DIALOG
        btnManageUnits.setOnClickListener {
            onManageUnitsRequest(unitsList) { updatedList ->
                unitsList.clear()
                unitsList.addAll(updatedList)
                refreshUnitAdapter()
            }
        }

        btnCancel.setOnClickListener { dismiss() }

        btnSave.setOnClickListener {
            val name = actvName.text.toString().trim()
            if (name.isBlank()) {
                actvName.error = "Please enter an item name"
                return@setOnClickListener
            }

            val qtyStr = etQuantity.text.toString().trim()
            val qty = qtyStr.toDoubleOrNull() ?: 1.0

            val catPosition = spCategory.selectedItemPosition
            val categoryId = if (catPosition >= 0 && catPosition < categoriesList.size) {
                categoriesList[catPosition].id
            } else {
                categoriesList.firstOrNull()?.id ?: 1L
            }

            val unitPosition = spUnit.selectedItemPosition
            val unitSymbol = if (unitPosition >= 0 && unitPosition < unitsList.size) {
                unitsList[unitPosition].symbol
            } else {
                "pc"
            }

            val notes = etNotes.text.toString().trim().ifBlank { null }

            val item = existingItem?.copy(
                name = name,
                categoryId = categoryId,
                quantity = qty,
                unit = unitSymbol,
                date = selectedDate,
                notes = notes
            ) ?: MarketingItem(
                name = name,
                categoryId = categoryId,
                quantity = qty,
                unit = unitSymbol,
                date = selectedDate,
                notes = notes
            )

            onSave(item)
            dismiss()
        }
    }

    private fun refreshCategoryAdapter() {
        val names = categoriesList.map { it.name }
        categoryAdapter = ArrayAdapter(
            context,
            android.R.layout.simple_spinner_dropdown_item,
            names
        )
        spCategory.adapter = categoryAdapter
    }

    private fun refreshUnitAdapter() {
        val displayStrings = unitsList.map {
            if (it.name.equals(it.symbol, ignoreCase = true)) it.symbol else "${it.name} (${it.symbol})"
        }
        unitAdapter = ArrayAdapter(
            context,
            android.R.layout.simple_spinner_dropdown_item,
            displayStrings
        )
        spUnit.adapter = unitAdapter
    }
}
