package network.ermis.genstreamui.presentation

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.doOnLayout
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
import network.ermis.genstreamui.presentation.preview.PreviewMediaActivity
import network.ermis.genstreamui.presentation.widget.setupStatusIcons
import network.ermis.genstreamui.presentation.windows.WindowsConnectActivity

@AndroidEntryPoint
class PlayGameActivity : AppCompatActivity() {

    private val TAG: String = "PlayGameActivity"
    private lateinit var binding: ActivityPlayGameBinding

    private val viewModel: PlayGameViewModel by viewModels()

    // Đảm bảo animation zoom-out của artwork chỉ chạy 1 lần (bindGame có thể gọi 2 lần: cache + API).
    private var artworkZoomed = false

    // Game đang hiển thị (set khi bindGame), để bấm Play Now mở kết nối đúng game.
    private var currentGame: Game? = null

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

        // Nhóm header (title → Check Compatibility) cao tối thiểu = 1 màn để luôn căn giữa theo chiều dọc;
        // phần nội dung phía dưới chỉ hiện khi người dùng vuốt lên.
        binding.nsvGameDetails.doOnLayout {
            binding.llHeaderGroup.minimumHeight = it.height
        }

        // Tên game ở topBar hiện dần khi tvGameTitle bị cuộn che; artwork blur dần khi header trôi hết.
        binding.nsvGameDetails.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            updateTopBarTitleAlpha()
            updateArtworkBlur(scrollY)
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

        // Play Now → kết nối VM rồi stream THẲNG vào game (không qua lưới AppView).
        binding.btnPlayNow.setOnClickListener { openWindowsConnectForPlay() }

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

    /**
     * Nạp HTML detailed_description vào WebView. Bọc CSS để chữ trắng/nền trong suốt hợp theme tối
     * và ảnh/video co theo bề ngang; cho video tự phát (muted) không cần chạm.
     */
    @android.annotation.SuppressLint("SetJavaScriptEnabled")
    private fun bindDetailedDescription(html: String) {
        val web = binding.wvDetailedDescription
        web.settings.javaScriptEnabled = true
        web.settings.mediaPlaybackRequiresUserGesture = false
        web.settings.loadWithOverviewMode = true
        web.settings.useWideViewPort = false
        web.setBackgroundColor(android.graphics.Color.TRANSPARENT)
        web.isVerticalScrollBarEnabled = false

        // Chặn bôi đen chữ / menu copy: nuốt long-press và vô hiệu long-click ở tầng Android
        // (CSS user-select:none ở dưới lo phần còn lại trong trang).
        web.setOnLongClickListener { true }
        web.isLongClickable = false
        web.isHapticFeedbackEnabled = false

        // WebView render CSS px ≈ dp; sp = dp * fontScale nên nhân fontScale để chữ tính theo sp.
        // Body 14sp, tiêu đề bị cắt còn tối đa 14sp (HTML Steam có h2 mặc định rất to).
        val fontScale = resources.configuration.fontScale
        val bodyFontPx = (14f * fontScale).toInt().coerceAtLeast(1)
        val maxFontPx = (14f * fontScale).toInt().coerceAtLeast(1)

        val document = """
            <!DOCTYPE html>
            <html>
            <head>
              <meta charset="utf-8">
              <meta name="viewport" content="width=device-width, initial-scale=1">
              <style>
                /* Chặn người dùng bôi đen/copy nội dung */
                * { -webkit-user-select:none; -khtml-user-select:none; -moz-user-select:none;
                    -ms-user-select:none; user-select:none; -webkit-touch-callout:none;
                    -webkit-tap-highlight-color:transparent; }
                body { margin:0; padding:0; background:transparent; color:#FFFFFF;
                       font-family:sans-serif; font-size:${bodyFontPx}px; line-height:1.6; word-wrap:break-word; }
                img, video { max-width:100%; height:auto; border-radius:8px; }
                a { color:#4DA3FF; }
                h1, h2, h3, h4, h5, h6 { color:#FFFFFF; font-size:${maxFontPx}px; }
              </style>
            </head>
            <body>$html</body>
            </html>
        """.trimIndent()

        web.loadDataWithBaseURL(
            "https://store.steampowered.com",
            document,
            "text/html",
            "UTF-8",
            null
        )
    }

    /**
     * Cập nhật độ mờ tên game ở topBar theo mức tvGameTitle bị topBar che:
     * chưa che -> alpha 0; che dần -> tăng dần; che hết (qua hết chiều cao title) -> alpha 1.
     */
    private fun updateTopBarTitleAlpha() {
        val titleLoc = IntArray(2)
        binding.tvGameTitle.getLocationInWindow(titleLoc)
        val barLoc = IntArray(2)
        binding.topBar.getLocationInWindow(barLoc)

        val titleBottom = titleLoc[1] + binding.tvGameTitle.height
        val barBottom = barLoc[1] + binding.topBar.height
        val fadeDistance = binding.tvGameTitle.height.coerceAtLeast(1).toFloat()

        // barBottom - titleBottom: dương dần khi title trôi lên dưới topBar.
        val alpha = ((barBottom - titleBottom) / fadeDistance).coerceIn(0f, 1f)
        binding.tvTopBarTitle.alpha = alpha
    }

