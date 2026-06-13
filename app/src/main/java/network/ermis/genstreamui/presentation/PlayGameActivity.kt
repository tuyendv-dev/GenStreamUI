package network.ermis.genstreamui.presentation

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import network.ermis.genstreamui.common.UiState
import network.ermis.genstreamui.common.base.ext.loadCover
import network.ermis.genstreamui.databinding.ActivityPlayGameBinding
import network.ermis.genstreamui.domain.model.Game
import network.ermis.genstreamui.domain.model.extension.getGameBackground
import network.ermis.genstreamui.domain.model.extension.getShortDescriptionExt

import network.ermis.genstreamui.presentation.widget.setupStatusIcons

@AndroidEntryPoint
class PlayGameActivity : AppCompatActivity() {

    private val TAG: String = "PlayGameActivity"
    private lateinit var binding: ActivityPlayGameBinding

    private val viewModel: PlayGameViewModel by viewModels()

    // Đảm bảo animation zoom-out của artwork chỉ chạy 1 lần (bindGame có thể gọi 2 lần: cache + API).
    private var artworkZoomed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityPlayGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Immersive landscape setup
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        // Setup status icons
        setupStatusIcons(
            binding = binding.statusIcons,
            showSearch = false,
            showGamepad = false
        )

        // Back navigation
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Pre-zoom artwork; animation zoom-out sẽ chạy khi ảnh load xong (xem bindGame).
        binding.ivGameArtwork.scaleX = 1.3f
        binding.ivGameArtwork.scaleY = 1.3f

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

        observeGameDetail()

        // gameId nhận từ màn khác gửi sang; chỉ load khi có id hợp lệ.
        val gameId = intent.getIntExtra(EXTRA_GAME_ID, -1)
        if (gameId > 0) {
            viewModel.loadGameDetail(gameId)
        }
    }

    /**
     * Quan sát trạng thái chi tiết game: Success phát 2 lần — lần đầu là game đã cache trong
     * Database (hiển thị ngay trước khi API xong), lần sau là data mới từ API (đã được ghi lại DB).
     */
    private fun observeGameDetail() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { ui ->
                    when (ui) {
                        UiState.Idle, UiState.Loading -> Unit
                        is UiState.Success -> bindGame(ui.data)
                        is UiState.Error ->
                            Toast.makeText(this@PlayGameActivity, ui.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    /** Bind dữ liệu game vào UI (title, mô tả, ảnh artwork). */
    private fun bindGame(game: Game) {
        binding.tvGameTitle.text = game.title
        binding.tvGameDesc.text = game.getShortDescriptionExt()

        binding.ivGameArtwork.loadCover(game.getGameBackground()) { zoomOutArtwork() }
    }

    /** Chạy animation zoom-out cho artwork khi ảnh đã load xong (chỉ chạy 1 lần). */
    private fun zoomOutArtwork() {
        if (artworkZoomed) return
        artworkZoomed = true
        binding.ivGameArtwork.animate()
            .scaleX(1.0f)
            .scaleY(1.0f)
            .setDuration(1500)
            .start()
    }

    companion object {
        /** Game id (Int) của game được chọn; -1 nếu mở không gắn game cụ thể. */
        const val EXTRA_GAME_ID = "extra_game_id"

        /** Game slug của game được chọn; rỗng nếu mở không gắn game cụ thể. */
        const val EXTRA_GAME_SLUG = "extra_game_slug"
    }
}
