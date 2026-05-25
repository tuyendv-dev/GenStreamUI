package network.ermis.genstreamui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import network.ermis.genstreamui.databinding.ItemGameBinding

class GameAdapter(private val games: List<GameModel>) : RecyclerView.Adapter<GameAdapter.GameViewHolder>() {

    class GameViewHolder(val binding: ItemGameBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val binding = ItemGameBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GameViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        val game = games[position]
        holder.binding.tvGameTitle.text = game.title
        holder.binding.tvGameDesc.text = game.description
        holder.binding.ivGameCover.setImageResource(game.imageRes)
        
        holder.binding.root.addScaleClickEffect()
        holder.binding.root.setOnClickListener {
            val context = holder.binding.root.context
            val intent = android.content.Intent(context, PlayGameActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = games.size
}
