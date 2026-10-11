package com.example.marketingtracker.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.example.marketingtracker.R

class AddUnitDialog(
    context: Context,
    private val onUnitAdded: (name: String, symbol: String) -> Unit
) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_add_unit, null)
        setContentView(view)

        window?.setLayout(
            (context.resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        window?.setBackgroundDrawableResource(android.R.color.transparent)

        val etUnitName = view.findViewById<EditText>(R.id.etUnitName)
        val etUnitSymbol = view.findViewById<EditText>(R.id.etUnitSymbol)
        val btnCancel = view.findViewById<Button>(R.id.btnCancelUnit)
        val btnSave = view.findViewById<Button>(R.id.btnSaveUnit)

        btnCancel.setOnClickListener { dismiss() }

        btnSave.setOnClickListener {
            val name = etUnitName.text.toString().trim()
            val symbol = etUnitSymbol.text.toString().trim()

            if (name.isBlank()) {
                etUnitName.error = "Enter unit name"
                return@setOnClickListener
            }
            if (symbol.isBlank()) {
                etUnitSymbol.error = "Enter abbreviation/symbol"
                return@setOnClickListener
            }

            onUnitAdded(name, symbol)
            dismiss()
        }
    }
}
