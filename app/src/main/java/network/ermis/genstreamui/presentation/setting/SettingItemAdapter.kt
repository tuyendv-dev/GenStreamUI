package network.ermis.genstreamui.presentation.setting

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import network.ermis.genstreamui.R
import network.ermis.genstreamui.databinding.ItemSettingArrowBinding
import network.ermis.genstreamui.databinding.ItemSettingToggleBinding

class SettingItemAdapter(
    private var items: List<SettingItem>,
    private val onItemChanged: (SettingItem, Any) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_TOGGLE = 0
        const val VIEW_TYPE_ARROW = 1 // Used for ARROW, INFO, LIST, SEEKBAR
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
        val context = holder.itemView.context

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
            holder.binding.tvSettingDesc.visibility =
                if (item.description.isNotEmpty()) View.VISIBLE else View.GONE

            holder.binding.ivToggle.setImageResource(if (item.isEnabled) R.drawable.ic_checkbox_checked else R.drawable.ic_checkbox_unchecked)

            holder.itemView.setOnClickListener {
                val newValue = !item.isEnabled
                item.isEnabled = newValue
                notifyItemChanged(position)
                onItemChanged(item, newValue)
            }
        } else if (holder is ArrowViewHolder) {
            holder.binding.tvSettingTitle.text = item.title
            holder.binding.tvSettingDesc.text = item.description
            holder.binding.tvSettingDesc.visibility =
                if (item.description.isNotEmpty()) View.VISIBLE else View.GONE

            if (item.type == SettingType.INFO || item.type == SettingType.ARROW) {
                holder.binding.tvSettingValue.text = item.value ?: ""
                holder.binding.tvSettingValue.visibility =
                    if (item.value != null) View.VISIBLE else View.GONE
                holder.itemView.setOnClickListener(null)
            } else if (item.type == SettingType.LIST) {
                val displayValue =
                    if (item.value != null && item.entryValues != null && item.entries != null) {
                        val idx = item.entryValues!!.indexOf(item.value)
                        if (idx >= 0 && idx < item.entries!!.size) item.entries!![idx] else item.value
                    } else {
                        item.value ?: ""
                    }
                holder.binding.tvSettingValue.text = displayValue
                holder.binding.tvSettingValue.visibility = View.VISIBLE

                holder.itemView.setOnClickListener {
                    val entries = item.entries ?: return@setOnClickListener
                    val entryValues = item.entryValues ?: return@setOnClickListener
                    val currentIndex = entryValues.indexOf(item.value).coerceAtLeast(0)

                    MaterialAlertDialogBuilder(context)
                        .setTitle(item.title)
                        .setSingleChoiceItems(entries, currentIndex) { dialog, which ->
                            val newValue = entryValues[which]
                            item.value = newValue
                            notifyItemChanged(position)
                            onItemChanged(item, newValue)
                            dialog.dismiss()
                        }
                        .setNegativeButton(android.R.string.cancel, null)
                        .show()
                }
            } else if (item.type == SettingType.SEEKBAR) {
                holder.binding.tvSettingValue.text = "${item.value ?: item.min} ${item.suffix}"
                holder.binding.tvSettingValue.visibility = View.VISIBLE

                holder.itemView.setOnClickListener {
                    val padding = (16 * context.resources.displayMetrics.density).toInt()
                    val container = android.widget.LinearLayout(context).apply {
                        orientation = android.widget.LinearLayout.VERTICAL
                        setPadding(padding, padding, padding, padding)
                    }
                    val white = ContextCompat.getColor(context, R.color.white)
                    val tvValue = TextView(context).apply {
                        textSize = 16f
                        setPadding(0, 0, 0, padding)
                        // View tạo bằng context activity (không theo theme dialog) → set màu sáng thủ công.
                        setTextColor(white)
                    }
                    val seekBar = SeekBar(context).apply {
                        progressTintList = ColorStateList.valueOf(white)
                        thumbTintList = ColorStateList.valueOf(white)
                    }

                    container.addView(tvValue)
                    container.addView(seekBar)

                    val currentValue = item.value?.toIntOrNull() ?: item.min
                    val maxProgress = (item.max - item.min) / item.step
                    val currentProgress = (currentValue - item.min) / item.step

                    seekBar.max = maxProgress
                    seekBar.progress = currentProgress
                    tvValue.text = "$currentValue ${item.suffix}"

                    seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(
                            sb: SeekBar?,
                            progress: Int,
                            fromUser: Boolean
                        ) {
                            val newVal = item.min + (progress * item.step)
                            tvValue.text = "$newVal ${item.suffix}"
                        }

                        override fun onStartTrackingTouch(sb: SeekBar?) {}
                        override fun onStopTrackingTouch(sb: SeekBar?) {}
                    })

                    MaterialAlertDialogBuilder(context)
                        .setTitle(item.title)
                        .setView(container)
                        .setPositiveButton(android.R.string.ok) { dialog, _ ->
                            val finalValue = item.min + (seekBar.progress * item.step)
                            item.value = finalValue.toString()
                            notifyItemChanged(position)
                            onItemChanged(item, finalValue)
                        }
                        .setNegativeButton(android.R.string.cancel, null)
                        .show()
                }
            }
        }
    }

    override fun getItemCount() = items.size

    inner class ToggleViewHolder(val binding: ItemSettingToggleBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class ArrowViewHolder(val binding: ItemSettingArrowBinding) :
        RecyclerView.ViewHolder(binding.root)
}
