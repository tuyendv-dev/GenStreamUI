package network.ermis.genstreamui.presentation.home.adapter

import android.graphics.Rect
import android.view.FocusFinder
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import network.ermis.genstreamui.common.base.ext.loadCover
import network.ermis.genstreamui.databinding.ItemGameBinding
import network.ermis.genstreamui.domain.model.Game
import network.ermis.genstreamui.domain.model.extension.getGameImage
import network.ermis.genstreamui.domain.model.extension.getShortDescriptionExt
import network.ermis.genstreamui.presentation.addScaleClickEffect

/**
 * Adapter card game trong một hàng category màn Discovery. Bind list [network.ermis.genstreamui.domain.model.Game]:
 * cover = main_capsule (fallback header_image), title = title, desc = short_description.
 * Cụm icon nền tảng (llPlatforms) hiện/ẩn theo [Game.platforms]. Giữ logic chặn focus DPAD cho TV.
 */
class GameAdapter(
    private val games: List<Game>,
    private val onClick: (Game) -> Unit
) : RecyclerView.Adapter<GameAdapter.GameViewHolder>() {

    class GameViewHolder(val binding: ItemGameBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val binding = ItemGameBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GameViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        val game = games[position]
        holder.binding.tvGameTitle.text = game.title
        holder.binding.tvGameDesc.text = game.getShortDescriptionExt()
        holder.binding.ivGameCover.loadCover(game.getGameImage())

        bindPlatforms(holder.binding, game.platforms)

        holder.binding.root.addScaleClickEffect()
        holder.binding.root.setOnClickListener { onClick(game) }

        holder.binding.root.setOnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    val direction =
                        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) View.FOCUS_LEFT else View.FOCUS_RIGHT
                    val nextFocus =
                        FocusFinder.getInstance().findNextFocus(v.rootView as ViewGroup, v, direction)
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

    /**
     * Hiện/ẩn từng icon trong llPlatforms theo [platforms] của game. Mỗi icon chỉ hiện nếu [platforms]
     * (chuẩn hoá lowercase) chứa một trong các alias tương ứng. Phải set cả 7 mỗi lần bind vì view tái dùng.
     */
    private fun bindPlatforms(binding: ItemGameBinding, platforms: List<String>) {
        val keys = platforms.map { it.trim().lowercase() }.toHashSet()

        fun toggle(view: ImageView, vararg aliases: String) {
            view.visibility = if (aliases.any { it in keys }) View.VISIBLE else View.GONE
        }

        // Windows luôn hiện (app stream PC).
        binding.ivWindows.visibility = View.VISIBLE
        toggle(binding.ivSteam, "steam")
        toggle(binding.ivXbox, "xbox", "microsoft")
        toggle(binding.ivGog, "gog")
        toggle(binding.ivEa, "ea", "origin")
        toggle(binding.ivBattle, "battle-net", "battlenet", "battle")
        toggle(binding.ivEpic, "epic", "epicgames", "epic-games")
    }

    override fun getItemCount() = games.size
}