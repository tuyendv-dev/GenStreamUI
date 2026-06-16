package network.ermis.genstreamui.database.security

import android.content.Context
import com.limelight.binding.crypto.AndroidCryptoProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Danh tính client DÙNG CHUNG với engine Moonlight: trả về PEM của chính cert (client.crt) mà
 * [AndroidCryptoProvider] dùng cho mTLS khi Game/NvHTTP gọi serverinfo/launch.
 *
 * ⚠️ token-auth PHẢI gửi cert này (không phải cert riêng) thì cert host authorize mới trùng cert
 * mTLS của Game → host coi là đã paired (genstream-custom-auth.md §8). [AndroidCryptoProvider] tự
 * sinh client.crt/client.key lần đầu và lưu bền, tái dùng cho cả token-auth lẫn stream.
 */
@Singleton
class MoonlightClientIdentity @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val provider by lazy { AndroidCryptoProvider(context) }

    fun certificatePem(): String = String(provider.pemEncodedClientCertificate, Charsets.UTF_8)
}
