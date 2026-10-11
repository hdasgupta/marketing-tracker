package com.example.marketingtracker.ui.adapter

import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.marketingtracker.R
import com.example.marketingtracker.model.Category
import com.example.marketingtracker.model.MarketingItem

class MarketingItemAdapter(
    private val onToggleBought: (MarketingItem) -> Unit,
    private val onShiftDate: (MarketingItem) -> Unit,
    private val onEditItem: (MarketingItem) -> Unit,
    private val onDeleteItem: (MarketingItem) -> Unit
) : ListAdapter<MarketingItem, MarketingItemAdapter.ViewHolder>(DiffCallback) {

    private var categoryMap: Map<Long, Category> = emptyMap()

    fun updateCategories(categories: List<Category>) {
        categoryMap = categories.associateBy { it.id }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_marketing, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: CardView = itemView.findViewById(R.id.cardItem)
        private val cbBought: CheckBox = itemView.findViewById(R.id.cbBought)
        private val tvName: TextView = itemView.findViewById(R.id.tvItemName)
        private val tvQuantity: TextView = itemView.findViewById(R.id.tvQuantity)
        private val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        private val tvNotes: TextView = itemView.findViewById(R.id.tvNotes)
        private val btnShift: ImageButton = itemView.findViewById(R.id.btnShiftDate)
        private val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
        private val categoryIndicator: View = itemView.findViewById(R.id.categoryIndicator)

        fun bind(item: MarketingItem) {
            tvName.text = item.name
            tvQuantity.text = "${item.formattedQuantity} ${item.unit}"

            val category = categoryMap[item.categoryId]
            val catName = category?.name ?: "General"
            tvCategory.text = catName

            val catColor = try {
                Color.parseColor(category?.colorHex ?: "#10B981")
            } catch (e: Exception) {
                Color.parseColor("#10B981")
            }
            categoryIndicator.setBackgroundColor(catColor)
            tvCategory.setTextColor(catColor)

            if (!item.notes.isNullOrBlank()) {
                tvNotes.visibility = View.VISIBLE
                tvNotes.text = item.notes
            } else {
                tvNotes.visibility = View.GONE
            }

            cbBought.isChecked = item.isBought

            // Strikethrough & visual feedback for bought status
            if (item.isBought) {
                tvName.paintFlags = tvName.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                tvName.alpha = 0.6f
                tvQuantity.alpha = 0.6f
                cardView.alpha = 0.75f
            } else {
                tvName.paintFlags = tvName.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                tvName.alpha = 1.0f
                tvQuantity.alpha = 1.0f
                cardView.alpha = 1.0f
            }

            cbBought.setOnClickListener {
                onToggleBought(item)
            }

            btnShift.setOnClickListener {
                onShiftDate(item)
            }

            btnEdit.setOnClickListener {
                onEditItem(item)
            }

            btnDelete.setOnClickListener {
                onDeleteItem(item)
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<MarketingItem>() {
        override fun areItemsTheSame(oldItem: MarketingItem, newItem: MarketingItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MarketingItem, newItem: MarketingItem): Boolean {
            return oldItem == newItem
        }
    }
}
