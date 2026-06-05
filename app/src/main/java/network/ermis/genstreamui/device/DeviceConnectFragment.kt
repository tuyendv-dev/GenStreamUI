package network.ermis.genstreamui.device

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import network.ermis.genstreamui.addScaleClickEffect
import network.ermis.genstreamui.databinding.FragmentDeviceConnectBinding

class DeviceConnectFragment : Fragment() {

    private var _binding: FragmentDeviceConnectBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeviceConnectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val device = arguments?.getSerializable("device") as? DeviceItem
        device?.let {
            if (it.iconResId != null) {
                binding.ivBigDevice.setImageResource(it.iconResId)
            }
        }

        binding.btnConnect.addScaleClickEffect()
        binding.btnConnect.setOnClickListener {
            val dialog = DeviceConnectPermissionDialog()
            dialog.show(childFragmentManager, "DeviceConnectPermissionDialog")
        }

        binding.btnReconnect.addScaleClickEffect()
        binding.btnViewTutorial.addScaleClickEffect()

        // Đưa focus về nút Connect để điều hướng bằng tay cầm/D-pad ngay khi vào màn.
        binding.btnConnect.post { binding.btnConnect.requestFocus() }
    }

    companion object {
        fun newInstance(device: DeviceItem): DeviceConnectFragment {
            val fragment = DeviceConnectFragment()
            val args = Bundle()
            args.putSerializable("device", device)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
