package network.ermis.genstreamui.presentation.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Typeface
import android.os.BatteryManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.widget.NestedScrollView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import dagger.hilt.android.AndroidEntryPoint
import network.ermis.genstreamui.R
import network.ermis.genstreamui.common.base.ext.loadAvatar
import network.ermis.genstreamui.database.cache.SharedPrefCommon
import network.ermis.genstreamui.database.cache.cachedUser
import network.ermis.genstreamui.databinding.FragmentHomeBinding
import network.ermis.genstreamui.presentation.addScaleClickEffect
import network.ermis.genstreamui.presentation.device.DeviceActivity
import network.ermis.genstreamui.presentation.home.adapter.HomePagerAdapter
import network.ermis.genstreamui.presentation.profile.UserProfileActivity
import network.ermis.genstreamui.presentation.setting.SettingActivity
import network.ermis.genstreamui.presentation.subscription.SubscriptionActivity
import network.ermis.genstreamui.presentation.widget.setupStatusIcons
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
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

        setupStatusIcons(
            binding = binding.statusIcons,
            showSearch = true
        )

        setupViewPager()

        // Add scale click effects
        binding.sideMenu.layoutUserProfile.addScaleClickEffect()
        binding.btnMenu.addScaleClickEffect()
        binding.sideMenu.menuItemHome.addScaleClickEffect()
        binding.sideMenu.menuItemGame.addScaleClickEffect()
        binding.sideMenu.menuItemSteam.addScaleClickEffect()
        binding.sideMenu.menuItemSetting.addScaleClickEffect()
        binding.sideMenu.menuItemDevice.addScaleClickEffect()
        binding.sideMenu.menuItemUserCenter.addScaleClickEffect()

        binding.btnMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
            binding.sideMenu.menuItemHome.isFocusableInTouchMode = true
            binding.sideMenu.menuItemHome.requestFocus()
        }

        binding.sideMenu.menuItemHome.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                v.isFocusableInTouchMode = false
            }
        }

        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    isEnabled = false
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                    isEnabled = true
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressedCallback)

        binding.drawerLayout.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
            override fun onDrawerOpened(drawerView: View) {
                binding.sideMenu.menuItemHome.requestFocus()
            }

            override fun onDrawerClosed(drawerView: View) {
                binding.btnMenu.requestFocus()
            }
        })

        binding.sideMenu.layoutUserProfile.setOnClickListener {
            val intent = Intent(requireContext(), UserProfileActivity::class.java)
            startActivity(intent)
            binding.drawerLayout.closeDrawer(GravityCompat.START)
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

        binding.sideMenu.menuItemGame.setOnClickListener {
            val intent = Intent(requireContext(), SubscriptionActivity::class.java)
            startActivity(intent)
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    override fun onResume() {
        super.onResume()
        // Cập nhật lại theo cache (đề phòng user vừa thay đổi thông tin ở màn Profile)
        if (_binding != null) bindSidebarUser()
    }

    /** Đổ thông tin user đã cache vào side menu (avatar + tên hiển thị). */
    private fun bindSidebarUser() {
        val user = SharedPrefCommon.cachedUser()
        if (user.displayName.isNotEmpty()) {
            binding.sideMenu.tvSidebarUsername.text = user.displayName
        }
        binding.sideMenu.ivSidebarAvatar.loadAvatar(user.avatarUrl)
    }



    private fun setupViewPager() {
        val adapter = HomePagerAdapter(this)
        binding.viewPager.adapter = adapter

        // Disable swipe to change tabs
        binding.viewPager.isUserInputEnabled = false

        binding.tabMine.setOnClickListener { selectTab(0) }
        binding.tabDiscovery.setOnClickListener { selectTab(1) }
        binding.tabFindGame.setOnClickListener { selectTab(2) }

        // Select Discovery as default tab (position 1)
        selectTab(1)
    }

    private fun selectTab(position: Int) {
        if (binding.viewPager.currentItem == position) {
            scrollToTop(position)
            return
        }

        binding.viewPager.setCurrentItem(position, false)
        scrollToTop(position)

        val colorPrimary = ContextCompat.getColor(requireContext(), R.color.text_primary)
        val colorSecondary = ContextCompat.getColor(requireContext(), R.color.text_secondary)

        // Reset all tabs
        binding.tabMine.setTextColor(colorSecondary)
        binding.tabMine.setTypeface(null, Typeface.NORMAL)
        binding.tabDiscovery.setTextColor(colorSecondary)
        binding.tabDiscovery.setTypeface(null, Typeface.NORMAL)
        binding.tabFindGame.setTextColor(colorSecondary)
        binding.tabFindGame.setTypeface(null, Typeface.NORMAL)

        // Highlight selected tab
        when (position) {
            0 -> {
                binding.tabMine.setTextColor(colorPrimary)
                binding.tabMine.setTypeface(null, Typeface.BOLD)
            }
            1 -> {
                binding.tabDiscovery.setTextColor(colorPrimary)
                binding.tabDiscovery.setTypeface(null, Typeface.BOLD)
            }
            2 -> {
                binding.tabFindGame.setTextColor(colorPrimary)
                binding.tabFindGame.setTypeface(null, Typeface.BOLD)
            }
        }
    }

    private fun scrollToTop(position: Int) {
        val fragment = childFragmentManager.findFragmentByTag("f$position")
        fragment?.view?.findViewById<NestedScrollView>(R.id.scrollView)?.smoothScrollTo(0, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}