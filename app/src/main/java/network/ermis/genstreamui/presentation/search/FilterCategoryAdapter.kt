package network.ermis.genstreamui.presentation.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import network.ermis.genstreamui.R
import network.ermis.genstreamui.databinding.ItemPopupFilterBinding

class FilterCategoryAdapter(
    private val items: List<String>,
    private val selectedIndex: Int,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<FilterCategoryAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemPopupFilterBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(position)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPopupFilterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvCategoryName.text = item
        if (position == selectedIndex) {
            holder.binding.ivCheck.setImageResource(R.drawable.ic_checkbox_checked)
        } else {
            holder.binding.ivCheck.setImageResource(R.drawable.ic_checkbox_unchecked)
        }
    }

    override fun getItemCount(): Int = items.size
}
