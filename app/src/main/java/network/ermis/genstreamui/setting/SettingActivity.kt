package network.ermis.genstreamui.setting

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.activity.enableEdgeToEdge
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import network.ermis.genstreamui.R
import network.ermis.genstreamui.databinding.ActivitySettingBinding
import android.view.KeyEvent

class SettingActivity : AppCompatActivity() {

    private lateinit var categories: List<SettingCategory>
    private lateinit var binding: ActivitySettingBinding
    private lateinit var itemAdapter: SettingItemAdapter
    private var initialFocusDone = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        categories = SettingManager.loadSettings(this)

        binding.btnBack.setOnClickListener {
            finish()
        }

        setupRecyclerViews()
    }

    private fun setupRecyclerViews() {
        // Setup Right Items list
        binding.rvItems.layoutManager = LinearLayoutManager(this)
        itemAdapter = SettingItemAdapter(emptyList()) { item, isEnabled ->
            // Save when toggled
            SettingManager.saveSettings(this, categories)
        }
        binding.rvItems.adapter = itemAdapter

        // Setup Left Category list
        binding.rvCategories.layoutManager = LinearLayoutManager(this)
        // Tắt change-animation: nếu không, notifyItemChanged khi đổi selection sẽ
        // detach view đang focus và làm mất focus, khiến phải nhấn D-pad nhiều lần
        // mới di chuyển được giữa các item.
        (binding.rvCategories.itemAnimator as? androidx.recyclerview.widget.SimpleItemAnimator)
            ?.supportsChangeAnimations = false
        val categoryAdapter = SettingCategoryAdapter(categories) { selectedCategory ->
            binding.rvItems.alpha = 0f
            itemAdapter.updateItems(selectedCategory.items)
            binding.rvItems.scrollToPosition(0)
            binding.rvItems.animate().alpha(1f).setDuration(300).start()
        }
        binding.rvCategories.adapter = categoryAdapter

        // Select the first category by default.
        // Focus ban đầu được SettingCategoryAdapter tự request lên item đầu tiên
        // ngay khi nó được bind (đáng tin cậy hơn getChildAt khi RV chưa layout).
        if (categories.isNotEmpty()) {
            itemAdapter.updateItems(categories[0].items)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        // Đặt focus ban đầu lên item đầu tiên của danh sách trái ngay khi cửa sổ
        // thực sự nhận focus. Làm ở đây để không bị Android ghi đè focus mặc định
        // về btnBack (khiến lần nhấn D-pad đầu tiên bị "trễ"/không có hiệu lực).
        if (hasFocus && !initialFocusDone && categories.isNotEmpty()) {
            initialFocusDone = true
            focusFirstCategory()
        }
    }

    private fun focusFirstCategory(attempt: Int = 0) {
        val rv = binding.rvCategories
        val firstItem = rv.findViewHolderForAdapterPosition(0)?.itemView ?: rv.getChildAt(0)
        if (firstItem != null) {
            // Item đã sẵn sàng -> ép focus về đây. requestFocus chỉ đảm bảo khi
            // item đã được layout, nếu chưa thì thử lại ở frame sau.
            firstItem.requestFocus()
            if (!firstItem.isFocused && attempt < 5) {
                rv.postDelayed({ focusFirstCategory(attempt + 1) }, 16L)
            }
        } else if (attempt < 10) {
            // rvCategories chưa layout xong (danh sách phải có thể đã sẵn sàng và
            // giành mất focus) -> chờ frame sau rồi thử lại.
            rv.postDelayed({ focusFirstCategory(attempt + 1) }, 16L)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BUTTON_B) {
            finish()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}
