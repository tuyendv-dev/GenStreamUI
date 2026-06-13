package network.ermis.genstreamui.presentation.home

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.telephony.PhoneStateListener
import android.telephony.SignalStrength
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class NetworkSignalState(
    val isWifi: Boolean = true,
    val level: Int = 4 // 0 to 4
)

class NetworkSignalMonitor(context: Context) {
    private val appContext = context.applicationContext
    private val _networkState = MutableStateFlow(NetworkSignalState())
    val networkState: StateFlow<NetworkSignalState> = _networkState

    private val connectivityManager = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val telephonyManager = appContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    private val wifiManager = appContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    private var currentIsWifi = true
    private var cellularLevel = 4
    private var wifiLevel = 4

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            super.onCapabilitiesChanged(network, networkCapabilities)
            
            val isWifi = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
            val isCellular = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
            
            if (isWifi) {
                currentIsWifi = true
                val info = wifiManager.connectionInfo
                val rssi = info.rssi
                val level = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    wifiManager.calculateSignalLevel(rssi)
                } else {
                    WifiManager.calculateSignalLevel(rssi, 5)
                }
                wifiLevel = level.coerceIn(0, 4)
                // If permission is missing, rssi is -127 and level is 0. 
                // Let's assume 4 if it's exactly -127 to avoid showing 0 bars when connected.
                if (rssi <= -127) wifiLevel = 4 
            } else if (isCellular) {
                currentIsWifi = false
            }
            updateState()
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            // Just keep the last state or show disconnected
            updateState()
        }
    }

    private var phoneStateListener: PhoneStateListener? = null
    private var telephonyCallback: TelephonyCallback? = null

    fun start() {
        try {
            connectivityManager.registerDefaultNetworkCallback(networkCallback)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                telephonyCallback = object : TelephonyCallback(), TelephonyCallback.SignalStrengthsListener {
                    override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
                        cellularLevel = signalStrength.level.coerceIn(0, 4)
                        updateState()
                    }
                }
                telephonyManager.registerTelephonyCallback(appContext.mainExecutor, telephonyCallback!!)
            } else {
                phoneStateListener = object : PhoneStateListener() {
                    override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
                        super.onSignalStrengthsChanged(signalStrength)
                        cellularLevel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            signalStrength.level.coerceIn(0, 4)
                        } else {
                            // Rough estimation for old APIs
                            val gsm = signalStrength.gsmSignalStrength
                            when {
                                gsm <= 2 -> 0
                                gsm <= 8 -> 1
                                gsm <= 12 -> 2
                                gsm <= 14 -> 3
                                else -> 4
                            }
                        }
                        updateState()
                    }
                }
                telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stop() {
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                telephonyCallback?.let { telephonyManager.unregisterTelephonyCallback(it) }
            } else {
                phoneStateListener?.let { telephonyManager.listen(it, PhoneStateListener.LISTEN_NONE) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateState() {
        val activeNetwork = connectivityManager.activeNetwork
        val caps = connectivityManager.getNetworkCapabilities(activeNetwork)
        
        if (caps != null) {
            currentIsWifi = caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        }
        
        val level = if (currentIsWifi) wifiLevel else cellularLevel
        _networkState.value = NetworkSignalState(isWifi = currentIsWifi, level = level)
    }
}
