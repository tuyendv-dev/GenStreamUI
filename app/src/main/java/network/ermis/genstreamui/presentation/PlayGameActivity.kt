package network.ermis.genstreamui.presentation

import dagger.hilt.android.AndroidEntryPoint

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import network.ermis.genstreamui.databinding.ActivityPlayGameBinding

@AndroidEntryPoint
class PlayGameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayGameBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityPlayGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Immersive landscape setup
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        // Back navigation
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Zoom out animation for artwork
        binding.ivGameArtwork.scaleX = 1.3f
        binding.ivGameArtwork.scaleY = 1.3f
        binding.ivGameArtwork.animate()
            .scaleX(1.0f)
            .scaleY(1.0f)
            .setDuration(1500)
            .start()

        // Initial state for compatibility badge
        binding.btnCheckCompatibility.visibility = android.view.View.GONE
        binding.btnCheckCompatibility.alpha = 0f

        // Sequential fade-in animation for game details
        val detailsViews = mutableListOf<android.view.View>()
        for (i in 0 until binding.llGameDetails.childCount) {
            detailsViews.add(binding.llGameDetails.getChildAt(i))
        }

        val totalDuration = 1500L
        val animDuration = 500L
        val delayStep = if (detailsViews.size > 1) (totalDuration - animDuration) / (detailsViews.size - 1) else 0L

        detailsViews.forEachIndexed { index, view ->
            view.alpha = 0f
            view.translationY = 30f
            view.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(animDuration)
                .setStartDelay(index * delayStep)
                .start()
        }

        binding.btnPlayNow.addScaleClickEffect()
        binding.btnMore.addScaleClickEffect()
        binding.btnCheckCompatibility.addScaleClickEffect()

        val hideCompatibilityBadge = {
            if (binding.btnCheckCompatibility.visibility == android.view.View.VISIBLE) {
                binding.btnCheckCompatibility.animate()
                    .alpha(0f)
                    .translationY(20f)
                    .setDuration(200)
                    .withEndAction {
                        binding.btnCheckCompatibility.visibility = android.view.View.GONE
                    }
                    .start()
            }
        }

        binding.btnMore.setOnClickListener {
            if (binding.btnCheckCompatibility.visibility == android.view.View.GONE) {
                binding.btnCheckCompatibility.visibility = android.view.View.VISIBLE
                binding.btnCheckCompatibility.translationY = 20f
                binding.btnCheckCompatibility.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(200)
                    .withEndAction(null)
                    .start()
            } else {
                hideCompatibilityBadge()
            }
        }

        // Hide when clicking anywhere else
        binding.root.setOnClickListener { hideCompatibilityBadge() }
        binding.ivGameArtwork.setOnClickListener { hideCompatibilityBadge() }
    }
}
