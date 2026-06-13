package network.ermis.genstreamui.presentation.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.view.View
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import network.ermis.genstreamui.R
import network.ermis.genstreamui.databinding.LayoutStatusIconsBinding
import network.ermis.genstreamui.presentation.home.NetworkSignalMonitor
import network.ermis.genstreamui.presentation.home.NetworkSignalState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StatusIconsManager(
    private val binding: LayoutStatusIconsBinding,
    private val showSearch: Boolean = true,
    private val showGamepad: Boolean = true,
    private val onSearchClick: (() -> Unit)? = null,
    private val onGamepadClick: (() -> Unit)? = null
) : DefaultLifecycleObserver {

    private lateinit var networkSignalMonitor: NetworkSignalMonitor
    private var networkCollectJob: Job? = null
    
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val timeRunnable = object : Runnable {
        override fun run() {
            binding.tvStatusTime.text = timeFormat.format(Date())
            binding.tvStatusTime.postDelayed(this, 60000)
        }
    }

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_BATTERY_CHANGED) {
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL

                if (level != -1 && scale != -1) {
                    val batteryPct = level * 100 / scale
                    updateBatteryIcon(batteryPct, isCharging)
                }
            }
        }
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        networkSignalMonitor = NetworkSignalMonitor(binding.root.context)

        // Remove tint so multi-opacity icons work properly
        binding.ivStatusBattery.imageTintList = null
        binding.ivStatusNetwork.imageTintList = null

        // Search icon visibility and click
        binding.ivStatusSearch.visibility = if (showSearch) View.VISIBLE else View.GONE
        onSearchClick?.let { clickListener ->
            binding.ivStatusSearch.setOnClickListener { clickListener() }
        }

        // Gamepad icon visibility and click
        binding.ivStatusGamepad.visibility = if (showGamepad) View.VISIBLE else View.GONE
        onGamepadClick?.let { clickListener ->
            binding.ivStatusGamepad.setOnClickListener { clickListener() }
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        
        // Register battery receiver
        binding.root.context.registerReceiver(
            batteryReceiver, 
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )

        // Start network signal monitor
        networkSignalMonitor.start()

        // Start network flow collection
        networkCollectJob = owner.lifecycleScope.launch {
            networkSignalMonitor.networkState.collect { state ->
                updateNetworkIcon(state)
            }
        }

        // Start time updating
        binding.tvStatusTime.removeCallbacks(timeRunnable)
        timeRunnable.run()
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)

        // Unregister battery receiver
        try {
            binding.root.context.unregisterReceiver(batteryReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Stop network signal monitor
        networkSignalMonitor.stop()
        networkCollectJob?.cancel()
        networkCollectJob = null

        // Stop time updating
        binding.tvStatusTime.removeCallbacks(timeRunnable)
    }

    private fun updateNetworkIcon(state: NetworkSignalState) {
        val iconRes = if (state.isWifi) {
            when (state.level) {
                0 -> R.drawable.ic_wifi_0
                1 -> R.drawable.ic_wifi_1
                2 -> R.drawable.ic_wifi_2
                3 -> R.drawable.ic_wifi_3
                else -> R.drawable.ic_wifi_4
            }
        } else {
            when (state.level) {
                0 -> R.drawable.ic_cellular_0
                1 -> R.drawable.ic_cellular_1
                2 -> R.drawable.ic_cellular_2
                3 -> R.drawable.ic_cellular_3
                else -> R.drawable.ic_cellular_4
            }
        }
        binding.ivStatusNetwork.setImageResource(iconRes)
    }

    private fun updateBatteryIcon(batteryPct: Int, isCharging: Boolean) {
        val level = when {
            batteryPct >= 90 -> 100
            batteryPct >= 70 -> 80
            batteryPct >= 50 -> 60
            batteryPct >= 30 -> 40
            batteryPct >= 10 -> 20
            else -> 0
        }

        val iconRes = if (isCharging) {
            when (level) {
                100 -> R.drawable.ic_battery_charging_100
                80 -> R.drawable.ic_battery_charging_80
                60 -> R.drawable.ic_battery_charging_60
                40 -> R.drawable.ic_battery_charging_40
                20 -> R.drawable.ic_battery_charging_20
                else -> R.drawable.ic_battery_charging_0
            }
        } else {
            when (level) {
                100 -> R.drawable.ic_battery_100
                80 -> R.drawable.ic_battery_80
                60 -> R.drawable.ic_battery_60
                40 -> R.drawable.ic_battery_40
                20 -> R.drawable.ic_battery_20
                else -> R.drawable.ic_battery_0
            }
        }
        binding.ivStatusBattery.setImageResource(iconRes)
    }
}

/**
 * Extension function to easily setup status icons logic for any LifecycleOwner (Activity or Fragment).
 */
fun LifecycleOwner.setupStatusIcons(
    binding: LayoutStatusIconsBinding,
    showSearch: Boolean = true,
    showGamepad: Boolean = true,
    onSearchClick: (() -> Unit)? = null,
    onGamepadClick: (() -> Unit)? = null
): StatusIconsManager {
    val manager = StatusIconsManager(
        binding = binding,
        showSearch = showSearch,
        showGamepad = showGamepad,
        onSearchClick = onSearchClick,
        onGamepadClick = onGamepadClick
    )
    this.lifecycle.addObserver(manager)
    return manager
}
