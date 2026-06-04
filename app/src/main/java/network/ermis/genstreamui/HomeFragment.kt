package network.ermis.genstreamui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import android.content.Intent
import androidx.core.view.GravityCompat
import network.ermis.genstreamui.databinding.FragmentHomeBinding
import network.ermis.genstreamui.device.DeviceActivity
import network.ermis.genstreamui.setting.SettingActivity

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViewPager()
        updateTime()

        // Add scale click effects
        binding.btnMenu.addScaleClickEffect()
        binding.sideMenu.menuItemHome.addScaleClickEffect()
        binding.sideMenu.menuItemGame.addScaleClickEffect()
        binding.sideMenu.menuItemSteam.addScaleClickEffect()
        binding.sideMenu.menuItemSetting.addScaleClickEffect()
        binding.sideMenu.menuItemDevice.addScaleClickEffect()
        binding.sideMenu.menuItemUserCenter.addScaleClickEffect()

        binding.btnMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        binding.sideMenu.menuItemSetting.setOnClickListener {
            val intent = Intent(requireContext(), SettingActivity::class.java)
            startActivity(intent)
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }
        
        binding.sideMenu.menuItemDevice.setOnClickListener {
            val intent = Intent(requireContext(), DeviceActivity::class.java)
            startActivity(intent)
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    private fun updateTime() {
        val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        binding.statusIcons.tvStatusTime.text = sdf.format(java.util.Date())
        
        binding.statusIcons.tvStatusTime.postDelayed(object : Runnable {
            override fun run() {
                _binding?.let {
                    it.statusIcons.tvStatusTime.text = sdf.format(java.util.Date())
                    it.statusIcons.tvStatusTime.postDelayed(this, 60000)
                }
            }
        }, 60000)
    }

    private fun setupViewPager() {
        val adapter = HomePagerAdapter(this)
        binding.viewPager.adapter = adapter
        
        // Disable swipe to change tabs
        binding.viewPager.isUserInputEnabled = false
        
        // Select Discovery as default tab (position 1)
        binding.viewPager.setCurrentItem(1, false)

        TabLayoutMediator(binding.tabLayout, binding.viewPager, true, false) { tab, position ->
            when (position) {
                0 -> tab.text = "Mine"
                1 -> tab.text = "Discovery"
                2 -> tab.text = "Find game"
            }
        }.attach()

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                scrollToTop(tab?.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {
                scrollToTop(tab?.position)
            }

            private fun scrollToTop(position: Int?) {
                position?.let {
                    val fragment = childFragmentManager.findFragmentByTag("f$it")
                    fragment?.view?.findViewById<androidx.core.widget.NestedScrollView>(R.id.scrollView)?.smoothScrollTo(0, 0)
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
