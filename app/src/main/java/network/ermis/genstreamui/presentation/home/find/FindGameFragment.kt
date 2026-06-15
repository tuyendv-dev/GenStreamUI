package network.ermis.genstreamui.presentation.home.find

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import network.ermis.genstreamui.R
import network.ermis.genstreamui.common.UiState
import network.ermis.genstreamui.common.base.BaseFragment
import network.ermis.genstreamui.common.base.ext.collectWhenStarted
import network.ermis.genstreamui.common.base.ext.loadCover
import network.ermis.genstreamui.databinding.FragmentFindGameBinding
import network.ermis.genstreamui.domain.model.Discovery
import network.ermis.genstreamui.domain.model.Game
import network.ermis.genstreamui.domain.model.extension.getGameBackground
import network.ermis.genstreamui.domain.model.extension.getGameImage
import network.ermis.genstreamui.domain.model.extension.getShortDescriptionExt
import network.ermis.genstreamui.presentation.PlayGameActivity
import network.ermis.genstreamui.presentation.addScaleClickEffect
import network.ermis.genstreamui.presentation.home.adapter.SectionGameAdapter

@AndroidEntryPoint
class FindGameFragment :
    BaseFragment<FragmentFindGameBinding>(FragmentFindGameBinding::inflate) {

    private val TAG: String = "FindGameFragment"
    private val viewModel: FindGameViewModel by viewModels()

    override fun initViews() {
        setupScrollEffect()
        setupCardClicks()

        viewModel.loadStore(null)
    }

    override fun observerData() {
        collectWhenStarted(viewModel.events) { ui ->
            when (ui) {
                UiState.Idle -> Unit
                UiState.Loading -> showLoading()
                is UiState.Success -> {
                    hideLoading()
                    bindStore(ui.data)
                }
                is UiState.Error -> {
                    hideLoading()
                    Toast.makeText(requireContext(), ui.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /** Bind dữ liệu browse: banner hero + 5 small banner (featured) + danh sách section động. */
    private fun bindStore(store: Discovery) {
        val hero = store.featured.firstOrNull()
            ?: store.hot.firstOrNull()
            ?: store.recommended.firstOrNull()
            ?: store.sections.firstOrNull()?.games?.firstOrNull()
        if (hero != null) {
            bindBanner(hero)
        }
        bindSmallBanners(store.featured)
        binding.rvSections.adapter = SectionGameAdapter(store.sections) { openPlayGame(it) }
    }

    /** Index card small banner đang được chọn; -1 nếu chưa có. Dùng để phân biệt tap chọn vs tap mở. */
    private var selectedBannerIndex = -1

    /** Bind tối đa 5 card nhỏ trong hsvSmallBanners từ data.featured; card dư (thiếu data) thì ẩn. */
    private fun bindSmallBanners(featured: List<Game>) {
        val cards = listOf(binding.card1, binding.card2, binding.card3, binding.card4, binding.card5)
        val images = listOf(
            binding.ivCard1, binding.ivCard2, binding.ivCard3, binding.ivCard4, binding.ivCard5
        )

        selectedBannerIndex = -1

        val visibleCount = featured.size.coerceAtMost(5)
        val emptyWeight = 5f - visibleCount
        (binding.spaceStart.layoutParams as android.widget.LinearLayout.LayoutParams).weight = emptyWeight / 2f
        (binding.spaceEnd.layoutParams as android.widget.LinearLayout.LayoutParams).weight = emptyWeight / 2f

        cards.forEachIndexed { index, card ->
            val game = featured.getOrNull(index)
            if (game == null) {
                card.visibility = View.GONE
            } else {
                card.visibility = View.VISIBLE
                images[index].loadCover(game.getGameImage())
                // Tap lần đầu chỉ chọn card (đổi banner); chỉ tap vào card đang được chọn mới mở game.
                card.setOnClickListener {
                    if (selectedBannerIndex == index) {
                        openPlayGame(game)
                    } else {
                        selectedBannerIndex = index
                        bindBanner(game)
                    }
                }
                // Chỉ coi focus = "đã chọn" khi đến từ D-pad/bàn phím (không phải touch mode).
                // Touch: việc chọn/mở do click handler ở trên quản lý (tap đầu chọn, tap sau mở).
                card.setOnFocusChangeListener { v, hasFocus ->
                    if (hasFocus && !v.isInTouchMode) {
                        selectedBannerIndex = index
                        bindBanner(game)
                    }
                }
            }
        }
    }

    private fun bindBanner(hero: Game) {
        binding.tvBannerTitle.text = hero.title
        binding.tvBannerDesc.text = hero.getShortDescriptionExt()
        binding.ivBannerBg.loadCover(hero.getGameBackground())
    }

    /** Mở PlayGameActivity, kèm id/slug của [game] khi có (mỗi small banner mở đúng game của nó). */
    private fun openPlayGame(game: Game? = null) {
        val intent = Intent(requireContext(), PlayGameActivity::class.java).apply {
            putExtra(PlayGameActivity.EXTRA_GAME_ID, game?.id ?: -1)
            putExtra(PlayGameActivity.EXTRA_GAME_SLUG, game?.slug.orEmpty())
        }
        startActivity(intent)
    }

    private fun setupCardClicks() {
        val cards = listOf(binding.card1, binding.card2, binding.card3, binding.card4, binding.card5)
        cards.forEach { card -> card.addScaleClickEffect() }
    }

    private var isFooterHidden = false
    private val scrollHandler = Handler(Looper.getMainLooper())
    private val showFooterRunnable = Runnable {
        if (view == null) return@Runnable
        isFooterHidden = false
        binding.footer.animate()
            .translationX(0f)
            .alpha(1f)
            .setDuration(500)
            .start()
    }

    private fun setupScrollEffect() {
        val bgColor = ContextCompat.getColor(requireContext(), R.color.bg_color)

        // Initial state
        binding.llPlatform.setBackgroundColor(ColorUtils.setAlphaComponent(bgColor, 0))

        binding.llPlatform.post {
            binding.scrollView.topOffset = binding.llPlatform.height
        }

        binding.scrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            // Assume 500px is the maximum scroll distance to completely fade out the banner
            val maxScrollBanner = 500f

            val fractionBanner = (scrollY / maxScrollBanner).coerceIn(0f, 1f)

            // Background image and details fade out
            val imageAlpha = 1f - fractionBanner
            binding.ivBannerBg.alpha = imageAlpha
            binding.vGradient.alpha = imageAlpha
            binding.tvBannerTitle.alpha = imageAlpha
            binding.tvBannerDesc.alpha = imageAlpha
            binding.ivWindows.alpha = imageAlpha

            // Set platform background only when banner is completely hidden
            if (imageAlpha <= 0f) {
                binding.llPlatform.setBackgroundColor(bgColor)
            } else {
                binding.llPlatform.setBackgroundColor(ColorUtils.setAlphaComponent(bgColor, 0))
            }

            // Footer animation logic
            if (!isFooterHidden) {
                isFooterHidden = true
                binding.footer.animate()
                    .translationX(binding.footer.width.toFloat() + 100f)
                    .alpha(0f)
                    .setDuration(500)
                    .start()
            }

            scrollHandler.removeCallbacks(showFooterRunnable)
            scrollHandler.postDelayed(showFooterRunnable, 500)
        }
    }

    override fun onDestroyView() {
        scrollHandler.removeCallbacks(showFooterRunnable)
        super.onDestroyView()
    }

    private companion object {
        const val STORE_STEAM = "steam"
        const val STORE_EPIC = "epic"
        const val STORE_GOG = "gog"
    }
}
