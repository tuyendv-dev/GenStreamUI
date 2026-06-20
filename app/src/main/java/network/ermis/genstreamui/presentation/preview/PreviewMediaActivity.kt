package network.ermis.genstreamui.presentation.preview

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import network.ermis.genstreamui.R
import network.ermis.genstreamui.databinding.ActivityPreviewMediaBinding

/**
 * Xem ảnh/video toàn màn hình dạng vuốt qua lại (ViewPager2). Mở từ GalleryAdapter ở
 * [network.ermis.genstreamui.presentation.PlayGameActivity], nhảy thẳng tới [EXTRA_START_POSITION].
 *
 * Dữ liệu truyền dạng 3 mảng song song (cùng index): [EXTRA_URLS] (video url / image url),
 * [EXTRA_THUMBS] (poster cho video, rỗng nếu ảnh) và [EXTRA_IS_VIDEO]. Dùng 1 ExoPlayer chia sẻ,
 * chỉ gắn vào trang video đang hiển thị; chuyển trang -> nhả player khỏi trang cũ.
 */
class PreviewMediaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPreviewMediaBinding

    private var urls: List<String> = emptyList()
    private var thumbs: List<String> = emptyList()
    private var isVideo: BooleanArray = BooleanArray(0)

    // 1 ExoPlayer cho mỗi trang video (key = vị trí) — giữ nguyên trạng thái phát khi vuốt qua lại,
    // không load lại từ đầu. Tạo lazy, giải phóng tất cả ở onDestroy.
    private val players = HashMap<Int, ExoPlayer>()

    // Player của trang video đang hiển thị.
    private var activePlayer: ExoPlayer? = null

    // Trang video đang gắn player; nhả ra khi rời trang để tránh 2 PlayerView giữ chung 1 player.
    private var attachedPlayerView: PlayerView? = null

    // Holder video đang hiển thị, để ẩn overlay thumbnail/loading khi player READY.
    private var attachedHolder: MediaAdapter.VideoViewHolder? = null

    // Mặc định bật tiếng; người dùng tắt/bật lại bằng nút mute.
    private var muted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPreviewMediaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Immersive: ẩn system bars cho trải nghiệm xem media toàn màn hình.
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.systemBars())

        urls = intent.getStringArrayListExtra(EXTRA_URLS) ?: emptyList()
        thumbs = intent.getStringArrayListExtra(EXTRA_THUMBS) ?: emptyList()
        isVideo = intent.getBooleanArrayExtra(EXTRA_IS_VIDEO) ?: BooleanArray(urls.size)
        val startPosition = intent.getIntExtra(EXTRA_START_POSITION, 0).coerceIn(0, (urls.size - 1).coerceAtLeast(0))

        if (urls.isEmpty()) {
            finish()
            return
        }

        binding.btnBack.setOnClickListener { finish() }

        binding.viewPager.adapter = MediaAdapter()
        binding.viewPager.setCurrentItem(startPosition, false)
        updateCounter(startPosition)

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateCounter(position)
                bindPlayerToPage(position)
            }
        })

        // Trang đầu chưa kịp layout khi setCurrentItem -> gắn player sau khi pager dựng xong.
        binding.viewPager.post { bindPlayerToPage(binding.viewPager.currentItem) }
    }

    private fun updateCounter(position: Int) {
        binding.tvCounter.text = "${position + 1}/${urls.size}"
    }

    /** LB (bumper trái) lùi trang, RB (bumper phải) tiến trang; clamp trong [0, size-1]. */
    override fun dispatchKeyEvent(event: android.view.KeyEvent): Boolean {
        if (event.action == android.view.KeyEvent.ACTION_DOWN) {
            when (event.keyCode) {
                android.view.KeyEvent.KEYCODE_BUTTON_L1 -> {
                    goToPage(binding.viewPager.currentItem - 1)
                    return true
                }
                android.view.KeyEvent.KEYCODE_BUTTON_R1 -> {
                    goToPage(binding.viewPager.currentItem + 1)
                    return true
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }

    private fun goToPage(target: Int) {
        val clamped = target.coerceIn(0, (urls.size - 1).coerceAtLeast(0))
        if (clamped != binding.viewPager.currentItem) {
            binding.viewPager.setCurrentItem(clamped, true)
        }
    }

    /**
     * Lấy ExoPlayer cho trang video [position] — tạo + prepare lần đầu rồi cache lại, các lần sau
     * trả về player cũ (giữ nguyên vị trí phát/buffer). Listener ẩn overlay khi player này READY.
     */
    private fun obtainPlayerFor(position: Int): ExoPlayer {
        return players[position] ?: ExoPlayer.Builder(this).build().also { exo ->
            exo.repeatMode = Player.REPEAT_MODE_ONE
            exo.volume = if (muted) 0f else 1f
            exo.setMediaItem(MediaItem.fromUri(urls[position]))
            exo.prepare()
            // Video sẵn sàng -> ẩn overlay (chỉ khi đây đúng là trang đang hiển thị).
            exo.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_READY && activePlayer === exo) {
                        attachedHolder?.loadingOverlay?.visibility = android.view.View.GONE
                    }
                }
            })
            players[position] = exo
        }
    }

    /**
     * Gắn player của trang [position] vào PlayerView của trang đó (resume từ trạng thái đã lưu),
     * tạm dừng player trang trước. Holder có thể chưa sẵn -> post lại 1 lần.
     */
    private fun bindPlayerToPage(position: Int) {
        val rv = binding.viewPager.getChildAt(0) as? RecyclerView ?: return
        val holder = rv.findViewHolderForAdapterPosition(position)
        if (holder == null) {
            binding.viewPager.post { bindPlayerToPage(position) }
            return
        }

        // Tạm dừng + nhả player khỏi trang trước đó (vẫn giữ player trong cache để resume sau).
        activePlayer?.playWhenReady = false
        attachedPlayerView?.player = null
        attachedPlayerView = null
        attachedHolder = null

        if (holder is MediaAdapter.VideoViewHolder) {
            val exo = obtainPlayerFor(position)
            activePlayer = exo
            attachedHolder = holder
            holder.playerView.player = exo
            attachedPlayerView = holder.playerView

            // Đã READY (quay lại trang) -> ẩn overlay ngay; chưa thì hiện thumbnail + loading.
            holder.loadingOverlay.visibility =
                if (exo.playbackState == Player.STATE_READY) android.view.View.GONE
                else android.view.View.VISIBLE

            exo.volume = if (muted) 0f else 1f
            // Không tự phát: người dùng phải bấm play. Quay lại trang giữ nguyên trạng thái pause.
            wireMuteButton(holder.playerView, exo)
        } else {
            activePlayer = null
        }
    }

    /** Wire nút mute trong controller tuỳ biến của PlayerView hiện tại. */
    private fun wireMuteButton(playerView: PlayerView, exo: ExoPlayer) {
        val btnMute = playerView.findViewById<android.widget.ImageButton>(R.id.btnMute) ?: return
        applyMuteIcon(btnMute)
        btnMute.setOnClickListener {
            muted = !muted
            exo.volume = if (muted) 0f else 1f
            applyMuteIcon(btnMute)
        }
    }

    private fun applyMuteIcon(btn: android.widget.ImageButton) {
        btn.setImageResource(if (muted) R.drawable.ic_volume_off else R.drawable.ic_volume_up)
    }

    override fun onPause() {
        super.onPause()
        activePlayer?.playWhenReady = false
    }

    // Không tự phát lại khi quay lại app — người dùng tự bấm play (giữ nguyên trạng thái pause).

    override fun onDestroy() {
        super.onDestroy()
        attachedPlayerView?.player = null
        players.values.forEach { it.release() }
        players.clear()
        activePlayer = null
    }

    /** Adapter ViewPager2: video dùng PlayerView (item_preview_video), ảnh dùng ImageView. */
    private inner class MediaAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private val typeVideo = 0
        private val typeImage = 1

        inner class VideoViewHolder(view: android.view.View) : RecyclerView.ViewHolder(view) {
            val playerView: PlayerView = view.findViewById(R.id.playerView)
            val loadingOverlay: android.view.View = view.findViewById(R.id.loadingOverlay)
        }

        inner class ImageViewHolder(view: android.view.View) : RecyclerView.ViewHolder(view) {
            val image: android.widget.ImageView = view.findViewById(R.id.ivPreviewImage)
        }

        override fun getItemViewType(position: Int) =
            if (isVideo.getOrElse(position) { false }) typeVideo else typeImage

        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            if (viewType == typeVideo) {
                VideoViewHolder(layoutInflater.inflate(R.layout.item_preview_video, parent, false))
            } else {
                ImageViewHolder(layoutInflater.inflate(R.layout.item_preview_image, parent, false))
            }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (holder) {
                is VideoViewHolder -> {
                    holder.playerView.useController = true
                    // Loading hiển thị tới khi player READY (xem obtainPlayerFor listener).
                    holder.loadingOverlay.visibility = android.view.View.VISIBLE
                }
                is ImageViewHolder ->
                    Glide.with(holder.itemView)
                        .load(urls[position])
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.bg_image_placeholder)
                        .into(holder.image)
            }
        }

        override fun getItemCount() = urls.size
    }

    companion object {
        private const val EXTRA_URLS = "extra_urls"
        private const val EXTRA_THUMBS = "extra_thumbs"
        private const val EXTRA_IS_VIDEO = "extra_is_video"
        private const val EXTRA_START_POSITION = "extra_start_position"

        /**
         * Mở màn preview. [urls]/[thumbs]/[isVideo] là 3 mảng song song cùng index;
         * [startPosition] là vị trí item được tap trong gallery.
         */
        fun start(
            context: Context,
            urls: ArrayList<String>,
            thumbs: ArrayList<String>,
            isVideo: BooleanArray,
            startPosition: Int
        ) {
            val intent = Intent(context, PreviewMediaActivity::class.java).apply {
                putStringArrayListExtra(EXTRA_URLS, urls)
                putStringArrayListExtra(EXTRA_THUMBS, thumbs)
                putExtra(EXTRA_IS_VIDEO, isVideo)
                putExtra(EXTRA_START_POSITION, startPosition)
            }
            context.startActivity(intent)
        }
    }
}
