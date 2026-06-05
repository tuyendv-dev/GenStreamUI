package network.ermis.genstreamui.subscription

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import network.ermis.genstreamui.databinding.ItemPlanFeatureBinding

class PlanFeatureAdapter(
    private var features: List<PlanFeature>
) : RecyclerView.Adapter<PlanFeatureAdapter.ViewHolder>() {

    fun updateData(newFeatures: List<PlanFeature>) {
        features = newFeatures
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemPlanFeatureBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(feature: PlanFeature) {
            binding.tvFeatureText.text = feature.text
            binding.ivFeatureIcon.setImageResource(feature.iconResId)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPlanFeatureBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(features[position])
    }

    override fun getItemCount() = features.size
}
