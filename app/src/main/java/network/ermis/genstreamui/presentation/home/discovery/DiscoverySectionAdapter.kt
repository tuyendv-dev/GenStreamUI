package network.ermis.genstreamui.presentation.home.discovery

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import network.ermis.genstreamui.databinding.ItemGameSectionBinding
import network.ermis.genstreamui.domain.model.DiscoverySection
import network.ermis.genstreamui.domain.model.Game

/**
 * Adapter danh sách section màn Discovery (data.sections). Mỗi item = 1 category:
 * tiêu đề = section.category, lưới game = section.games (GridLayoutManager 4 cột qua [DiscoveryGameAdapter]).
 */
class DiscoverySectionAdapter(
    private val sections: List<DiscoverySection>,
    private val onGameClick: (Game) -> Unit
) : RecyclerView.Adapter<DiscoverySectionAdapter.SectionViewHolder>() {

    class SectionViewHolder(val binding: ItemGameSectionBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SectionViewHolder {
        val binding = ItemGameSectionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SectionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SectionViewHolder, position: Int) {
        val section = sections[position]
        holder.binding.tvSectionTitle.text = section.category
        holder.binding.rvSectionGames.adapter =
            DiscoveryGameAdapter(section.games, onGameClick)
    }

    override fun getItemCount() = sections.size
}
