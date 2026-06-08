package network.ermis.genstreamui.presentation

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
        
        holder.binding.root.setOnKeyListener { v, keyCode, event ->
            if (event.action == android.view.KeyEvent.ACTION_DOWN) {
                if (keyCode == android.view.KeyEvent.KEYCODE_DPAD_LEFT || keyCode == android.view.KeyEvent.KEYCODE_DPAD_RIGHT) {
                    val direction = if (keyCode == android.view.KeyEvent.KEYCODE_DPAD_LEFT) android.view.View.FOCUS_LEFT else android.view.View.FOCUS_RIGHT
                    val nextFocus = android.view.FocusFinder.getInstance().findNextFocus(v.rootView as android.view.ViewGroup, v, direction)
                    if (nextFocus != null) {
                        // Chặn nhảy focus ra ngoài RecyclerView (như menu bên trái)
                        if (nextFocus.parent != v.parent) {
                            return@setOnKeyListener true
                        }

                        val currentRect = android.graphics.Rect()
                        v.getGlobalVisibleRect(currentRect)
                        val nextRect = android.graphics.Rect()
                        nextFocus.getGlobalVisibleRect(nextRect)
                        
                        // Chặn nhảy focus nếu view tiếp theo nằm ở hàng khác
                        if (currentRect.bottom <= nextRect.top || currentRect.top >= nextRect.bottom) {
                            return@setOnKeyListener true
                        }
                    } else {
                        // Không có view tiếp theo -> chặn luôn
                        return@setOnKeyListener true
                    }
                }
            }
            false
        }
    }

    override fun getItemCount() = games.size
}
