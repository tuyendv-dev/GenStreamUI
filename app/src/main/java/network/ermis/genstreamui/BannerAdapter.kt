package network.ermis.genstreamui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import network.ermis.genstreamui.databinding.ItemBannerSlideBinding

class BannerAdapter(private val banners: List<GameModel>) : RecyclerView.Adapter<BannerAdapter.BannerViewHolder>() {

    class BannerViewHolder(val binding: ItemBannerSlideBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        val binding = ItemBannerSlideBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BannerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
        val banner = banners[position]
        holder.binding.tvBannerTitle.text = banner.title
        holder.binding.tvBannerDesc.text = banner.description
        holder.binding.ivBannerImage.setImageResource(banner.imageRes)

        holder.binding.root.setOnClickListener {
            val context = holder.binding.root.context
            val intent = android.content.Intent(context, PlayGameActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = banners.size
}
