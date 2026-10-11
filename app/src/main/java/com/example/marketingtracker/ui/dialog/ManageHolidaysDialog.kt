package com.example.marketingtracker.ui.dialog

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.marketingtracker.R
import com.example.marketingtracker.model.Holiday
import com.example.marketingtracker.util.DateUtils
import java.util.Calendar

class ManageHolidaysDialog(
    context: Context,
    private val holidays: List<Holiday>,
    private val onAddHoliday: (date: String, name: String) -> Unit,
    private val onDeleteHoliday: (Holiday) -> Unit
) : Dialog(context) {

    private var selectedDate: String = DateUtils.getTodayDateString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_manage_holidays, null)
        setContentView(view)

        window?.setLayout(
            (context.resources.displayMetrics.widthPixels * 0.92).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        window?.setBackgroundDrawableResource(android.R.color.transparent)

        val etHolidayName = view.findViewById<EditText>(R.id.etHolidayName)
        val btnPickDate = view.findViewById<Button>(R.id.btnPickHolidayDate)
        val btnAdd = view.findViewById<Button>(R.id.btnAddHoliday)
        val rvHolidays = view.findViewById<RecyclerView>(R.id.rvHolidays)
        val btnClose = view.findViewById<Button>(R.id.btnCloseHolidays)

        btnPickDate.text = "Date: $selectedDate"

        btnPickDate.setOnClickListener {
            val cal = DateUtils.parseDate(selectedDate)
            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    val chosenCal = Calendar.getInstance().apply {
                        set(year, month, dayOfMonth)
                    }
                    selectedDate = DateUtils.formatDate(chosenCal)
                    btnPickDate.text = "Date: $selectedDate"
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        btnAdd.setOnClickListener {
            val name = etHolidayName.text.toString().trim()
            if (name.isBlank()) {
                etHolidayName.error = "Enter holiday name"
                return@setOnClickListener
            }
            onAddHoliday(selectedDate, name)
            etHolidayName.setText("")
            dismiss()
        }

        rvHolidays.layoutManager = LinearLayoutManager(context)
        rvHolidays.adapter = HolidayAdapter(holidays) { holiday ->
            onDeleteHoliday(holiday)
            dismiss()
        }

        btnClose.setOnClickListener { dismiss() }
    }

    private class HolidayAdapter(
        private val list: List<Holiday>,
        private val onDelete: (Holiday) -> Unit
    ) : RecyclerView.Adapter<HolidayAdapter.VH>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_holiday, parent, false)
            return VH(view)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val holiday = list[position]
            holder.tvName.text = holiday.name
            holder.tvDate.text = holiday.date
            holder.btnDelete.setOnClickListener { onDelete(holiday) }
        }

        override fun getItemCount(): Int = list.size

        class VH(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
            val tvName: TextView = itemView.findViewById(R.id.tvHolidayName)
            val tvDate: TextView = itemView.findViewById(R.id.tvHolidayDate)
            val btnDelete: Button = itemView.findViewById(R.id.btnDeleteHoliday)
        }
    }
}
