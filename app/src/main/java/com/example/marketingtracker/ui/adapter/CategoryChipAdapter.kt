package com.example.marketingtracker.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.marketingtracker.R
import com.example.marketingtracker.model.Category

class CategoryChipAdapter(
    private val onCategorySelected: (Long?) -> Unit
) : RecyclerView.Adapter<CategoryChipAdapter.ViewHolder>() {

    private var categories: List<Category> = emptyList()
    private var selectedCategoryId: Long? = null

    fun submitList(newCategories: List<Category>, selectedId: Long?) {
        categories = newCategories
        selectedCategoryId = selectedId
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_chip, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position == 0) {
            // "All" chip
            holder.bind("All Categories", null, selectedCategoryId == null, "#10B981")
        } else {
            val category = categories[position - 1]
            holder.bind(category.name, category.id, category.id == selectedCategoryId, category.colorHex)
        }
    }

    override fun getItemCount(): Int = categories.size + 1

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvChip: TextView = itemView.findViewById(R.id.tvChip)

        fun bind(name: String, categoryId: Long?, isSelected: Boolean, colorHex: String) {
            tvChip.text = name

            val chipColor = try {
                Color.parseColor(colorHex)
            } catch (e: Exception) {
                ContextCompat.getColor(itemView.context, R.color.primary)
            }

            if (isSelected) {
                tvChip.setBackgroundResource(R.drawable.bg_chip_selected)
                tvChip.setTextColor(Color.WHITE)
            } else {
                tvChip.setBackgroundResource(R.drawable.bg_chip_unselected)
                tvChip.setTextColor(ContextCompat.getColor(itemView.context, R.color.text_primary))
            }

            itemView.setOnClickListener {
                selectedCategoryId = categoryId
                notifyDataSetChanged()
                onCategorySelected(categoryId)
            }
        }
    }
}
