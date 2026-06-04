package network.ermis.genstreamui.device

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import network.ermis.genstreamui.R
import network.ermis.genstreamui.addScaleClickEffect
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
