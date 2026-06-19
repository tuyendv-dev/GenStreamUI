package network.ermis.genstreamui.presentation.search

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import network.ermis.genstreamui.R
import network.ermis.genstreamui.common.UiState
import network.ermis.genstreamui.common.base.ext.collectWhenStarted
import network.ermis.genstreamui.databinding.ActivitySearchGameBinding
import network.ermis.genstreamui.domain.model.Game
import network.ermis.genstreamui.domain.model.GameSearchResult
import network.ermis.genstreamui.presentation.PlayGameActivity
import network.ermis.genstreamui.presentation.addScaleClickEffect
import network.ermis.genstreamui.presentation.home.adapter.GameAdapter

/**
 * Màn tìm game: thanh search (q) + icon filter (lọc theo category) -> GET /games.
 * Hiển thị số lượng kết quả và lưới game 4 cột (tái dùng [GameAdapter] + item_game).
 * Điều hướng tới đây từ icon Search ở HomeFragment.
 */
@AndroidEntryPoint
class SearchGameActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchGameBinding

    private val viewModel: SearchGameViewModel by viewModels()

    /** Danh mục tải từ API cho popup filter. */
    private var categories: List<String> = emptyList()

    /** Category đang lọc (null = tất cả). */
    private var selectedCategory: String? = null

    /** Adapter lưới game — giữ tham chiếu để append trang kế thay vì tạo lại. */
    private var gameAdapter: GameAdapter? = null

    /** Debounce gõ phím để không gọi API mỗi ký tự. */
    private val searchHandler = Handler(Looper.getMainLooper())
    private val searchRunnable = Runnable { triggerSearch() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySearchGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        binding.btnBack.setOnClickListener { finish() }

        binding.btnFilter.addScaleClickEffect()
        binding.btnFilter.setOnClickListener {
            hideKeyboard()
            showCategoryFilter()
        }

        setupSearchInput()
        setupLoadMore()
        observeSearch()
        observeCategories()

        // Hiển thị category đang chọn (mặc định "All").
        updateSelectedCategoryLabel()

        // Chỉ tải sẵn danh mục cho filter; KHÔNG tự search khi mở màn (thanh search rỗng).
        viewModel.loadCategories()

        // Vào màn -> focus con trỏ ở ô search và bật sẵn bàn phím ảo.
        showKeyboard()
    }

    /** Focus ô search và bật bàn phím ảo. */
    private fun showKeyboard() {
        binding.etSearch.requestFocus()
        binding.etSearch.post {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.etSearch, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun setupSearchInput() {
        // Gõ phím -> ẩn/hiện icon clear + tự search sau debounce (rỗng thì triggerSearch tự bỏ qua).
        binding.etSearch.doAfterTextChanged { text ->
            binding.btnClear.visibility = if (text.isNullOrEmpty()) View.GONE else View.VISIBLE
            searchHandler.removeCallbacks(searchRunnable)
            searchHandler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_MS)
        }
        // Icon clear -> xoá hết text.
        binding.btnClear.setOnClickListener { binding.etSearch.text?.clear() }
        // Bấm "Search" trên bàn phím ảo -> search ngay (huỷ debounce đang chờ).
        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchHandler.removeCallbacks(searchRunnable)
                triggerSearch()
                hideKeyboard()
                true
            } else {
                false
            }
        }
    }

    /** Chạm ra ngoài ô search -> bỏ focus và ẩn bàn phím ảo. */
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            val focused = currentFocus
            if (focused === binding.etSearch) {
                val rect = Rect()
                focused.getGlobalVisibleRect(rect)
                if (!rect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                    hideKeyboard()
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    /** Đóng bàn phím ảo và bỏ focus khỏi ô search. */
    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.etSearch.windowToken, 0)
        binding.etSearch.clearFocus()
    }

    /** Tự load trang kế khi cuộn gần cuối lưới. */
    private fun setupLoadMore() {
        binding.rvGames.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy <= 0) return
                val lm = recyclerView.layoutManager as? GridLayoutManager ?: return
                val lastVisible = lm.findLastVisibleItemPosition()
                if (lastVisible >= lm.itemCount - LOAD_MORE_THRESHOLD) {
                    viewModel.loadNextPage()
                }
            }
        })
    }

    private fun triggerSearch() {
        val query = binding.etSearch.text?.toString().orEmpty().trim()
        // Thanh search rỗng -> không gọi API.
        if (query.isEmpty()) return
        // Trang đầu: hiện progress + "Đang tìm kiếm cho từ khoá ..."; ẩn lại khi có kết quả.
        binding.pbSearching.visibility = View.VISIBLE
        binding.tvResultCount.text = getString(R.string.search_searching_query, query)
        viewModel.search(query = query, category = selectedCategory)
    }

    private fun observeSearch() {
        collectWhenStarted(viewModel.searchEvents) { ui ->
            when (ui) {
                UiState.Idle, UiState.Loading -> Unit
                is UiState.Success -> bindResult(ui.data)
                is UiState.Error -> {
                    binding.pbSearching.visibility = View.GONE
                    // Lỗi khi đang load thêm trang -> giữ kết quả cũ, chỉ báo nhẹ.
                    if ((gameAdapter?.itemCount ?: 0) > 0) {
                        Toast.makeText(this, ui.message, Toast.LENGTH_SHORT).show()
                    } else {
                        binding.tvResultCount.text = ui.message
                        gameAdapter = GameAdapter(emptyList()) {}
                        binding.rvGames.adapter = gameAdapter
                    }
                }
            }
        }
    }

    private fun observeCategories() {
        collectWhenStarted(viewModel.categoryEvents) { ui ->
            if (ui is UiState.Success) categories = ui.data
        }
    }

    private fun bindResult(result: GameSearchResult) {
        binding.pbSearching.visibility = View.GONE
        // Có từ khoá -> "N kết quả cho từ khoá X"; không có -> "N kết quả".
        val query = binding.etSearch.text?.toString().orEmpty().trim()
        binding.tvResultCount.text = if (query.isEmpty()) {
            getString(R.string.search_result_count, result.total)
        } else {
            getString(R.string.search_result_count_query, result.total, query)
        }
        if (result.page <= 1) {
            // Trang đầu (search/filter mới) -> thay trọn lưới, cuộn về đầu.
            gameAdapter = GameAdapter(result.games) { openPlayGame(it) }
            binding.rvGames.adapter = gameAdapter
        } else {
            // Trang kế -> nối tiếp, giữ vị trí cuộn.
            gameAdapter?.append(result.games)
        }
    }

    /**
     * Popup chọn category dạng single-choice (tích radio): "All" + danh mục từ API.
     * Mặc định tích theo [selectedCategory] hiện tại (null = "All"). Chọn xong lọc lại theo category đó.
     */
    private fun showCategoryFilter() {
        val items = mutableListOf(getString(R.string.search_filter_all))
        items.addAll(categories)

        val checkedIndex = selectedCategory?.let {
            val idx = categories.indexOf(it)
            if (idx >= 0) idx + 1 else 0
        } ?: 0

        val popupBinding = network.ermis.genstreamui.databinding.LayoutPopupFilterBinding.inflate(layoutInflater)
        val popupWindow = android.widget.PopupWindow(
            popupBinding.root,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        popupWindow.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
        popupWindow.elevation = 8f

        popupBinding.rvFilterOptions.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        popupBinding.rvFilterOptions.adapter = FilterCategoryAdapter(items, checkedIndex) { position ->
            selectedCategory = if (position == 0) null else categories[position - 1]
            updateSelectedCategoryLabel()
            triggerSearch()
            popupWindow.dismiss()
        }

        // Show popup under btnFilter with slight vertical offset
        popupWindow.showAsDropDown(binding.btnFilter, 0, 8)
    }

    /** Cập nhật nhãn category đang chọn ở góc phải resultBar (null = "All"). */
    private fun updateSelectedCategoryLabel() {
        binding.tvSelectedCategory.text = selectedCategory ?: getString(R.string.search_filter_all)
    }

    private fun openPlayGame(game: Game) {
        hideKeyboard()
        val intent = Intent(this, PlayGameActivity::class.java).apply {
            putExtra(PlayGameActivity.EXTRA_GAME_ID, game.id)
            putExtra(PlayGameActivity.EXTRA_GAME_SLUG, game.slug)
        }
        startActivity(intent)
    }

    override fun onDestroy() {
        searchHandler.removeCallbacks(searchRunnable)
        super.onDestroy()
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MS = 350L
        const val FILTER_GROUP_ID = 1
        const val ALL_CATEGORY_ID = 0

        /** Số item trước cuối lưới thì bắt đầu prefetch trang kế. */
        const val LOAD_MORE_THRESHOLD = 8
    }
}
