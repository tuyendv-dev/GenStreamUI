package network.ermis.genstreamui.presentation.windows

import android.os.Build
import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import network.ermis.genstreamui.common.UiState
import network.ermis.genstreamui.database.network.factory.ApiErrorCode
import network.ermis.genstreamui.domain.model.AgentToken
import network.ermis.genstreamui.domain.model.ConnectionToken
import network.ermis.genstreamui.domain.model.Session
import network.ermis.genstreamui.domain.model.SessionStatus
import network.ermis.genstreamui.domain.model.TokenAuthResult
import network.ermis.genstreamui.domain.usecase.session.CloseGameUseCase
import network.ermis.genstreamui.domain.usecase.session.ConnectionTokenResult
import network.ermis.genstreamui.domain.usecase.session.EndSessionUseCase
import network.ermis.genstreamui.domain.usecase.session.GetAgentTokenUseCase
import network.ermis.genstreamui.domain.usecase.session.GetConnectionTokenUseCase
import network.ermis.genstreamui.domain.usecase.session.HeartbeatUseCase
import network.ermis.genstreamui.domain.usecase.session.LaunchGameUseCase
import network.ermis.genstreamui.domain.usecase.session.StartSessionUseCase
import network.ermis.genstreamui.domain.usecase.session.TokenAuthUseCase
import javax.inject.Inject

/**
 * Orchestrator luồng kết nối tới máy tính (VM) cho [WindowsConnectActivity] — chạy đúng thứ tự
 * genstream-custom-auth.md §6: tạo phiên → poll connection-token (chờ VM) → token-auth → (đang/sau)
 * vào stream; song song giữ **heartbeat 30s** để VM không bị reap (§9).
 *
 * Toàn bộ logic kết nối nằm ở đây (không ở PlayGameActivity). Phần serverinfo/stream (Stage 3b) là
 * native engine sẽ nối ở [ConnectStage.Connected].
 */
