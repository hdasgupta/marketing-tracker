package com.example.marketingtracker.ui.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import com.example.marketingtracker.R

class AddCategoryDialog(
    context: Context,
    private val onCategoryAdded: (name: String, colorHex: String) -> Unit
) : Dialog(context) {

    private var selectedColorHex: String = "#10B981"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_add_category, null)
        setContentView(view)

        window?.setLayout(
            (context.resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        window?.setBackgroundDrawableResource(android.R.color.transparent)

        val etCategoryName = view.findViewById<EditText>(R.id.etCategoryName)
        val colorContainer = view.findViewById<LinearLayout>(R.id.colorContainer)
        val btnCancel = view.findViewById<Button>(R.id.btnCancelCategory)
        val btnSave = view.findViewById<Button>(R.id.btnSaveCategory)

        val presetColors = listOf(
            "#10B981", "#3B82F6", "#F59E0B", "#EF4444",
            "#8B5CF6", "#EC4899", "#06B6D4", "#F97316"
        )

        for (colorHex in presetColors) {
            val colorView = View(context).apply {
                val size = (context.resources.displayMetrics.density * 36).toInt()
                layoutParams = LinearLayout.LayoutParams(size, size).apply {
                    setMargins(12, 12, 12, 12)
                }
                setBackgroundColor(Color.parseColor(colorHex))
                setOnClickListener {
                    selectedColorHex = colorHex
                    Toast.makeText(context, "Color selected", Toast.LENGTH_SHORT).show()
                }
            }
            colorContainer.addView(colorView)
        }

        btnCancel.setOnClickListener { dismiss() }

        btnSave.setOnClickListener {
            val name = etCategoryName.text.toString().trim()
            if (name.isBlank()) {
                etCategoryName.error = "Enter a category name"
                return@setOnClickListener
            }
            onCategoryAdded(name, selectedColorHex)
            dismiss()
        }
    }
}
