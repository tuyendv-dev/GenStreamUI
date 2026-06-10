package network.ermis.genstreamui.domain.usecase.auth

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import network.ermis.genstreamui.BuildConfig
import javax.inject.Inject

/**
 * Tạo GoogleSignInClient và cung cấp Intent đăng nhập Google.
 * Sau khi có kết quả (idToken + serverAuthCode), gọi [LoginWithGoogleUseCase] để đổi lấy phiên.
 * Port theo GenPlayAndroid (domain/usecase/auth/LoginByGoogleUseCase.kt).
 */
class LoginByGoogleUseCase @Inject constructor() {

    fun buildClient(context: Context): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(BuildConfig.serverClientId)
            .requestServerAuthCode(BuildConfig.serverClientId)
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context, gso)
    }

    fun signInIntent(context: Context): Intent = buildClient(context).signInIntent
}
