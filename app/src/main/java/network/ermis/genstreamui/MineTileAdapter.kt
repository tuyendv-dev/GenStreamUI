package network.ermis.genstreamui

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import network.ermis.genstreamui.databinding.ItemMineTileBinding

class MineTileAdapter(
    private val items: List<MineTileItem>,
    private val onItemClick: (position: Int) -> Unit
) : RecyclerView.Adapter<MineTileAdapter.ViewHolder>() {

    private var selectedIndex = 0

    fun getSelectedIndex(): Int = selectedIndex

    fun setSelectedIndex(index: Int) {
        selectedIndex = index
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMineTileBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item, position == selectedIndex, position)
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(val binding: ItemMineTileBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MineTileItem, isActive: Boolean, position: Int) {
            val context = binding.root.context

            // Background
            binding.ivCardBackground.setImageResource(item.backgroundResId)

            // Icon
            binding.ivCardIcon.setImageResource(item.iconResId)
            if (item.tintColorString != null) {
                binding.ivCardIcon.imageTintList = ColorStateList.valueOf(
                    Color.parseColor(item.tintColorString)
                )
            } else {
                binding.ivCardIcon.imageTintList = null
            }

            // Label text
            if (item.labelText != null) {
                binding.tvCardLabel.visibility = View.VISIBLE
                binding.tvCardLabel.text = item.labelText
                if (item.tintColorString != null) {
                    binding.tvCardLabel.textColor = Color.parseColor(item.tintColorString)
                } else {
                    binding.tvCardLabel.textColor = Color.WHITE
                }
            } else {
                binding.tvCardLabel.visibility = View.GONE
            }

            // Click listener
            binding.root.setOnClickListener {
                onItemClick(position)
            }

            // Set states immediately when binding to prevent visual pops during recycling
            val targetScale = if (isActive) 1.5f else 1.0f
            binding.cardView.scaleX = targetScale
            binding.cardView.scaleY = targetScale
            binding.cardView.strokeWidth = if (isActive) dpToPx(2, context) else dpToPx(0, context)
            binding.badgeEnter.visibility = if (isActive) View.VISIBLE else View.GONE
        }

        fun animateScale(isActive: Boolean) {
            val context = binding.root.context
            val targetScale = if (isActive) 1.5f else 1.0f

            // Animate scale smoothly (250ms duration)
            binding.cardView.animate()
                .scaleX(targetScale)
                .scaleY(targetScale)
                .setDuration(250)
                .start()

            // Update border stroke & badge state
            binding.cardView.strokeWidth = if (isActive) dpToPx(2, context) else dpToPx(0, context)
            binding.badgeEnter.visibility = if (isActive) View.VISIBLE else View.GONE
        }
    }

    private fun dpToPx(dp: Int, context: android.content.Context): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density).toInt()
    }

    // Helper extension to change textColor of a TextView easily
    private var android.widget.TextView.textColor: Int
        get() = currentTextColor
        set(value) = setTextColor(value)
}