    /**
     * Blur artwork nền: bắt đầu khi [llHeaderGroup] đã bị cuộn hết khỏi màn (scrollY > đáy header),
     * tăng dần tới [MAX_BLUR_RADIUS]. Chỉ áp dụng từ API 31 (RenderEffect); thấp hơn thì bỏ qua.
     */
    private fun updateArtworkBlur(scrollY: Int) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S) return

        val over = (scrollY - binding.llHeaderGroup.bottom).coerceAtLeast(0).toFloat()
        val radius = (over / BLUR_FULL_DISTANCE_PX).coerceIn(0f, 1f) * MAX_BLUR_RADIUS
        binding.flGameArtwork.setRenderEffect(
            if (radius > 0f) {
                android.graphics.RenderEffect.createBlurEffect(
                    radius, radius, android.graphics.Shader.TileMode.CLAMP
                )
            } else {
                null
            }
        )
    }

    /** Bind cụm thông tin Age Rating / Language / Developer / Release Date. */
    private fun bindGameInfo(game: Game) {
        binding.tvAgeRating.text = if (game.requiredAge > 0) game.requiredAge.toString() else "—"

        // supported_languages là HTML + nhiều ngôn ngữ cách nhau bằng dấu phẩy; bỏ tag, tách danh sách.
        // Hiển thị ngôn ngữ đầu + "+N" với N = số ngôn ngữ còn lại (nếu có).
        val languages = game.supportedLanguages
            .replace(Regex("<[^>]*>"), "")
            .split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
        binding.tvLanguage.text = when {
            languages.isEmpty() -> "—"
            languages.size == 1 -> languages.first()
            else -> "${languages.first()} ${languages.size - 1}+"
        }

        binding.tvDeveloper.text =
            game.developers.firstOrNull()?.takeIf { it.isNotBlank() }
                ?: game.publisher.ifBlank { "—" }

        binding.tvReleaseDate.text = game.releaseDateText.ifBlank {
            if (game.releaseYear > 0) game.releaseYear.toString() else "—"
        }
    }

    /** Bind dữ liệu game vào UI (title, mô tả, ảnh artwork). */
    private fun bindGame(game: Game) {
        currentGame = game
        binding.tvGameTitle.text = game.title
        binding.tvTopBarTitle.text = game.title
        binding.tvGameDesc.text = game.getShortDescriptionExt()

        bindGameInfo(game)

        if (game.description.isNotBlank()) {
            binding.tvDescriptionTitle.visibility = android.view.View.VISIBLE
            binding.tvGameFullDesc.visibility = android.view.View.VISIBLE
            binding.tvGameFullDesc.text = game.description
        } else {
            binding.tvDescriptionTitle.visibility = android.view.View.GONE
            binding.tvGameFullDesc.visibility = android.view.View.GONE
        }

        if (game.detailedDescription.isNotBlank()) {
            binding.tvMoreTitle.visibility = android.view.View.VISIBLE
            binding.wvDetailedDescription.visibility = android.view.View.VISIBLE
            bindDetailedDescription(game.detailedDescription)
        } else {
            binding.tvMoreTitle.visibility = android.view.View.GONE
            binding.wvDetailedDescription.visibility = android.view.View.GONE
        }

        // Gallery: trailers (thumbnail + nút play) lên đầu, sau đó tới screenshots.
        val galleryItems = game.trailers.map { GalleryItem.Trailer(it) } +
            game.screenshots.map { GalleryItem.Screenshot(it) }
        if (galleryItems.isNotEmpty()) {
            binding.tvVideoGalleryTitle.visibility = android.view.View.VISIBLE
            binding.rvScreenshots.visibility = android.view.View.VISIBLE
            binding.rvScreenshots.adapter = GalleryAdapter(galleryItems)
        } else {
            binding.tvVideoGalleryTitle.visibility = android.view.View.GONE
            binding.rvScreenshots.visibility = android.view.View.GONE
        }

        if (game.legalNotice.isNotBlank()) {
            binding.tvLegalNotice.visibility = android.view.View.VISIBLE
            binding.tvLegalNotice.text = game.legalNotice
        } else {
            binding.tvLegalNotice.visibility = android.view.View.GONE
        }

        binding.ivGameArtwork.loadCover(game.getGameBackground()) { zoomOutArtwork() }

        bindPlatforms(game.platforms)

        // Vào màn -> luôn cuộn lên đầu (tránh RecyclerView con kéo focus làm lệch vị trí).
        binding.nsvGameDetails.post { binding.nsvGameDetails.scrollTo(0, 0) }
    }

    /** Phần tử của video gallery: trailer (có thumbnail + nút play) hoặc screenshot (chỉ ảnh). */
    private sealed interface GalleryItem {
        data class Trailer(val trailer: network.ermis.genstreamui.domain.model.GameTrailer) : GalleryItem
        data class Screenshot(val url: String) : GalleryItem
    }

    /**
     * Adapter cho rvScreenshots với 2 loại item: trailer hiện thumbnail + nút play ở giữa,
     * screenshot chỉ hiện ảnh. Trailers luôn đứng trước screenshots (xem bindGame).
     */
    private inner class GalleryAdapter(private val items: List<GalleryItem>) :
        androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {

        private val typeTrailer = 0
        private val typeScreenshot = 1

        inner class TrailerViewHolder(view: android.view.View) :
            androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
            val ivThumb: android.widget.ImageView = view.findViewById(network.ermis.genstreamui.R.id.ivTrailerThumb)
        }

        inner class ScreenshotViewHolder(view: android.view.View) :
            androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
            val ivScreenshot: android.widget.ImageView = view.findViewById(network.ermis.genstreamui.R.id.ivScreenshot)
        }

        override fun getItemViewType(position: Int) =
            if (items[position] is GalleryItem.Trailer) typeTrailer else typeScreenshot

        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int):
            androidx.recyclerview.widget.RecyclerView.ViewHolder =
            if (viewType == typeTrailer) {
                TrailerViewHolder(
                    layoutInflater.inflate(network.ermis.genstreamui.R.layout.item_trailer, parent, false)
                )
            } else {
                ScreenshotViewHolder(
                    layoutInflater.inflate(network.ermis.genstreamui.R.layout.item_screenshot, parent, false)
                )
            }

        override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
            when (val item = items[position]) {
                is GalleryItem.Trailer ->
                    (holder as TrailerViewHolder).ivThumb.loadCover(item.trailer.thumbnail)
                is GalleryItem.Screenshot ->
                    (holder as ScreenshotViewHolder).ivScreenshot.loadCover(item.url)
            }
            holder.itemView.setOnClickListener { openPreview(holder.bindingAdapterPosition) }
        }

        override fun getItemCount() = items.size

        /** Mở PreviewMediaActivity với toàn bộ gallery, nhảy tới [position] vừa tap. */
        private fun openPreview(position: Int) {
            if (position == androidx.recyclerview.widget.RecyclerView.NO_POSITION) return
            val urls = ArrayList<String>(items.size)
            val thumbs = ArrayList<String>(items.size)
            val isVideo = BooleanArray(items.size)
            items.forEachIndexed { i, item ->
                when (item) {
                    is GalleryItem.Trailer -> {
                        urls.add(item.trailer.video)
                        thumbs.add(item.trailer.thumbnail)
                        isVideo[i] = true
                    }
                    is GalleryItem.Screenshot -> {
                        urls.add(item.url)
                        thumbs.add("")
                        isVideo[i] = false
                    }
                }
            }
            PreviewMediaActivity.start(this@PlayGameActivity, urls, thumbs, isVideo, position)
        }
    }

    /** Mở WindowsConnectActivity ở chế độ Play Now (stream thẳng vào game, không qua lưới AppView). */
    private fun openWindowsConnectForPlay() {
        val steamId = currentGame?.steamAppid ?: 0
        val intent = Intent(this, WindowsConnectActivity::class.java).apply {
            putExtra(WindowsConnectActivity.EXTRA_OPEN_APP_LIST, false)
            // steam_appid → platform là "steam"; agent /launch tự mở đúng game (Stage 3a) khi appId > 0.
            putExtra(
                WindowsConnectActivity.EXTRA_PLATFORM,
                if (steamId > 0) "steam" else currentGame?.platforms?.firstOrNull().orEmpty()
            )
            putExtra(WindowsConnectActivity.EXTRA_APP_ID, steamId)
        }
        startActivity(intent)
    }

    /**
     * Hiện/ẩn từng icon trong llPlatforms theo [platforms] của game — giống logic ở GameAdapter.
     * Windows luôn hiện (app stream PC); các icon khác chỉ hiện nếu [platforms] chứa alias tương ứng.
     */
    private fun bindPlatforms(platforms: List<String>) {
        val keys = platforms.map { it.trim().lowercase() }.toHashSet()

        fun toggle(view: android.view.View, vararg aliases: String) {
            view.visibility =
                if (aliases.any { it in keys }) android.view.View.VISIBLE else android.view.View.GONE
        }

        binding.ivWindows.visibility = android.view.View.VISIBLE
        toggle(binding.ivSteam, "steam")
        toggle(binding.ivXbox, "xbox", "microsoft")
        toggle(binding.ivGog, "gog")
        toggle(binding.ivBattle, "battle-net", "battlenet", "battle")
        toggle(binding.ivEa, "ea", "origin")
        toggle(binding.ivEpic, "epic", "epicgames", "epic-games")
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

        /** Bán kính blur tối đa (px) cho artwork nền khi cuộn quá header. */
        private const val MAX_BLUR_RADIUS = 28f

        /** Quãng cuộn (px) tính từ lúc header trôi hết để blur đạt tối đa. */
        private const val BLUR_FULL_DISTANCE_PX = 400f
    }
}