@HiltViewModel
class WindowsConnectViewModel @Inject constructor(
    private val startSession: StartSessionUseCase,
    private val getConnectionToken: GetConnectionTokenUseCase,
    private val tokenAuth: TokenAuthUseCase,
    private val heartbeat: HeartbeatUseCase,
    private val endSession: EndSessionUseCase,
    private val getAgentToken: GetAgentTokenUseCase,
    private val launchGame: LaunchGameUseCase,
    private val closeGame: CloseGameUseCase
) : ViewModel() {

    private val _stage = MutableStateFlow<ConnectStage>(ConnectStage.Idle)
    val stage: StateFlow<ConnectStage> = _stage.asStateFlow()

    private val deviceName: String = Build.MODEL ?: "Android"
    private var sessionId: Int? = null
    private var connectJob: Job? = null
    private var heartbeatJob: Job? = null

    // Play-Now: nếu [appId] > 0 thì sau khi Authorized sẽ launch game qua agent.
    private var platform: String = ""
    private var appId: Int = 0
    private var agentToken: AgentToken? = null

    /**
     * Bắt đầu (hoặc thử lại) luồng kết nối cho [subscriptionId].
     * [platform]/[appId]: nếu [appId] > 0 → Play-Now (mở sẵn game qua agent); 0 → chỉ stream Desktop.
     */
    fun connect(subscriptionId: Int, platform: String = "", appId: Int = 0) {
        if (connectJob?.isActive == true) return
        this.platform = platform
        this.appId = appId
        connectJob = viewModelScope.launch {
            _stage.value = ConnectStage.CreatingSession

            val session = startSessionOrNull(subscriptionId) ?: return@launch
            sessionId = session.id

            val token = pollConnectionTokenOrNull(session.id) ?: return@launch

            _stage.value = ConnectStage.Authorizing
            when (val r = tokenAuth(token, deviceName)) {
                is TokenAuthResult.Authorized -> {
                    // Kết nối VM thành công → bắt đầu heartbeat 30s/lần TỪ ĐÂY (tránh beat lúc còn provisioning).
                    startHeartbeat(session.id)
                    _stage.value = ConnectStage.Connected(r.effectiveName, token.host, token.port)
                    if (appId > 0) ensureAgentTokenAndLaunch(session.id)
                }
                is TokenAuthResult.Rejected -> fail(r.message)
                is TokenAuthResult.Error -> fail(r.message)
            }
        }
    }

    /** Stage 3a — lấy agent token rồi launch game. Fire-and-forget: fail vẫn giữ stream Desktop. */
    private suspend fun ensureAgentTokenAndLaunch(sessionId: Int) {
        val tokenState = getAgentToken(sessionId)
        if (tokenState is UiState.Success) {
            agentToken = tokenState.data
            launchGame(tokenState.data, platform.ifEmpty { DEFAULT_PLATFORM }, appId)
        }
        // Lấy agent token / launch fail → bỏ qua, user tự mở game trong Desktop.
    }

    /** Stage 0. Trả [Session] nếu tạo phiên thành công, null (đã set Failed) nếu lỗi. */
    private suspend fun startSessionOrNull(subscriptionId: Int): Session? {
        var session: Session? = null
        startSession(subscriptionId).collect { state ->
            when (state) {
                is UiState.Success -> session = state.data
                is UiState.Error -> fail(state.message)
                else -> Unit
            }
        }
        return session
    }

    /** Stage 1. Poll tới khi VM sẵn sàng (Issued) hoặc hết ngân sách / lỗi (đã set Failed). */
    private suspend fun pollConnectionTokenOrNull(sessionId: Int): ConnectionToken? {
        val deadline = SystemClock.elapsedRealtime() + POLL_BUDGET_MS
        var attempt = 0
        while (currentCoroutineContext().isActive) {
            _stage.value = ConnectStage.WaitingForVm(++attempt)
            when (val r = getConnectionToken(sessionId, deviceName)) {
                is ConnectionTokenResult.Issued -> return r.token
                ConnectionTokenResult.VmNotReady -> {
                    if (SystemClock.elapsedRealtime() >= deadline) {
                        fail("Máy ảo khởi tạo quá lâu, vui lòng thử lại")
                        return null
                    }
                    delay(POLL_INTERVAL_MS)
                }
                is ConnectionTokenResult.Error -> {
                    if (r.errorCode == ApiErrorCode.RATE_LIMITED) {
                        delay(RATE_LIMIT_BACKOFF_MS)
                    } else {
                        fail(r.message)
                        return null
                    }
                }
            }
        }
        return null
    }

    /** Stage 1.5 — heartbeat 30s/lần giữ VM sống. Lỗi tạm thời bỏ qua; phiên `stopped` → fail. */
    private fun startHeartbeat(sessionId: Int) {
        heartbeatJob?.cancel()
        heartbeatJob = viewModelScope.launch {
            while (isActive) {
                delay(HEARTBEAT_INTERVAL_MS)
                val r = heartbeat(sessionId)
                if (r is UiState.Success && r.data.sessionStatus == SessionStatus.STOPPED) {
                    fail("Phiên đã dừng")
                    break
                }
            }
        }
    }

    private fun fail(message: String) {
        heartbeatJob?.cancel()
        _stage.value = ConnectStage.Failed(message)
    }

    /**
     * Hủy kết nối + best-effort kết thúc phiên để giải phóng VM (không thì backend reap sau 180s).
     * Chạy ngoài [viewModelScope] vì scope đã bị hủy khi rời màn.
     */
    fun cancelAndRelease() {
        connectJob?.cancel()
        heartbeatJob?.cancel()
        val id = sessionId ?: return
        sessionId = null
        val at = agentToken
        val p = platform.ifEmpty { DEFAULT_PLATFORM }
        val app = appId
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                // §9: phải /close TRƯỚC /end (sau /end agent verify token sẽ fail).
                if (at != null && app > 0) closeGame(at, p, app)
                endSession(id).collect { }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        cancelAndRelease()
    }

    private companion object {
        const val POLL_INTERVAL_MS = 7_000L
        const val RATE_LIMIT_BACKOFF_MS = 15_000L
        const val POLL_BUDGET_MS = 5 * 60 * 1000L
        const val HEARTBEAT_INTERVAL_MS = 30_000L
        const val DEFAULT_PLATFORM = "steam"
    }
}
