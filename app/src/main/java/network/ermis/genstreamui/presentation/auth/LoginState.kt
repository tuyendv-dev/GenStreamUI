package network.ermis.genstreamui.presentation.auth

import network.ermis.genstreamui.common.UiState
import network.ermis.genstreamui.domain.model.dto.ResLoginDTO

/** State của màn Login. */
data class LoginState(
    val uiState: UiState<ResLoginDTO> = UiState.Idle
)
