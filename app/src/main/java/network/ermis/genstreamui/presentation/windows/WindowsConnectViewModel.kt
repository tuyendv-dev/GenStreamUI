package network.ermis.genstreamui.presentation.windows

import android.os.Build
import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CompletableDeferred
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
import network.ermis.genstreamui.domain.model.ConnectionToken
import network.ermis.genstreamui.domain.model.Session
import network.ermis.genstreamui.domain.model.SessionStatus
import network.ermis.genstreamui.domain.model.TokenAuthResult
import network.ermis.genstreamui.domain.usecase.session.ConnectionTokenResult
import network.ermis.genstreamui.domain.usecase.session.EndSessionUseCase
import network.ermis.genstreamui.domain.usecase.session.GetAgentTokenUseCase
import network.ermis.genstreamui.domain.usecase.session.GetConnectionTokenUseCase
import network.ermis.genstreamui.domain.usecase.session.HeartbeatUseCase
import network.ermis.genstreamui.domain.usecase.session.LaunchGameUseCase
import network.ermis.genstreamui.domain.usecase.session.StartSessionUseCase
import network.ermis.genstreamui.domain.usecase.session.TokenAuthUseCase
import network.ermis.genstreamui.domain.usecase.subscription.GetActiveSessionBySubscriptionUseCase
import network.ermis.genstreamui.domain.usecase.subscription.GetUserSubscriptionsUseCase
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
    private val getUserSubscriptions: GetUserSubscriptionsUseCase,
    private val getActiveSessionBySubscription: GetActiveSessionBySubscriptionUseCase
) : ViewModel() {

    private val _stage = MutableStateFlow<ConnectStage>(ConnectStage.Idle)
    val stage: StateFlow<ConnectStage> = _stage.asStateFlow()

    private val deviceName: String = Build.MODEL ?: "Android"
    private var sessionId: Int? = null
    private var connectJob: Job? = null
    private var resolveJob: Job? = null
    private var heartbeatJob: Job? = null

    // Subscription đã chốt để mở phiên (auto khi có 1 gói, hoặc do user chọn khi có nhiều gói).
    private var resolvedSubscriptionId: Int? = null

    // Play-Now: nếu [appId] > 0 thì sau khi Authorized sẽ launch game qua agent.
    private var platform: String = ""
    private var appId: Int = 0

    // Game id (GenStream) gắn với phiên, gửi lên khi tạo session. null khi vào không gắn game (MineFragment).
    private var gameId: Int? = null
    // Tên game muốn chơi — chỉ để hiển thị dialog khi xung đột với phiên đang chạy.
    private var gameTitle: String = ""

    // Chờ user quyết định khi phiên cũ khác game (true = chơi game mới, false = tiếp tục game cũ).
    private var conflictDecision: CompletableDeferred<Boolean>? = null

    /**
     * Điểm vào của màn: xác định subscription rồi mới connect.
     * - 0 gói → Failed (chưa có gói thuê bao).
     * - 1 gói → tự dùng id gói đó, connect ngay.
     * - >1 gói → phát [ConnectStage.ChoosingSubscription] để UI hiện popup cho user chọn.
     *
     * [platform]/[appId] giữ cho cả luồng (Play-Now nếu appId > 0).
     */
    fun start(platform: String = "", appId: Int = 0, gameId: Int? = null, gameTitle: String = "") {
        if (connectJob?.isActive == true || resolveJob?.isActive == true) return
        this.platform = platform
        this.appId = appId
        this.gameId = gameId
        this.gameTitle = gameTitle
        resolveJob = viewModelScope.launch {
            _stage.value = ConnectStage.ResolvingSubscription
            getUserSubscriptions().collect { state ->
                when (state) {
                    is UiState.Success -> {
                        val subs = state.data
                        when {
                            subs.isEmpty() -> fail("Bạn chưa có gói thuê bao nào", canRetry = false)
                            subs.size == 1 -> connect(subs.first().id, platform, appId, gameId, gameTitle)
                            else -> _stage.value = ConnectStage.ChoosingSubscription(subs)
                        }
                    }
                    is UiState.Error -> fail(state.message)
                    else -> Unit
                }
            }
        }
    }

    /** User đã chọn 1 gói ở popup → chốt id và tiếp tục connect. */
    fun onSubscriptionChosen(subscriptionId: Int) {
        connect(subscriptionId, platform, appId, gameId, gameTitle)
    }

    /** Thử lại: nếu đã chốt subscription thì connect lại luôn, chưa thì resolve lại từ đầu. */
    fun retry() {
        val id = resolvedSubscriptionId
        if (id != null) connect(id, platform, appId, gameId, gameTitle)
        else start(platform, appId, gameId, gameTitle)
    }

    /**
     * User đã chọn ở dialog xung đột phiên ([ConnectStage.ConflictingSession]):
     * [playNew] = true → end phiên cũ + tạo phiên mới với game mới; false → connect vào phiên cũ.
     */
    fun onSessionConflictResolved(playNew: Boolean) {
        conflictDecision?.complete(playNew)
        conflictDecision = null
    }

    /**
     * Bắt đầu (hoặc thử lại) luồng kết nối cho [subscriptionId].
     * [platform]/[appId]: nếu [appId] > 0 → Play-Now (mở sẵn game qua agent); 0 → chỉ stream Desktop.
     */
    fun connect(
        subscriptionId: Int,
        platform: String = "",
        appId: Int = 0,
        gameId: Int? = null,
        gameTitle: String = ""
    ) {
        if (connectJob?.isActive == true) return
        this.platform = platform
        this.appId = appId
        this.gameId = gameId
        this.gameTitle = gameTitle
        this.resolvedSubscriptionId = subscriptionId
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
                    if (appId > 0) {
                        launchGameFireAndForget(session.id)
                    } else {
                        Log.i(TAG, "Stage 3a bỏ qua (appId=$appId) → chỉ stream Desktop")
                    }
                }
                is TokenAuthResult.Rejected -> fail(r.message)
                is TokenAuthResult.Error -> fail(r.message)
            }
        }
    }

    /**
     * Stage 3a launch game qua agent — **FIRE-AND-FORGET**: chạy ở coroutine riêng, KHÔNG chặn luồng
     * connect/stream. Mọi lỗi (agent-token/launch/exception) đều bị nuốt → stream Desktop vẫn tiếp tục,
     * user tự mở game nếu cần. Agent không track state nên ta cũng không chờ/không retry.
     */
    private fun launchGameFireAndForget(sessionId: Int) {
        viewModelScope.launch {
            runCatching { ensureAgentTokenAndLaunch(sessionId) }
                .onFailure { Log.w(TAG, "Stage 3a lỗi (bỏ qua, vẫn stream Desktop): ${it.message}") }
        }
    }

    /** Stage 3a — lấy agent token rồi launch game. Fire-and-forget: fail vẫn giữ stream Desktop. */
    private suspend fun ensureAgentTokenAndLaunch(sessionId: Int) {
        val p = platform.ifEmpty { DEFAULT_PLATFORM }
        Log.i(TAG, "Stage 3a: agent launch — session=$sessionId platform=$p appId=$appId")

        val tokenState = getAgentToken(sessionId)
        if (tokenState is UiState.Success) {
            val result = launchGame(tokenState.data, p, appId)
            Log.i(TAG, "Stage 3a: agent /launch result = $result")
        } else {
            Log.w(TAG, "Stage 3a: agent-token thất bại ($tokenState) → bỏ qua launch, stream Desktop")
        }
    }

    /**
     * Stage 0. Tạo phiên mới; nếu backend báo gói đã có phiên active (409 SESSION_INVALID_STATE)
     * thì giải quyết phiên cũ (trùng game → tiếp tục; khác game → hỏi user). Trả null nếu lỗi/chưa có
     * phiên để connect (đã set Failed hoặc đang chờ ở stage khác).
     */
    private suspend fun startSessionOrNull(subscriptionId: Int): Session? {
        val (session, alreadyHasActiveSession) = requestNewSession(subscriptionId)
        if (session != null) return session
        if (alreadyHasActiveSession) return resolveActiveSessionOrNull(subscriptionId)
        return null
    }

    /**
     * Gọi POST /sessions với [gameId]. Trả (phiên mới, false) nếu tạo được; (null, true) nếu gói đã có
     * phiên active (409 SESSION_INVALID_STATE); (null, false) nếu lỗi khác (đã set Failed).
     */
    private suspend fun requestNewSession(subscriptionId: Int): Pair<Session?, Boolean> {
        var session: Session? = null
        var alreadyHasActiveSession = false
        startSession(subscriptionId, gameId).collect { state ->
            when (state) {
                is UiState.Success -> session = state.data
                is UiState.Error ->
                    if (ApiErrorCode.from(state.code) == ApiErrorCode.SESSION_INVALID_STATE) {
                        alreadyHasActiveSession = true
                    } else {
                        fail(state.message)
                    }
                else -> Unit
            }
        }
        return session to alreadyHasActiveSession
    }

    /**
     * Gói đã có phiên đang chạy → lấy phiên đó (GET active-session) và xử lý theo game
     * (coi game_id = null hoặc 0/empty là "không gắn game"):
     * - Cùng "game" (trùng game_id, hoặc cả hai đều không gắn game) → tiếp tục phiên cũ.
     * - Cả hai đều gắn game cụ thể nhưng KHÁC nhau → hiện dialog cho user chọn (tiếp tục cũ / chơi mới).
     * - Các trường hợp lệch còn lại (một bên không gắn game) → tự động end cũ + tạo mới (không hỏi).
     */
    private suspend fun resolveActiveSessionOrNull(subscriptionId: Int): Session? {
        val active = activeSessionOrNull(subscriptionId) ?: return null

        val oldGameId = active.gameId?.takeIf { it != 0 }
        val newGameId = gameId?.takeIf { it != 0 }

        // Trùng game (kể cả cả hai đều không gắn game) → tiếp tục phiên cũ.
        if (oldGameId == newGameId) return active

        // Cả hai cùng gắn game cụ thể nhưng khác nhau → hỏi user.
        if (oldGameId != null && newGameId != null) {
            _stage.value = ConnectStage.ConflictingSession(
                oldGameTitle = active.gameTitle.orEmpty(),
                newGameTitle = gameTitle
            )
            val playNew = awaitConflictDecision()
            if (!playNew) return active // Tiếp tục game cũ → connect thẳng vào phiên cũ.
        }

        // Còn lại (một bên không gắn game), hoặc user chọn chơi game mới → end cũ + tạo mới.
        return endOldAndStartNew(subscriptionId, active.id)
    }

    /** End phiên cũ [oldSessionId] rồi tạo phiên mới với [gameId] hiện tại. null nếu lỗi (đã set Failed). */
    private suspend fun endOldAndStartNew(subscriptionId: Int, oldSessionId: Int): Session? {
        _stage.value = ConnectStage.CreatingSession
        if (!endSessionAndWait(oldSessionId)) return null
        val (session, stillActive) = requestNewSession(subscriptionId)
        if (session != null) return session
        if (stillActive) fail("Phiên cũ chưa kết thúc, vui lòng thử lại")
        return null
    }

    /** Treo tới khi user chọn ở dialog xung đột (true = game mới, false = game cũ). */
    private suspend fun awaitConflictDecision(): Boolean {
        val deferred = CompletableDeferred<Boolean>()
        conflictDecision = deferred
        return deferred.await()
    }

    /** Lấy phiên đang active của [subscriptionId]. null (đã set Failed) nếu lỗi. */
    private suspend fun activeSessionOrNull(subscriptionId: Int): Session? {
        var session: Session? = null
        getActiveSessionBySubscription(subscriptionId).collect { state ->
            when (state) {
                is UiState.Success -> session = state.data
                is UiState.Error -> fail(state.message)
                else -> Unit
            }
        }
        return session
    }

    /** End phiên [id]; true nếu thành công. Set Failed nếu lỗi. */
    private suspend fun endSessionAndWait(id: Int): Boolean {
        var ok = false
        endSession(id).collect { state ->
            when (state) {
                is UiState.Success -> ok = true
                is UiState.Error -> fail(state.message)
                else -> Unit
            }
        }
        return ok
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

    private fun fail(message: String, canRetry: Boolean = true) {
        heartbeatJob?.cancel()
        _stage.value = ConnectStage.Failed(message, canRetry)
    }

    /**
     * Hủy kết nối khi rời màn: chỉ dừng các job (đặc biệt là heartbeat). KHÔNG /close game hay /end
     * phiên — server tự đóng VM khi không còn nhận heartbeat, nên app không cần end chủ động.
     */
    fun cancelAndRelease() {
        resolveJob?.cancel()
        connectJob?.cancel()
        heartbeatJob?.cancel()
        sessionId = null
    }

    override fun onCleared() {
        super.onCleared()
        cancelAndRelease()
    }

    private companion object {
        const val TAG = "WindowsConnect"
        const val POLL_INTERVAL_MS = 7_000L
        const val RATE_LIMIT_BACKOFF_MS = 15_000L
        const val POLL_BUDGET_MS = 5 * 60 * 1000L
        const val HEARTBEAT_INTERVAL_MS = 30_000L
        const val DEFAULT_PLATFORM = "steam"
    }
}
