package network.ermis.genstreamui.presentation

import dagger.hilt.android.AndroidEntryPoint

import network.ermis.genstreamui.R

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import network.ermis.genstreamui.databinding.FragmentDiscoveryBinding

@AndroidEntryPoint
class DiscoveryFragment : Fragment() {

    private var _binding: FragmentDiscoveryBinding? = null
    private val binding get() = _binding!!

    private val slideHandler = Handler(Looper.getMainLooper())
    private val slideRunnable = Runnable {
        _binding?.let {
            val vp = it.vpMainBanner
            val adapter = vp.adapter
            if (adapter != null && adapter.itemCount > 0) {
                var nextItem = vp.currentItem + 1
                if (nextItem >= adapter.itemCount) {
                    nextItem = 0
                }
                vp.setCurrentItem(nextItem, true)
            }
        }
    }

    private var isFooterHidden = false
    private val showFooterRunnable = Runnable {
        _binding?.let {
            isFooterHidden = false
            it.footer.animate()
                .translationX(0f)
                .alpha(1f)
                .setDuration(500)
                .start()
        }
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDiscoveryBinding.inflate(inflater, container, false)
        return binding.root
    }

    private val strictRowFocusListener = View.OnKeyListener { v, keyCode, event ->
        if (event.action == android.view.KeyEvent.ACTION_DOWN) {
            if (keyCode == android.view.KeyEvent.KEYCODE_DPAD_LEFT || keyCode == android.view.KeyEvent.KEYCODE_DPAD_RIGHT) {
                val direction = if (keyCode == android.view.KeyEvent.KEYCODE_DPAD_LEFT) View.FOCUS_LEFT else View.FOCUS_RIGHT
                val nextFocus = android.view.FocusFinder.getInstance().findNextFocus(v.rootView as ViewGroup, v, direction)
                if (nextFocus != null) {
                    val currentRect = android.graphics.Rect()
                    v.getGlobalVisibleRect(currentRect)
                    val nextRect = android.graphics.Rect()
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupBanners()
        setupSideBanners()
        setupRecyclerViews()
        setupScrollEffect()
        
        applyStrictRowFocus(binding.bannerSection)
    }

    private fun setupBanners() {
        val banners = listOf(
            GameModel("Atomic Heart", "Stream game Recomendation", R.drawable.image_1),
            GameModel("The Witcher 3: Wild Hunt", "Stream game Recomendation", R.drawable.image_1),
            GameModel("Call of Duty: Mobile", "Stream game Recomendation", R.drawable.image_1),
            GameModel("Dead or Alive 6", "Stream game Recomendation", R.drawable.image_1),
            GameModel("NieR: Automata", "Stream game Recomendation", R.drawable.image_1)
        )

        val bannerAdapter = BannerAdapter(banners)
        binding.vpMainBanner.adapter = bannerAdapter

        setupIndicators(banners.size)
        if (banners.isNotEmpty()) setCurrentIndicator(0)

        binding.vpMainBanner.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setCurrentIndicator(position)
                slideHandler.removeCallbacks(slideRunnable)
                slideHandler.postDelayed(slideRunnable, 2000)
            }
        })
        binding.cvTopSideBannerImage.addScaleClickEffect()
        binding.cvTopSideBannerImage.setOnClickListener {
            val intent = android.content.Intent(requireContext(), PlayGameActivity::class.java)
            startActivity(intent)
        }
        binding.cvBottomSideBannerImage.addScaleClickEffect()
        binding.cvBottomSideBannerImage.setOnClickListener {
            val intent = android.content.Intent(requireContext(), PlayGameActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupSideBanners() {
        val topSideCard = binding.sideBanners.getChildAt(0)
        val bottomSideCard = binding.sideBanners.getChildAt(1)

        val sideCards = listOf(topSideCard, bottomSideCard)
        sideCards.forEach { card ->
            card?.addScaleClickEffect()
            card?.setOnClickListener {
                val intent = android.content.Intent(requireContext(), PlayGameActivity::class.java)
                startActivity(intent)
            }
        }
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

    private fun setupRecyclerViews() {
        val adventureGames = listOf(
            GameModel("The Witcher 3: Wild Hunt Hunt Hunt Hunt", "Slay monster/ Find Ciri. Sha da sd akjdnsd", R.drawable.image_4),
            GameModel("The Witcher 3: Wild Hunt Hunt Hunt Hunt", "Slay monster/ Find Ciri. Sha da sd akjdnsd", R.drawable.image_2),
            GameModel("The Witcher 3: Wild Hunt Hunt Hunt Hunt", "Slay monster/ Find Ciri. Sha da sd akjdnsd", R.drawable.image_11),
            GameModel("NieR: Automata", "Slay monster/ Game...", R.drawable.image_5)
        )

        val fightingGames = listOf(
            GameModel("The Witcher 3: Wild Hunt Hunt Hunt Hunt", "Slay monster/ Find Ciri. Sha da sd akjdnsd", R.drawable.image_4),
            GameModel("The Witcher 3: Wild Hunt Hunt Hunt Hunt", "Slay monster/ Find Ciri. Sha da sd akjdnsd", R.drawable.image_2),
            GameModel("The Witcher 3: Wild Hunt Hunt Hunt Hunt", "Slay monster/ Find Ciri. Sha da sd akjdnsd", R.drawable.image_11),
            GameModel("NieR: Automata", "Slay monster/ Find Ciri. Sha da sd akjdnsd", R.drawable.image_5),
            GameModel("The Witcher 3: Wild Hunt Hunt Hunt Hunt", "Slay monster/ Find Ciri. Sha da sd akjdnsd", R.drawable.image_4),
            GameModel("The Witcher 3: Wild Hunt Hunt Hunt Hunt", "Slay monster/ Find Ciri. Sha da sd akjdnsd", R.drawable.image_2),
            GameModel("The Witcher 3: Wild Hunt Hunt Hunt Hunt", "Slay monster/ Find Ciri. Sha da sd akjdnsd", R.drawable.image_11),
            GameModel("NieR: Automata", "Slay monster/ Find Ciri. Sha da sd akjdnsd", R.drawable.image_5)
        )

        val adventureAdapter = GameAdapter(adventureGames)
        binding.rvAdventure.adapter = adventureAdapter

        val fightingAdapter = GameAdapter(fightingGames)
        binding.rvFighting.adapter = fightingAdapter

        val childAttachListener = object : androidx.recyclerview.widget.RecyclerView.OnChildAttachStateChangeListener {
            override fun onChildViewAttachedToWindow(view: View) {
                applyStrictRowFocus(view)
            }
            override fun onChildViewDetachedFromWindow(view: View) {}
        }
        binding.rvAdventure.addOnChildAttachStateChangeListener(childAttachListener)
        binding.rvFighting.addOnChildAttachStateChangeListener(childAttachListener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
