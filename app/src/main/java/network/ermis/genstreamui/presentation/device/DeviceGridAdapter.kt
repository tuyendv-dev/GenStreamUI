package network.ermis.genstreamui.presentation.device

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import network.ermis.genstreamui.R
import network.ermis.genstreamui.presentation.addScaleClickEffect
import network.ermis.genstreamui.databinding.ItemDeviceGridBinding

class DeviceGridAdapter(
    private var items: List<DeviceItem>,
    private val onItemClick: (DeviceItem) -> Unit
) : RecyclerView.Adapter<DeviceGridAdapter.ViewHolder>() {

    fun updateItems(newItems: List<DeviceItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemDeviceGridBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDeviceGridBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvDeviceName.text = item.name

        holder.itemView.setOnKeyListener { v, keyCode, event ->
            if (event.action == android.view.KeyEvent.ACTION_DOWN) {
                val rvItems = v.rootView.findViewById<RecyclerView>(R.id.rvItems)
                val spanCount = (rvItems?.layoutManager as? GridLayoutManager)?.spanCount ?: 1
                val pos = holder.adapterPosition
                when (keyCode) {
                    android.view.KeyEvent.KEYCODE_DPAD_LEFT -> {
                        // Đang ở cột trái cùng -> quay về category đang chọn bên trái.
                        if (pos % spanCount == 0) {
                            val rvCategories = v.rootView.findViewById<RecyclerView>(R.id.rvCategories)
                            var handled = false
                            for (i in 0 until (rvCategories?.childCount ?: 0)) {
                                val child = rvCategories?.getChildAt(i)
                                if (child?.isSelected == true) {
                                    child.requestFocus()
                                    handled = true
                                    break
                                }
                            }
                            handled
                        } else {
                            false
                        }
                    }
                    // Chặn rời khỏi lưới khi đang ở hàng trên cùng/dưới cùng.
                    android.view.KeyEvent.KEYCODE_DPAD_UP -> pos < spanCount
                    android.view.KeyEvent.KEYCODE_DPAD_DOWN -> {
                        val lastRowStart = ((itemCount - 1) / spanCount) * spanCount
                        pos >= lastRowStart
                    }
                    else -> false
                }
            } else {
                false
            }
        }

        if (item.isAddButton) {
            holder.binding.ivDeviceIcon.visibility = View.GONE
            holder.binding.tvAddIcon.visibility = View.VISIBLE
            holder.binding.cardDevice.setBackgroundResource(R.drawable.bg_setting_category_selected)
        } else {
            holder.binding.ivDeviceIcon.visibility = View.VISIBLE
            holder.binding.tvAddIcon.visibility = View.GONE
            holder.binding.cardDevice.setBackgroundResource(R.drawable.bg_device_item)
            if (item.iconResId != null) {
                holder.binding.ivDeviceIcon.setImageResource(item.iconResId)
            } else {
                holder.binding.ivDeviceIcon.setImageResource(R.drawable.ic_controller_game) // Fallback icon
            }
        }
        holder.itemView.addScaleClickEffect()
        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount() = items.size
}
