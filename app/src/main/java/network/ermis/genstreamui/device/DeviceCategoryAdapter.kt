package network.ermis.genstreamui.device

import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import network.ermis.genstreamui.R
import network.ermis.genstreamui.databinding.ItemDeviceCategoryBinding

class DeviceCategoryAdapter(
    private val categories: List<DeviceCategory>,
    private val onCategorySelected: (DeviceCategory) -> Unit
) : RecyclerView.Adapter<DeviceCategoryAdapter.ViewHolder>() {

    private var selectedIndex = 0

    inner class ViewHolder(val binding: ItemDeviceCategoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDeviceCategoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]
        holder.binding.tvCategoryName.text = category.title

        val isSelectedCategory = position == selectedIndex
        holder.binding.containerCategory.isSelected = isSelectedCategory

        if (isSelectedCategory) {
            holder.binding.tvCategoryName.setTextColor(Color.WHITE)
            holder.binding.tvCategoryName.setTypeface(null, Typeface.BOLD)
        } else {
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

        // Khi focus (D-pad) chuyển tới category này thì coi như chọn nó.
        holder.itemView.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus && selectedIndex != holder.adapterPosition) {
                val prevIndex = selectedIndex
                selectedIndex = holder.adapterPosition
                v.post {
                    notifyItemChanged(prevIndex)
                    notifyItemChanged(selectedIndex)
                    onCategorySelected(category)
                }
            }
        }

        holder.itemView.setOnKeyListener { v, keyCode, event ->
            if (event.action == android.view.KeyEvent.ACTION_DOWN) {
                when (keyCode) {
                    android.view.KeyEvent.KEYCODE_DPAD_RIGHT -> {
                        // Sang lưới thiết bị bên phải.
                        val rvItems = v.rootView.findViewById<RecyclerView>(R.id.rvItems)
                        rvItems?.getChildAt(0)?.requestFocus()
                        true
                    }
                    // Chặn rời khỏi list khi đang ở item đầu/cuối.
                    android.view.KeyEvent.KEYCODE_DPAD_UP -> holder.adapterPosition == 0
                    android.view.KeyEvent.KEYCODE_DPAD_DOWN -> holder.adapterPosition == itemCount - 1
                    else -> false
                }
            } else {
                false
            }
        }
    }

    override fun getItemCount() = categories.size
}
