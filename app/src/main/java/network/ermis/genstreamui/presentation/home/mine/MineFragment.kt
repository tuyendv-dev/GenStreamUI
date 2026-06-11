package network.ermis.genstreamui.presentation.home.mine

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import network.ermis.genstreamui.R
import network.ermis.genstreamui.databinding.FragmentMineBinding
import network.ermis.genstreamui.databinding.ItemMineTileBinding

@AndroidEntryPoint
class MineFragment : Fragment() {

    private var _binding: FragmentMineBinding? = null
    private val binding get() = _binding!!

    private lateinit var tileItems: List<MineTileItem>
    private var selectedIndex = -1

    private val tileBindings = mutableListOf<ItemMineTileBinding>()
    private var snapRunnable: Runnable? = null
    private var isProgrammaticScroll = false
    private var steamGridAnimator: ObjectAnimator? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMineBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tileItems = listOf(
            MineTileItem(0, R.drawable.ic_controller_game, null, R.drawable.bg_card_controller),
            MineTileItem(1, R.drawable.ic_windows, null, R.drawable.bg_card_windows),
            MineTileItem(2, R.drawable.ic_steam_white, null, R.drawable.bg_card_steam),
            MineTileItem(3, R.drawable.ic_epic_game, null, R.drawable.bg_card_epic),
            MineTileItem(4, R.drawable.ic_gog_com, null, R.drawable.bg_card_gog),
            MineTileItem(5, R.drawable.ic_ea_game, null, R.drawable.bg_card_ea_game),
            MineTileItem(6, R.drawable.ic_xbox_game, null, R.drawable.bg_card_xbox),
            MineTileItem(7, R.drawable.ic_battle_game, null, R.drawable.bg_card_battle)
        )

        binding.horizontalScrollViewTiles.post {
            val paddingEnd = binding.horizontalScrollViewTiles.width - dpToPx(148)
            binding.horizontalScrollViewTiles.setPaddingRelative(
                binding.horizontalScrollViewTiles.paddingStart,
                binding.horizontalScrollViewTiles.paddingTop,
                paddingEnd,
                binding.horizontalScrollViewTiles.paddingBottom
            )
        }

        setupTiles()

        binding.horizontalScrollViewTiles.setOnScrollChangeListener { _, scrollX, _, _, _ ->
            if (isProgrammaticScroll) return@setOnScrollChangeListener
            snapRunnable?.let { binding.horizontalScrollViewTiles.removeCallbacks(it) }
            snapRunnable = Runnable {
                snapToNearestItem(scrollX)
            }
            binding.horizontalScrollViewTiles.postDelayed(snapRunnable, 150)
        }

