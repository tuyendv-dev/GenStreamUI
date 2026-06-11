package network.ermis.genstreamui.common.base.ext

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import network.ermis.genstreamui.R

/**
 * Load avatar từ [url] qua Glide, hình tròn + ảnh default khi rỗng/lỗi.
 *
 * [DiskCacheStrategy.ALL] ép Glide cache cả ảnh gốc lẫn ảnh đã decode xuống đĩa, nên URL
 * đã load 1 lần sẽ hiển thị lại ngay ở lần sau — kể cả sau khi kill app (disk cache bền vững).
 */
fun ImageView.loadAvatar(url: String?) {
    if (url.isNullOrEmpty()) {
        setImageResource(R.drawable.ic_avatar_default)
        return
    }
    Glide.with(this)
        .load(url)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .placeholder(R.drawable.ic_avatar_default)
        .error(R.drawable.ic_avatar_default)
        .circleCrop()
        .into(this)
}
