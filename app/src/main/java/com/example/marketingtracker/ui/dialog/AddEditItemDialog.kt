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
import android.widget.Toast
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
    private val categories: List<Category>,
    private val units: List<UnitEntity>,
    private val suggestions: List<String>,
    private val onSave: (MarketingItem) -> Unit,
    private val onAddNewCategory: () -> Unit,
    private val onAddNewUnit: () -> Unit
) : Dialog(context) {

    private var selectedDate: String = existingItem?.date ?: initialDate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_add_edit_item, null)
        setContentView(view)

        window?.setLayout(
            (context.resources.displayMetrics.widthPixels * 0.92).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        window?.setBackgroundDrawableResource(android.R.color.transparent)

        val tvDialogTitle = view.findViewById<TextView>(R.id.tvDialogTitle)
        val actvName = view.findViewById<AutoCompleteTextView>(R.id.actvItemName)
        val spCategory = view.findViewById<Spinner>(R.id.spCategory)
        val btnAddCategory = view.findViewById<ImageButton>(R.id.btnAddCategory)
        val etQuantity = view.findViewById<EditText>(R.id.etQuantity)
        val spUnit = view.findViewById<Spinner>(R.id.spUnit)
        val btnAddUnit = view.findViewById<ImageButton>(R.id.btnAddUnit)
        val btnDate = view.findViewById<Button>(R.id.btnDate)
        val etNotes = view.findViewById<EditText>(R.id.etNotes)
        val btnCancel = view.findViewById<Button>(R.id.btnCancel)
        val btnSave = view.findViewById<Button>(R.id.btnSave)

        tvDialogTitle.text = if (existingItem == null) "Add Marketing Item" else "Edit Marketing Item"

        // 1. Suggestions Autocomplete for previous items
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

        // 2. Category Spinner
        val categoryNames = categories.map { it.name }
        val categoryAdapter = ArrayAdapter(
            context,
            android.R.layout.simple_spinner_dropdown_item,
            categoryNames
        )
        spCategory.adapter = categoryAdapter

        // 3. Unit Spinner
        val unitSymbols = units.map { it.symbol }
        val unitAdapter = ArrayAdapter(
            context,
            android.R.layout.simple_spinner_dropdown_item,
            unitSymbols
        )
        spUnit.adapter = unitAdapter

        // Populate fields if editing
        if (existingItem != null) {
            actvName.setText(existingItem.name)
            etQuantity.setText(existingItem.formattedQuantity)
            etNotes.setText(existingItem.notes ?: "")

            val catIndex = categories.indexOfFirst { it.id == existingItem.categoryId }
            if (catIndex >= 0) spCategory.setSelection(catIndex)

            val unitIndex = units.indexOfFirst { it.symbol.equals(existingItem.unit, ignoreCase = true) }
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

        btnAddCategory.setOnClickListener {
            dismiss()
            onAddNewCategory()
        }

        btnAddUnit.setOnClickListener {
            dismiss()
            onAddNewUnit()
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
            val categoryId = if (catPosition >= 0 && catPosition < categories.size) {
                categories[catPosition].id
            } else {
                categories.firstOrNull()?.id ?: 1L
            }

            val unitPosition = spUnit.selectedItemPosition
            val unitSymbol = if (unitPosition >= 0 && unitPosition < units.size) {
                units[unitPosition].symbol
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
}
