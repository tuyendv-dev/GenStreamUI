package network.ermis.genstreamui.setting

import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import network.ermis.genstreamui.R
import network.ermis.genstreamui.databinding.ItemSettingCategoryBinding

class SettingCategoryAdapter(
    private val categories: List<SettingCategory>,
    private val onCategorySelected: (SettingCategory) -> Unit
) : RecyclerView.Adapter<SettingCategoryAdapter.ViewHolder>() {

    private var selectedIndex = 0

    inner class ViewHolder(val binding: ItemSettingCategoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSettingCategoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]
        holder.binding.tvCategoryName.text = category.title
        
        if (position == selectedIndex) {
            holder.binding.containerCategory.setBackgroundResource(R.drawable.bg_setting_category_selected)
            holder.binding.tvCategoryName.setTextColor(Color.WHITE)
            holder.binding.tvCategoryName.setTypeface(null, Typeface.BOLD)
        } else {
            holder.binding.containerCategory.background = null
            holder.binding.tvCategoryName.setTextColor(Color.parseColor("#80FFFFFF"))
            holder.binding.tvCategoryName.setTypeface(null, Typeface.NORMAL)
        }

        holder.itemView.setOnClickListener {
            val prevIndex = selectedIndex
            selectedIndex = holder.adapterPosition
            notifyItemChanged(prevIndex)
            notifyItemChanged(selectedIndex)
            onCategorySelected(category)
        }
    }

    override fun getItemCount() = categories.size
}
