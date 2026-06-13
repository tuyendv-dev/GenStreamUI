package network.ermis.genstreamui.presentation.home.discovery

import android.content.Intent
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.view.FocusFinder
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import dagger.hilt.android.AndroidEntryPoint
import network.ermis.genstreamui.R
import network.ermis.genstreamui.common.UiState
import network.ermis.genstreamui.common.base.BaseFragment
import network.ermis.genstreamui.common.base.ext.collectWhenStarted
import network.ermis.genstreamui.common.base.ext.loadCover
import network.ermis.genstreamui.databinding.FragmentDiscoveryBinding
import network.ermis.genstreamui.domain.model.Discovery
import network.ermis.genstreamui.domain.model.Game
import network.ermis.genstreamui.domain.model.extension.getShortDescriptionExt
import network.ermis.genstreamui.presentation.PlayGameActivity
import network.ermis.genstreamui.presentation.addScaleClickEffect
import network.ermis.genstreamui.presentation.home.adapter.SectionGameAdapter

@AndroidEntryPoint
class DiscoveryFragment :
    BaseFragment<FragmentDiscoveryBinding>(FragmentDiscoveryBinding::inflate) {

    private val viewModel: DiscoveryViewModel by viewModels()

    // Game đang gắn với 2 card phụ bên phải (set khi bind), để click mở đúng game.
    private var topSideGame: Game? = null
    private var bottomSideGame: Game? = null

    private val slideHandler = Handler(Looper.getMainLooper())
    private val slideRunnable = Runnable {
        if (view == null) return@Runnable
        val vp = binding.vpMainBanner
        val adapter = vp.adapter
        if (adapter != null && adapter.itemCount > 0) {
            var nextItem = vp.currentItem + 1
            if (nextItem >= adapter.itemCount) {
                nextItem = 0
            }
            vp.setCurrentItem(nextItem, true)
        }
    }

    private var isFooterHidden = false
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
        binding.scrollView.setOnScrollChangeListener { _, _, _, _, _ ->
            if (!isFooterHidden) {
                isFooterHidden = true
                binding.footer.animate()
                    .translationX(binding.footer.width.toFloat() + 100f)
                    .alpha(0f)
                    .setDuration(500)
                    .start()
            }

            slideHandler.removeCallbacks(showFooterRunnable)
            slideHandler.postDelayed(showFooterRunnable, 500)
        }
    }

    private val strictRowFocusListener = View.OnKeyListener { v, keyCode, event ->
        if (event.action == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                val direction = if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) View.FOCUS_LEFT else View.FOCUS_RIGHT
                val nextFocus = FocusFinder.getInstance().findNextFocus(v.rootView as ViewGroup, v, direction)
                if (nextFocus != null) {
                    val currentRect = Rect()
                    v.getGlobalVisibleRect(currentRect)
                    val nextRect = Rect()
                    nextFocus.getGlobalVisibleRect(nextRect)

                    if (currentRect.bottom <= nextRect.top || currentRect.top >= nextRect.bottom) {
                        return@OnKeyListener true
                    }
                } else {
                    return@OnKeyListener true
                }
            }
        }
        false
    }

    private fun applyStrictRowFocus(view: View) {
        if (view.isFocusable) {
            view.setOnKeyListener(strictRowFocusListener)
        }
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                applyStrictRowFocus(view.getChildAt(i))
            }
        }
    }

    override fun initViews() {
        setupSideBannerClicks()
        setupScrollEffect()

        applyStrictRowFocus(binding.bannerSection)

        viewModel.loadDiscovery()
    }

    override fun observerData() {
        collectWhenStarted(viewModel.events) { ui ->
            when (ui) {
                UiState.Idle -> Unit
                UiState.Loading -> showLoading()
                is UiState.Success -> {
                    hideLoading()
                    bindDiscovery(ui.data)
                }
                is UiState.Error -> {
                    hideLoading()
                    Toast.makeText(requireContext(), ui.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /** Bind toàn bộ dữ liệu discovery vào carousel, 2 card phụ và danh sách section. */
    private fun bindDiscovery(discovery: Discovery) {
        bindMainBanner(discovery.featured)
        bindSideBanners(discovery.featured)
        bindSections(discovery)
    }

    /** Mở PlayGameActivity, kèm id/slug của [game] khi có (để màn Play load đúng game). */
    private fun openPlayGame(game: Game? = null) {
        val intent = Intent(requireContext(), PlayGameActivity::class.java).apply {
            putExtra(PlayGameActivity.EXTRA_GAME_ID, game?.id ?: -1)
            putExtra(PlayGameActivity.EXTRA_GAME_SLUG, game?.slug.orEmpty())
        }
        startActivity(intent)
    }

    private fun bindMainBanner(featured: List<Game>) {
        binding.vpMainBanner.adapter = DiscoveryBannerAdapter(featured) { openPlayGame(it) }

        setupIndicators(featured.size)
        if (featured.isNotEmpty()) setCurrentIndicator(0)

        binding.vpMainBanner.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setCurrentIndicator(position)
                slideHandler.removeCallbacks(slideRunnable)
                slideHandler.postDelayed(slideRunnable, 4000)
            }
        })
    }

    /**
     * 2 card phụ bên phải: nếu featured có <= 3 item thì lấy 2 item cuối, ngược lại lấy [1], [2].
     * Ảnh dùng header_image, title = title, desc = short_description (fallback tagline).
     */
    private fun bindSideBanners(featured: List<Game>) {
        val sideGames = if (featured.size <= 3) {
            featured.takeLast(2)
        } else {
            listOf(featured[1], featured[2])
        }
        topSideGame = sideGames.getOrNull(0)
        bottomSideGame = sideGames.getOrNull(1)
        topSideGame?.let {
            binding.topSideBannerImage.loadCover(it.headerImage)
            binding.tvTopBannerTitle.text = it.title
            binding.tvTopBannerDesc.text = it.getShortDescriptionExt()
        }
        bottomSideGame?.let {
            binding.bottomSideBannerImage.loadCover(it.headerImage)
            binding.tvBottomBannerTitle.text = it.title
            binding.tvBottomBannerDesc.text = it.getShortDescriptionExt()
        }
    }

    private fun bindSections(discovery: Discovery) {
        binding.rvSections.adapter =
            SectionGameAdapter(discovery.sections) { openPlayGame(it) }
    }

    private fun setupSideBannerClicks() {
        binding.cvTopSideBannerImage.addScaleClickEffect()
        binding.cvTopSideBannerImage.setOnClickListener { openPlayGame(topSideGame) }
        binding.cvBottomSideBannerImage.addScaleClickEffect()
        binding.cvBottomSideBannerImage.setOnClickListener { openPlayGame(bottomSideGame) }
    }

    private fun setupIndicators(count: Int) {
        val indicators = arrayOfNulls<ImageView>(count)
        val layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(4, 0, 4, 0)

        binding.llIndicator.removeAllViews()
        for (i in indicators.indices) {
            indicators[i] = ImageView(requireContext())
            indicators[i]?.setImageDrawable(
                ContextCompat.getDrawable(requireContext(), R.drawable.dot_unselected)
            )
            indicators[i]?.layoutParams = layoutParams
            binding.llIndicator.addView(indicators[i])
        }
    }

    private fun setCurrentIndicator(index: Int) {
        val childCount = binding.llIndicator.childCount
        for (i in 0 until childCount) {
            val imageView = binding.llIndicator.getChildAt(i) as ImageView
            if (i == index) {
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(requireContext(), R.drawable.dot_selected)
                )
            } else {
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(requireContext(), R.drawable.dot_unselected)
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        slideHandler.postDelayed(slideRunnable, 1500)
    }

    override fun onPause() {
        super.onPause()
        slideHandler.removeCallbacks(slideRunnable)
    }

    override fun onDestroyView() {
        slideHandler.removeCallbacks(slideRunnable)
        slideHandler.removeCallbacks(showFooterRunnable)
        super.onDestroyView()
    }
}
