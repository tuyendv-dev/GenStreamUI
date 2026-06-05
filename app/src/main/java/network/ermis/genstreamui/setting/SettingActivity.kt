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

class SettingActivity : AppCompatActivity() {

    private lateinit var categories: List<SettingCategory>
    private lateinit var binding: ActivitySettingBinding
    private lateinit var itemAdapter: SettingItemAdapter

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
        val categoryAdapter = SettingCategoryAdapter(categories) { selectedCategory ->
            binding.rvItems.alpha = 0f
            itemAdapter.updateItems(selectedCategory.items)
            binding.rvItems.scrollToPosition(0)
            binding.rvItems.animate().alpha(1f).setDuration(300).start()
        }
        binding.rvCategories.adapter = categoryAdapter

        // Select the first category by default
        if (categories.isNotEmpty()) {
            itemAdapter.updateItems(categories[0].items)
        }
    }
}
