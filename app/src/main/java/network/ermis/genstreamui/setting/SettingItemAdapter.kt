package network.ermis.genstreamui.setting

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import network.ermis.genstreamui.R
import network.ermis.genstreamui.databinding.ItemSettingArrowBinding
import network.ermis.genstreamui.databinding.ItemSettingToggleBinding

class SettingItemAdapter(
    private var items: List<SettingItem>,
    private val onItemToggled: (SettingItem, Boolean) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_TOGGLE = 0
        const val VIEW_TYPE_ARROW = 1
    }

    fun updateItems(newItems: List<SettingItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (items[position].type == SettingType.TOGGLE) VIEW_TYPE_TOGGLE else VIEW_TYPE_ARROW
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_TOGGLE) {
            val binding = ItemSettingToggleBinding.inflate(inflater, parent, false)
            ToggleViewHolder(binding)
        } else {
            val binding = ItemSettingArrowBinding.inflate(inflater, parent, false)
            ArrowViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        
        holder.itemView.setOnKeyListener { v, keyCode, event ->
            if (event.action == android.view.KeyEvent.ACTION_DOWN) {
                when (keyCode) {
                    android.view.KeyEvent.KEYCODE_DPAD_LEFT -> {
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
                    }
                    android.view.KeyEvent.KEYCODE_DPAD_UP -> {
                        holder.adapterPosition == 0
                    }
                    android.view.KeyEvent.KEYCODE_DPAD_DOWN -> {
                        holder.adapterPosition == itemCount - 1
                    }
                    else -> false
                }
            } else {
                false
            }
        }
        if (holder is ToggleViewHolder) {
            holder.binding.tvSettingTitle.text = item.title
            holder.binding.tvSettingDesc.text = item.description

            holder.binding.ivToggle.setImageResource(if (item.isEnabled) R.drawable.ic_checkbox_checked else R.drawable.ic_checkbox_unchecked)
            
            holder.itemView.setOnClickListener {
                item.isEnabled = !item.isEnabled
                notifyItemChanged(position)
                onItemToggled(item, item.isEnabled)
            }
        } else if (holder is ArrowViewHolder) {
            holder.binding.tvSettingTitle.text = item.title
            holder.binding.tvSettingDesc.text = item.description

            if (item.value != null) {
                holder.binding.tvSettingValue.text = item.value
                holder.binding.tvSettingValue.visibility = View.VISIBLE
            } else {
                holder.binding.tvSettingValue.visibility = View.GONE
            }
        }
    }

    override fun getItemCount() = items.size

    inner class ToggleViewHolder(val binding: ItemSettingToggleBinding) : RecyclerView.ViewHolder(binding.root)

    inner class ArrowViewHolder(val binding: ItemSettingArrowBinding) : RecyclerView.ViewHolder(binding.root)
}
