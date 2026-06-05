package network.ermis.genstreamui.subscription

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import network.ermis.genstreamui.R
import network.ermis.genstreamui.addScaleClickEffect
import network.ermis.genstreamui.databinding.ItemSubscriptionPlanBinding

class SubscriptionPlanAdapter(
    private val plans: List<SubscriptionPlan>,
    private val onPlanSelected: (SubscriptionPlan) -> Unit
) : RecyclerView.Adapter<SubscriptionPlanAdapter.ViewHolder>() {

    var selectedPosition = 1
        private set

    inner class ViewHolder(val binding: ItemSubscriptionPlanBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.addScaleClickEffect()
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION && position != selectedPosition) {
                    val prev = selectedPosition
                    selectedPosition = position
                    notifyItemChanged(prev)
                    notifyItemChanged(selectedPosition)
                    onPlanSelected(plans[position])
                }
            }
        }

        fun bind(plan: SubscriptionPlan, isSelected: Boolean) {
            binding.tvPlanName.text = plan.name
            binding.ivPlanIcon.setImageResource(plan.iconResId)
            
            if (plan.isCurrentPlan) {
                binding.tvCurrentPlanBadge.visibility = View.VISIBLE
            } else {
                binding.tvCurrentPlanBadge.visibility = View.GONE
            }

            if (isSelected) {
                binding.root.setBackgroundResource(R.drawable.bg_subscription_plan_selected)
            } else {
                binding.root.setBackgroundResource(R.drawable.bg_subscription_plan_unselected)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSubscriptionPlanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(plans[position], position == selectedPosition)
    }

    override fun getItemCount() = plans.size
}
