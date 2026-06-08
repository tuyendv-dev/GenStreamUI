package network.ermis.genstreamui.common

/**
 * Trạng thái UI bất đồng bộ dùng chung cho mọi UseCase/ViewModel.
 * Port từ GenPlayAndroid (common/UiState.kt).
 */
sealed class UiState<out T> {
    data object Loading : UiState<Nothing>()
    data object Idle : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String, val code: String = "") : UiState<Nothing>()
}
