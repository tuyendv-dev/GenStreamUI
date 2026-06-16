package network.ermis.genstreamui.presentation.windows

import android.app.Activity
import android.content.Intent
import com.limelight.Game
import com.limelight.computers.IdentityManager
import network.ermis.genstreamui.database.security.PinnedServerCertStore
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cầu nối từ luồng connect (Kotlin) sang engine Moonlight: dựng Intent mở [Game] với đủ thông tin
 * để stream + điều khiển VM (genstream-custom-auth.md §6 Stage 3b).
 *
 * Điểm mấu chốt đã xử lý:
 * - **serverPortBase = base** → moonlight-common-c suy RTSP/video/control/audio qua relay (patch native).
 * - **EXTRA_SERVER_CERT** = server cert đã pin (TOFU ở token-auth) → NvHTTP tin host.
 * - Client cert mTLS do [com.limelight.binding.crypto.AndroidCryptoProvider] cung cấp — chính là cert
 *   đã token-auth ([network.ermis.genstreamui.database.security.MoonlightClientIdentity]) → host coi đã paired.
 * - **appName** ("Desktop") không kèm appId → NvConnection tự `getAppByName` resolve trên host.
 */
@Singleton
class MoonlightLauncher @Inject constructor(
    private val pinnedStore: PinnedServerCertStore
) {
    /**
     * Mở [Game] để stream màn hình VM tại [host]:[basePort].
     * @return false nếu chưa có server cert pin cho host (chưa token-auth) — không mở.
     */
    fun launch(
        activity: Activity,
        host: String,
        basePort: Int,
        pcUuid: String,
        appName: String = DEFAULT_APP_NAME
    ): Boolean {
        val serverCert = pinnedStore.get(host) ?: return false

        val intent = Intent(activity, Game::class.java).apply {
            putExtra(Game.EXTRA_HOST, host)
            putExtra(Game.EXTRA_PORT, basePort)
            putExtra(Game.EXTRA_HTTPS_PORT, basePort - HTTPS_OFFSET)
            putExtra(Game.EXTRA_SERVER_PORT_BASE, basePort)
            // Không set EXTRA_APP_ID → INVALID → NvConnection resolve theo tên (vd "Desktop").
            putExtra(Game.EXTRA_APP_NAME, appName)
            putExtra(Game.EXTRA_APP_HDR, false)
            putExtra(Game.EXTRA_UNIQUEID, IdentityManager(activity).uniqueId)
            putExtra(Game.EXTRA_PC_UUID, pcUuid)
            putExtra(Game.EXTRA_PC_NAME, PC_NAME)
            runCatching { putExtra(Game.EXTRA_SERVER_CERT, serverCert.encoded) }
        }
        activity.startActivity(intent)
        return true
    }

    private companion object {
        // HTTPS = base − 5 (DEFAULT_HTTPS_PORT 47984 − DEFAULT_HTTP_PORT 47989).
        const val HTTPS_OFFSET = 5
        const val DEFAULT_APP_NAME = "Desktop"
        const val PC_NAME = "GenStream VM"
    }
}
