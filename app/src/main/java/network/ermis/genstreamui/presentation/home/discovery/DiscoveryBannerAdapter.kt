package network.ermis.genstreamui.presentation.home.discovery

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import network.ermis.genstreamui.common.base.ext.loadCover
import network.ermis.genstreamui.databinding.ItemBannerSlideBinding
import network.ermis.genstreamui.domain.model.Game

/**
 * Adapter cho carousel banner lớn màn Discovery. Bind list [Game] (data.featured):
 * ảnh = header_image (load qua Glide), tiêu đề = title, mô tả = tagline.
 */
class DiscoveryBannerAdapter(
    private val games: List<Game>,
    private val onClick: (Game) -> Unit
) : RecyclerView.Adapter<DiscoveryBannerAdapter.BannerViewHolder>() {

    class BannerViewHolder(val binding: ItemBannerSlideBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        val binding = ItemBannerSlideBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return BannerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
        val game = games[position]
        holder.binding.tvBannerTitle.text = game.title
        holder.binding.tvBannerDesc.text =
            game.tagline.ifBlank { game.shortDescription }
        holder.binding.ivBannerImage.loadCover(game.headerImage)

        holder.binding.root.setOnClickListener { onClick(game) }
    }

    override fun getItemCount() = games.size
}
