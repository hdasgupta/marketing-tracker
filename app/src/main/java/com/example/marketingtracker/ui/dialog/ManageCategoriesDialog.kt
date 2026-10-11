package com.example.marketingtracker.ui.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Color
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
import com.example.marketingtracker.model.Category

class ManageCategoriesDialog(
    context: Context,
    private val categories: List<Category>,
    private val onAddCategoryClicked: () -> Unit,
    private val onDeleteCategory: (Category, (Boolean) -> Unit) -> Unit
) : Dialog(context) {

    private val currentList = categories.toMutableList()
    private lateinit var adapter: CategoryManageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_manage_categories, null)
        setContentView(view)

        window?.setLayout(
            (context.resources.displayMetrics.widthPixels * 0.94).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnAdd = view.findViewById<Button>(R.id.btnAddCategoryInManage)
        val rv = view.findViewById<RecyclerView>(R.id.rvManageCategories)
        val btnClose = view.findViewById<Button>(R.id.btnCloseManageCategories)

        adapter = CategoryManageAdapter(currentList) { category ->
            if (currentList.size <= 1) {
                Toast.makeText(context, "At least one category is required!", Toast.LENGTH_SHORT).show()
                return@CategoryManageAdapter
            }

            AlertDialog.Builder(context)
                .setTitle("Remove Category")
                .setMessage("Are you sure you want to remove '${category.name}'?\n\nItems in this category will be reassigned to another category.")
                .setPositiveButton("Remove") { _, _ ->
                    onDeleteCategory(category) { success ->
                        if (success) {
                            val idx = currentList.indexOfFirst { it.id == category.id }
                            if (idx >= 0) {
                                currentList.removeAt(idx)
                                adapter.notifyItemRemoved(idx)
                                Toast.makeText(context, "Category '${category.name}' removed", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Cannot remove the last category!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        rv.layoutManager = LinearLayoutManager(context)
        rv.adapter = adapter

        btnAdd.setOnClickListener {
            onAddCategoryClicked()
        }

        btnClose.setOnClickListener { dismiss() }
    }

    fun addCategoryToList(category: Category) {
        currentList.add(category)
        if (::adapter.isInitialized) {
            adapter.notifyItemInserted(currentList.size - 1)
        }
    }

    private class CategoryManageAdapter(
        private val list: List<Category>,
        private val onDelete: (Category) -> Unit
    ) : RecyclerView.Adapter<CategoryManageAdapter.VH>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_manage_category, parent, false)
            return VH(view)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val item = list[position]
            holder.tvName.text = item.name
            val color = try {
                Color.parseColor(item.colorHex)
            } catch (e: Exception) {
                Color.parseColor("#10B981")
            }
            holder.viewColor.setBackgroundColor(color)
            holder.btnDelete.setOnClickListener { onDelete(item) }
        }

        override fun getItemCount(): Int = list.size

        class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val viewColor: View = itemView.findViewById(R.id.viewCategoryColor)
            val tvName: TextView = itemView.findViewById(R.id.tvCategoryManageName)
            val btnDelete: ImageButton = itemView.findViewById(R.id.btnDeleteCategory)
        }
    }
}
