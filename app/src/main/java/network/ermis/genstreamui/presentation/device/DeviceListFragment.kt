package network.ermis.genstreamui.presentation.device

import dagger.hilt.android.AndroidEntryPoint

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import network.ermis.genstreamui.R
import network.ermis.genstreamui.databinding.FragmentDeviceListBinding

@AndroidEntryPoint
class DeviceListFragment : Fragment() {

    private var _binding: FragmentDeviceListBinding? = null
    private val binding get() = _binding!!
    private lateinit var gridAdapter: DeviceGridAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeviceListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerViews()
    }

    private fun setupRecyclerViews() {
        val categories = getMockData()

        // Setup Right Grid list
        binding.rvItems.layoutManager = GridLayoutManager(requireContext(), 4) // 4 columns
        gridAdapter = DeviceGridAdapter(emptyList()) { item ->
            if (!item.isAddButton) {
                (requireActivity() as? DeviceActivity)?.navigateToConnect(item)
            }
        }
        binding.rvItems.adapter = gridAdapter

        // Setup Left Category list
        binding.rvCategories.layoutManager = LinearLayoutManager(requireContext())
        // Tắt change-animation để notifyItemChanged khi đổi selection không detach
        // view đang focus (tránh phải nhấn D-pad nhiều lần mới di chuyển được).
        (binding.rvCategories.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
        val categoryAdapter = DeviceCategoryAdapter(categories) { selectedCategory ->
            binding.rvItems.alpha = 0f
            gridAdapter.updateItems(selectedCategory.items)
            binding.rvItems.animate().alpha(1f).setDuration(300).start()
        }
        binding.rvCategories.adapter = categoryAdapter

        if (categories.isNotEmpty()) {
            gridAdapter.updateItems(categories[0].items)
        }
    }

    private fun getMockData(): List<DeviceCategory> {
        return listOf(
            DeviceCategory(
                id = "my_device",
                title = "My device",
                items = listOf(
                    DeviceItem("x5s", "X5s", iconResId = R.drawable.img_device_x5s),
                    DeviceItem("add", "Add device", isAddButton = true)
                )
            ),
            DeviceCategory(
                id = "device_center",
                title = "Device center",
                items = listOf(
                    DeviceItem("x5s", "X5s", iconResId = R.drawable.img_device_x5s),
                    DeviceItem("x5lite", "X5Lite", iconResId = R.drawable.img_device_x5_lite),
                    DeviceItem("gamesir_x3", "GameSir-X3 Pro", iconResId = R.drawable.img_device_x3_pro),
                    DeviceItem("nova_2_lite", "Nova 2 Lite", iconResId = R.drawable.img_device_nova_2lite),
                    DeviceItem("g8", "G8", iconResId = R.drawable.img_device_g8),
                    DeviceItem("gamesir_g8_mfi", "GameSir G8 MFi", iconResId = R.drawable.img_device_g8_mfi),
                    DeviceItem("nova_pro", "Nova Pro", iconResId = R.drawable.img_device_nova_pro),
                    DeviceItem("nova_lite", "Nova Lite", iconResId = R.drawable.img_device_nova_lite)
                )
            )
        )
    }

    /**
     * Ép focus về category đầu tiên của danh sách trái. Có retry vì lúc cửa sổ
     * nhận focus RecyclerView có thể chưa layout xong (khi đó danh sách phải có
     * thể giành mất focus), khiến lần nhấn D-pad đầu tiên bị "trễ".
     */
    fun focusFirstCategory(attempt: Int = 0) {
        val rv = _binding?.rvCategories ?: return
        val firstItem = rv.findViewHolderForAdapterPosition(0)?.itemView ?: rv.getChildAt(0)
        if (firstItem != null) {
            firstItem.requestFocus()
            if (!firstItem.isFocused && attempt < 5) {
                rv.postDelayed({ focusFirstCategory(attempt + 1) }, 16L)
            }
        } else if (attempt < 10) {
            rv.postDelayed({ focusFirstCategory(attempt + 1) }, 16L)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
