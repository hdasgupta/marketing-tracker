package com.example.marketingtracker.ui.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.marketingtracker.R
import com.example.marketingtracker.model.UnitEntity

class ManageUnitsDialog(
    context: Context,
    private val units: List<UnitEntity>,
    private val onAddUnitClicked: () -> Unit,
    private val onDeleteUnit: (UnitEntity, (Boolean) -> Unit) -> Unit
) : Dialog(context) {

    private val currentList = units.toMutableList()
    private lateinit var adapter: UnitManageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_manage_units, null)
        setContentView(view)

        window?.setLayout(
            (context.resources.displayMetrics.widthPixels * 0.94).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnAdd = view.findViewById<Button>(R.id.btnAddUnitInManage)
        val rv = view.findViewById<RecyclerView>(R.id.rvManageUnits)
        val btnClose = view.findViewById<Button>(R.id.btnCloseManageUnits)

        adapter = UnitManageAdapter(currentList) { unit ->
            if (currentList.size <= 1) {
                Toast.makeText(context, "At least one unit must remain!", Toast.LENGTH_SHORT).show()
                return@UnitManageAdapter
            }

            AlertDialog.Builder(context)
                .setTitle("Remove Unit")
                .setMessage("Remove '${unit.name} (${unit.symbol})' from units list?")
                .setPositiveButton("Remove") { _, _ ->
                    onDeleteUnit(unit) { success ->
                        if (success) {
                            val idx = currentList.indexOfFirst { it.id == unit.id }
                            if (idx >= 0) {
                                currentList.removeAt(idx)
                                adapter.notifyItemRemoved(idx)
                                Toast.makeText(context, "Unit '${unit.symbol}' removed", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Cannot remove the last unit!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        rv.layoutManager = LinearLayoutManager(context)
        rv.adapter = adapter

        btnAdd.setOnClickListener {
            onAddUnitClicked()
        }

        btnClose.setOnClickListener { dismiss() }
    }

    fun addUnitToList(unit: UnitEntity) {
        currentList.add(unit)
        if (::adapter.isInitialized) {
            adapter.notifyItemInserted(currentList.size - 1)
        }
    }

    private class UnitManageAdapter(
        private val list: List<UnitEntity>,
        private val onDelete: (UnitEntity) -> Unit
    ) : RecyclerView.Adapter<UnitManageAdapter.VH>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_manage_unit, parent, false)
            return VH(view)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val item = list[position]
            holder.tvName.text = item.name
            holder.tvSymbol.text = "Symbol: ${item.symbol}"
            holder.btnDelete.setOnClickListener { onDelete(item) }
        }

        override fun getItemCount(): Int = list.size

        class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvName: TextView = itemView.findViewById(R.id.tvUnitManageName)
            val tvSymbol: TextView = itemView.findViewById(R.id.tvUnitManageSymbol)
            val btnDelete: ImageButton = itemView.findViewById(R.id.btnDeleteUnit)
        }
    }
}
