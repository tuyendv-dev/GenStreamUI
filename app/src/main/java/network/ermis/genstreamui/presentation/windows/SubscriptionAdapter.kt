package network.ermis.genstreamui.presentation.windows

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import network.ermis.genstreamui.R
import network.ermis.genstreamui.databinding.ItemDialogSubscriptionBinding

class SubscriptionAdapter(
    private val items: List<String>,
    private var selectedIndex: Int = 0
) : RecyclerView.Adapter<SubscriptionAdapter.ViewHolder>() {

    fun getSelectedIndex() = selectedIndex

    inner class ViewHolder(val binding: ItemDialogSubscriptionBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION && position != selectedIndex) {
                    val oldIndex = selectedIndex
                    selectedIndex = position
                    notifyItemChanged(oldIndex)
                    notifyItemChanged(selectedIndex)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDialogSubscriptionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.tvLabel.text = items[position]
        if (position == selectedIndex) {
            holder.binding.ivCheck.setImageResource(R.drawable.ic_checkbox_checked)
        } else {
            holder.binding.ivCheck.setImageResource(R.drawable.ic_checkbox_unchecked)
        }
    }

    override fun getItemCount(): Int = items.size
}
