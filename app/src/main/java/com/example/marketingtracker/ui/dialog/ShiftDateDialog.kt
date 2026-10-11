package com.example.marketingtracker.ui.dialog

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import com.example.marketingtracker.R
import com.example.marketingtracker.model.MarketingItem
import com.example.marketingtracker.util.DateUtils
import java.util.Calendar

class ShiftDateDialog(
    context: Context,
    private val item: MarketingItem,
    private val onShiftSingle: (MarketingItem, String) -> Unit,
    private val onShiftAllUnbought: (String, String) -> Unit
) : Dialog(context) {

    private var targetDate: String = DateUtils.getTomorrowDateString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_shift_date, null)
        setContentView(view)

        window?.setLayout(
            (context.resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        window?.setBackgroundDrawableResource(android.R.color.transparent)

        val tvTitle = view.findViewById<TextView>(R.id.tvShiftTitle)
        val tvSub = view.findViewById<TextView>(R.id.tvShiftSubtitle)
        val btnToday = view.findViewById<Button>(R.id.btnShiftToday)
        val btnTomorrow = view.findViewById<Button>(R.id.btnShiftTomorrow)
        val btnInTwoDays = view.findViewById<Button>(R.id.btnShiftInTwoDays)
        val btnNextSunday = view.findViewById<Button>(R.id.btnShiftNextSunday)
        val btnPickDate = view.findViewById<Button>(R.id.btnShiftPickDate)
        val cbShiftAll = view.findViewById<CheckBox>(R.id.cbShiftAllUnbought)
        val btnCancel = view.findViewById<Button>(R.id.btnCancelShift)
        val btnConfirm = view.findViewById<Button>(R.id.btnConfirmShift)

        tvTitle.text = "Shift Date: ${item.name}"
        tvSub.text = "Currently scheduled for: ${DateUtils.formatDisplayDate(item.date)}"

        fun updateTargetDate(newDate: String) {
            targetDate = newDate
            btnPickDate.text = "Custom: ${DateUtils.formatDisplayDate(targetDate)}"
        }

        val today = DateUtils.getTodayDateString()
        val tomorrow = DateUtils.getTomorrowDateString()
        val inTwoDays = DateUtils.addDays(today, 2)

        // Find next Sunday
        val nextSundayCal = Calendar.getInstance().apply {
            while (get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        val nextSunday = DateUtils.formatDate(nextSundayCal)

        btnToday.setOnClickListener { updateTargetDate(today) }
        btnTomorrow.setOnClickListener { updateTargetDate(tomorrow) }
        btnInTwoDays.setOnClickListener { updateTargetDate(inTwoDays) }
        btnNextSunday.setOnClickListener { updateTargetDate(nextSunday) }

        btnPickDate.setOnClickListener {
            val cal = DateUtils.parseDate(targetDate)
            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    val chosenCal = Calendar.getInstance().apply {
                        set(year, month, dayOfMonth)
                    }
                    updateTargetDate(DateUtils.formatDate(chosenCal))
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        btnCancel.setOnClickListener { dismiss() }

        btnConfirm.setOnClickListener {
            if (cbShiftAll.isChecked) {
                onShiftAllUnbought(item.date, targetDate)
            } else {
                onShiftSingle(item, targetDate)
            }
            dismiss()
        }
    }
}