        // Set default selection to first card without animation
        binding.horizontalScrollViewTiles.post {
            selectCard(0, smoothScroll = false)
        }
    }

    private fun setupTiles() {
        val layoutInflater = LayoutInflater.from(context)
        binding.linearLayoutTiles.removeAllViews()
        tileBindings.clear()

        for ((index, item) in tileItems.withIndex()) {
            val tileBinding = ItemMineTileBinding.inflate(layoutInflater, binding.linearLayoutTiles, false)

            // Set initial unselected state
            tileBinding.cardView.scaleX = 1.0f
            tileBinding.cardView.scaleY = 1.0f
            tileBinding.cardView.strokeWidth = dpToPx(0)
            tileBinding.badgeEnter.visibility = View.GONE

            // Bind data
            tileBinding.ivCardBackground.setImageResource(item.backgroundResId)
            tileBinding.ivCardIcon.setImageResource(item.iconResId)

            if (item.tintColorString != null) {
                tileBinding.ivCardIcon.imageTintList = ColorStateList.valueOf(Color.parseColor(item.tintColorString))
            } else {
                tileBinding.ivCardIcon.imageTintList = null
            }

            if (item.labelText != null) {
                tileBinding.tvCardLabel.visibility = View.VISIBLE
                tileBinding.tvCardLabel.text = item.labelText
                if (item.tintColorString != null) {
                    tileBinding.tvCardLabel.setTextColor(Color.parseColor(item.tintColorString))
                } else {
                    tileBinding.tvCardLabel.setTextColor(Color.WHITE)
                }
            } else {
                tileBinding.tvCardLabel.visibility = View.GONE
            }

            tileBinding.root.setOnClickListener {
                selectCard(index, smoothScroll = true)
            }

            tileBinding.root.isFocusable = true
            tileBinding.root.setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus) {
                    tileBinding.cardView.strokeWidth = dpToPx(1)
                    tileBinding.cardView.setStrokeColor(Color.parseColor("#E4E5E5"))
                    selectCard(index, smoothScroll = true)
                } else {
                    tileBinding.cardView.strokeWidth = dpToPx(0)
                    v.isFocusableInTouchMode = false
                }
            }

            tileBinding.root.setOnKeyListener { _, keyCode, event ->
                if (event.action == KeyEvent.ACTION_DOWN) {
                    when (keyCode) {
                        KeyEvent.KEYCODE_DPAD_LEFT -> {
                            index == 0
                        }
                        KeyEvent.KEYCODE_DPAD_RIGHT -> {
                            index == tileItems.size - 1
                        }
                        else -> false
                    }
                } else {
                    false
                }
            }

            binding.linearLayoutTiles.addView(tileBinding.root)
            tileBindings.add(tileBinding)
        }
    }

    private fun snapToNearestItem(scrollX: Int) {
        if (isProgrammaticScroll || tileBindings.isEmpty()) return
        var minDistance = Int.MAX_VALUE
        var nearestIndex = selectedIndex

        for (i in tileBindings.indices) {
            val childLeft = tileBindings[i].root.left
            val distance = Math.abs(childLeft - scrollX)
            if (distance < minDistance) {
                minDistance = distance
                nearestIndex = i
            }
        }

        if (nearestIndex != selectedIndex) {
            selectCard(nearestIndex, smoothScroll = true)
        } else {
            // Already selected, just snap it back if it drifted
            val targetLeft = tileBindings[nearestIndex].root.left
            if (Math.abs(scrollX - targetLeft) > 10) {
                isProgrammaticScroll = true
                binding.horizontalScrollViewTiles.smoothScrollTo(targetLeft, 0)
                binding.horizontalScrollViewTiles.postDelayed({ isProgrammaticScroll = false }, 300)
            }
        }
    }

    private fun selectCard(position: Int, smoothScroll: Boolean = true) {
        if (position < 0 || position >= tileBindings.size) return

        val targetView = tileBindings[position].root
        isProgrammaticScroll = true
        if (smoothScroll) {
            binding.horizontalScrollViewTiles.smoothScrollTo(targetView.left, 0)
        } else {
            binding.horizontalScrollViewTiles.scrollTo(targetView.left, 0)
        }
        binding.horizontalScrollViewTiles.postDelayed({ isProgrammaticScroll = false }, 300)

        if (position == selectedIndex) return
        val prevIndex = selectedIndex
        selectedIndex = position

        // Animate previous item down to 1.0x scale
        if (prevIndex != -1 && prevIndex < tileBindings.size) {
            val prevBinding = tileBindings[prevIndex]
            prevBinding.cardView.animate().scaleX(1.0f).scaleY(1.0f).setDuration(250).start()
            prevBinding.badgeEnter.visibility = View.GONE
        }

        // Animate new item up to 1.5x scale
        val newBinding = tileBindings[position]
        newBinding.cardView.animate().scaleX(1.4f).scaleY(1.4f).setDuration(250).start()
        newBinding.badgeEnter.visibility = View.VISIBLE

        binding.horizontalScrollViewTiles.selectedChildView = newBinding.root

        // Đảm bảo item nhận được focus ngay cả trong Touch mode
        newBinding.root.isFocusableInTouchMode = true
        newBinding.root.requestFocus()

        // Update details panel & background theme
        updateDetailsAndBackground(position)
    }

    private fun updateDetailsAndBackground(index: Int) {
        val context = requireContext()
        val btnAction = binding.btnAction

        // Fade-in animation for layoutDetails
        binding.layoutDetails.alpha = 0f
        binding.layoutDetails.animate()
            .alpha(1f)
            .setDuration(300)
            .start()

        // Reset overlays and grid default states
        steamGridAnimator?.cancel()
        binding.layoutSteamGrid.scaleX = 1f
        binding.layoutSteamGrid.scaleY = 1f
        binding.layoutSteamGrid.visibility = View.GONE
        binding.ivGhostIcon.animate().cancel()
        binding.ivGhostIcon.visibility = View.GONE
        binding.ivGhostIcon.alpha = 0f
        binding.layoutUserInfo.visibility = View.GONE
        when (index) {
            0 -> { // Game Library
                binding.tvTitle.text = "View Game Library"
                binding.tvDescription.text = "Easily manage your favorite games on PC, mobile, PS, Xbox, and more"
                btnAction.visibility = View.GONE

                binding.layoutSteamGrid.visibility = View.VISIBLE
            }
            1 -> {// Windows
                binding.tvTitle.text = "PC Emulator"
                binding.tvDescription.text = "Import PC games, play AAA titles on mobile"
                btnAction.visibility = View.GONE

                btnAction.visibility = View.VISIBLE
                btnAction.text = "Import PC games"
                btnAction.icon = null //ContextCompat.getDrawable(context, R.drawable.ic_windows)
//                btnAction.iconTint = ColorStateList.valueOf(Color.WHITE)

                binding.ivGhostIcon.setImageResource(R.drawable.ic_windows)
                binding.ivGhostIcon.visibility = View.VISIBLE
            }
            2 -> { // Steam
                binding.tvTitle.text = "Steam"
                binding.tvDescription.text = "View and manage all games under your current Steam account"

                btnAction.visibility = View.VISIBLE
                btnAction.text = "Sign in to Steam"
                btnAction.icon = null

                binding.layoutSteamGrid.visibility = View.VISIBLE
            }
            3 -> { // Epic Games
                binding.tvTitle.text = "Epic Games"
                binding.tvDescription.text = "Import your favorite PC games into your library and experience PC-grade triple-A games on your phone"

                btnAction.visibility = View.GONE
                binding.layoutUserInfo.visibility = View.VISIBLE

                binding.layoutSteamGrid.visibility = View.VISIBLE
            }
            4 -> {
                binding.tvTitle.text = ""
                binding.tvDescription.text = ""
                btnAction.visibility = View.GONE

                binding.layoutSteamGrid.visibility = View.VISIBLE
            }
            5 -> {
                binding.tvTitle.text = ""
                binding.tvDescription.text = ""
                btnAction.visibility = View.GONE

                binding.layoutSteamGrid.visibility = View.VISIBLE
            }
            6 -> {
                binding.tvTitle.text = ""
                binding.tvDescription.text = ""
                btnAction.visibility = View.GONE

                binding.layoutSteamGrid.visibility = View.VISIBLE
            }
            7 -> {
                binding.tvTitle.text = ""
                binding.tvDescription.text = ""
                btnAction.visibility = View.GONE

                binding.layoutSteamGrid.visibility = View.VISIBLE
            }
        }

        if (binding.ivGhostIcon.visibility == View.VISIBLE) {
            binding.ivGhostIcon.scaleX = 0.8f
            binding.ivGhostIcon.scaleY = 0.8f
            binding.ivGhostIcon.animate()
                .alpha(0.8f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(500)
                .start()
        }

        if (binding.layoutSteamGrid.visibility == View.VISIBLE) {
            val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.3f)
            val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.3f)
            steamGridAnimator = ObjectAnimator.ofPropertyValuesHolder(binding.layoutSteamGrid, scaleX, scaleY).apply {
                duration = 5000
                repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.REVERSE
                start()
            }
        }
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        steamGridAnimator?.cancel()
        _binding = null
    }
}