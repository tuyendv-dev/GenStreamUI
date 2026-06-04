package network.ermis.genstreamui.device

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import network.ermis.genstreamui.R
import network.ermis.genstreamui.databinding.FragmentDeviceListBinding

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
