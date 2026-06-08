package network.ermis.genstreamui.application

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import network.ermis.genstreamui.database.cache.SharedPrefCommon

/**
 * Application gốc — entry point của Hilt và nơi khởi tạo SharedPrefCommon.
 * Port từ GenPlayAndroid (application/GlobalApp.kt), bỏ Koin/Firebase do chưa cần.
 */
@HiltAndroidApp
class GlobalApp : Application() {

    override fun onCreate() {
        super.onCreate()
        SharedPrefCommon.init(this)
    }
}
