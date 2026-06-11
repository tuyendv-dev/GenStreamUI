package network.ermis.genstreamui.presentation.home

import android.content.Intent
import android.graphics.Rect
import android.view.FocusFinder
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import network.ermis.genstreamui.databinding.ItemGameBinding
import network.ermis.genstreamui.presentation.PlayGameActivity
import network.ermis.genstreamui.presentation.addScaleClickEffect

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
            val intent = Intent(context, PlayGameActivity::class.java)
            context.startActivity(intent)
        }

        holder.binding.root.setOnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    val direction = if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) View.FOCUS_LEFT else View.FOCUS_RIGHT
                    val nextFocus = FocusFinder.getInstance().findNextFocus(v.rootView as ViewGroup, v, direction)
                    if (nextFocus != null) {
                        // Chặn nhảy focus ra ngoài RecyclerView (như menu bên trái)
                        if (nextFocus.parent != v.parent) {
                            return@setOnKeyListener true
                        }

                        val currentRect = Rect()
                        v.getGlobalVisibleRect(currentRect)
                        val nextRect = Rect()
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