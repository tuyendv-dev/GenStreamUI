package network.ermis.genstreamui.application

import android.content.Context
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.load.engine.DiskCacheStrategy

/**
 * Cấu hình Glide cho toàn app.
 *
 * Disk cache đặt trong thư mục cache nội bộ (~100MB) nên ảnh đã tải tồn tại qua các lần kill app:
 * URL đã load 1 lần sẽ được đọc lại từ đĩa thay vì tải mạng. [DiskCacheStrategy.ALL] đặt mặc định
 * để chắc chắn cache cả ảnh gốc lẫn ảnh đã decode.
 */
@GlideModule
class GenStreamGlideModule : AppGlideModule() {

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        builder.setDiskCache(
            InternalCacheDiskCacheFactory(context, DISK_CACHE_SIZE_BYTES)
        )
        builder.setDefaultRequestOptions(
            RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
        )
    }

    // Đã dùng @GlideModule (annotation-based), không cần manifest parsing.
    override fun isManifestParsingEnabled(): Boolean = false

    private companion object {
        const val DISK_CACHE_SIZE_BYTES = 100L * 1024 * 1024 // 100MB
    }
}
