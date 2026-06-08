package network.ermis.genstreamui.common.base.ext

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * Collect một [Flow] gắn với vòng đời, tự động dừng khi rời STARTED và resume lại khi quay lại.
 * Tránh phải lặp lại boilerplate repeatOnLifecycle ở mỗi màn.
 */
fun <T> LifecycleOwner.collectWhenStarted(
    flow: Flow<T>,
    onEach: (T) -> Unit
) {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.collect { onEach(it) }
        }
    }
}
