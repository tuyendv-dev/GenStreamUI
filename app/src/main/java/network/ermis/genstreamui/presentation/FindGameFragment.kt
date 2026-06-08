package network.ermis.genstreamui.presentation

import dagger.hilt.android.AndroidEntryPoint

import network.ermis.genstreamui.R

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.Fragment
import network.ermis.genstreamui.databinding.FragmentFindGameBinding

@AndroidEntryPoint
class FindGameFragment : Fragment() {

    private var _binding: FragmentFindGameBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFindGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerViews()
        setupScrollEffect()
        setupCardClicks()
    }

    private fun setupCardClicks() {
        val cards = listOf(binding.card1, binding.card2, binding.card3, binding.card4, binding.card5)
        cards.forEach { card ->
            card.addScaleClickEffect()
            card.setOnClickListener {
                val intent = android.content.Intent(requireContext(), PlayGameActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private var isFooterHidden = false
    private val scrollHandler = android.os.Handler(android.os.Looper.getMainLooper())
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
        val bgColor = ContextCompat.getColor(requireContext(), R.color.bg_color)
        
        // Initial state
        binding.llPlatform.setBackgroundColor(ColorUtils.setAlphaComponent(bgColor, 0))

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

    private fun setupRecyclerViews() {
        val steamGames = listOf(
            GameModel("The Witcher 3: Wild Hunt Hunt Hunt Hunt", "Slay monster/ Find Ciri. Sha da sd akjdnsd", R.drawable.image_11),
            GameModel("The Witcher 3: Wild Hunt Hunt Hunt Hunt", "Slay monster/ Find Ciri. Sha da sd akjdnsd", R.drawable.image_3),
            GameModel("The Witcher 3: Wild Hunt Hunt Hunt Hunt", "Slay monster/ Find Ciri. Sha da sd akjdnsd", R.drawable.image_4),
            GameModel("NieR: Automata", "Slay monster/ Find Ciri. Sha da sd akjdnsd", R.drawable.image_5),
            GameModel("The Witcher 3: Wild Hunt Hunt Hunt Hunt", "Slay monster/ Find Ciri. Sha da sd akjdnsd", R.drawable.image_11),
            GameModel("The Witcher 3: Wild Hunt Hunt Hunt Hunt", "Slay monster/ Find Ciri. Sha da sd akjdnsd", R.drawable.image_3),
            GameModel("The Witcher 3: Wild Hunt Hunt Hunt Hunt", "Slay monster/ Find Ciri. Sha da sd akjdnsd", R.drawable.image_4),
            GameModel("NieR: Automata", "Slay monster/ Find Ciri. Sha da sd akjdnsd", R.drawable.image_5)
        )

        val fightingGames = listOf(
            GameModel("The Witcher 3: Wild Hunt Hunt Hunt Hunt", "Slay monster/ Find Ciri. Sha da sd akjdnsd", R.drawable.image_11),
            GameModel("The Witcher 3: Wild Hunt Hunt Hunt Hunt", "Slay monster/ Find Ciri. Sha da sd akjdnsd", R.drawable.image_3),
            GameModel("The Witcher 3: Wild Hunt Hunt Hunt Hunt", "Slay monster/ Find Ciri. Sha da sd akjdnsd", R.drawable.image_4),
            GameModel("NieR: Automata", "Slay monster/ Find Ciri. Sha da sd akjdnsd", R.drawable.image_5),
            GameModel("The Witcher 3: Wild Hunt Hunt Hunt Hunt", "Slay monster/ Find Ciri. Sha da sd akjdnsd", R.drawable.image_11),
            GameModel("The Witcher 3: Wild Hunt Hunt Hunt Hunt", "Slay monster/ Find Ciri. Sha da sd akjdnsd", R.drawable.image_3),
            GameModel("The Witcher 3: Wild Hunt Hunt Hunt Hunt", "Slay monster/ Find Ciri. Sha da sd akjdnsd", R.drawable.image_4),
            GameModel("NieR: Automata", "Slay monster/ Find Ciri. Sha da sd akjdnsd", R.drawable.image_5)
        )

        val steamAdapter = GameAdapter(steamGames)
        binding.rvSteamShooter.adapter = steamAdapter

        val fightingAdapter = GameAdapter(fightingGames)
        binding.rvFighting.adapter = fightingAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
