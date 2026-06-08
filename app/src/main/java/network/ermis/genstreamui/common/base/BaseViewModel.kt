package network.ermis.genstreamui.common.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Base ViewModel với helper [launchIO] chạy block trên IO dispatcher rồi quay về Main.
 * Dispatcher được inject để dễ test. Port từ GenPlayAndroid (common/base/BaseViewModel.kt).
 */
open class BaseViewModel(
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main
) : ViewModel() {

    protected fun launchIO(
        onError: (Throwable) -> Unit = {},
        onCompleted: (() -> Unit)? = null,
        block: suspend CoroutineScope.() -> Unit
    ) = viewModelScope.launch(mainDispatcher) {
        try {
            withContext(ioDispatcher) {
                block.invoke(this)
            }
        } catch (e: Exception) {
            onError.invoke(e)
        } finally {
            onCompleted?.invoke()
        }
    }
}
