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

    private var player: ExoPlayer? = null

    // Trang video đang gắn player; nhả ra khi rời trang để tránh 2 PlayerView giữ chung 1 player.
    private var attachedPlayerView: PlayerView? = null

    // Tắt tiếng mặc định (giống preview gallery thường thấy); người dùng bật lại bằng nút mute.
    private var muted = true

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

    /** Lấy ExoPlayer dùng chung (tạo lazy lần đầu chạm tới video). */
    private fun obtainPlayer(): ExoPlayer {
        return player ?: ExoPlayer.Builder(this).build().also {
            it.repeatMode = Player.REPEAT_MODE_ONE
            it.volume = if (muted) 0f else 1f
            player = it
        }
    }

    /**
     * Gắn player vào trang [position] nếu là video (chuẩn bị + phát), ngược lại nhả player khỏi
     * PlayerView trang cũ và tạm dừng. Holder có thể chưa sẵn -> post lại 1 lần.
     */
    private fun bindPlayerToPage(position: Int) {
        val rv = binding.viewPager.getChildAt(0) as? RecyclerView ?: return
        val holder = rv.findViewHolderForAdapterPosition(position)
        if (holder == null) {
            binding.viewPager.post { bindPlayerToPage(position) }
            return
        }

        // Nhả player khỏi trang trước đó.
        attachedPlayerView?.player = null
        attachedPlayerView = null

        if (holder is MediaAdapter.VideoViewHolder) {
            val exo = obtainPlayer()
            exo.setMediaItem(MediaItem.fromUri(urls[position]))
            exo.prepare()
            exo.playWhenReady = true
            holder.playerView.player = exo
            attachedPlayerView = holder.playerView
            wireMuteButton(holder.playerView, exo)
        } else {
            player?.playWhenReady = false
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
        player?.playWhenReady = false
    }

    override fun onResume() {
        super.onResume()
        // Tiếp tục phát nếu trang hiện tại là video.
        if (urls.isNotEmpty() && isVideo.getOrElse(binding.viewPager.currentItem) { false }) {
            player?.playWhenReady = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        attachedPlayerView?.player = null
        player?.release()
        player = null
    }

    /** Adapter ViewPager2: video dùng PlayerView (item_preview_video), ảnh dùng ImageView. */
    private inner class MediaAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private val typeVideo = 0
        private val typeImage = 1

        inner class VideoViewHolder(view: android.view.View) : RecyclerView.ViewHolder(view) {
            val playerView: PlayerView = view.findViewById(R.id.playerView)
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
                is VideoViewHolder -> holder.playerView.useController = true
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
