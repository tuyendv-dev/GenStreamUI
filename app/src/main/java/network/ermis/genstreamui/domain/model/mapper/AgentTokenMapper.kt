package network.ermis.genstreamui.domain.model.mapper

import network.ermis.genstreamui.domain.model.AgentToken
import network.ermis.genstreamui.domain.model.dto.res.AgentTokenDTO

/** Map DTO -> [AgentToken]; làm phẳng vm_endpoint thành host/port. */
fun AgentTokenDTO.toDomain(): AgentToken = AgentToken(
    token = token.orEmpty(),
    expiresIn = expiresIn ?: 0,
    host = vmEndpoint?.host.orEmpty(),
    port = vmEndpoint?.port ?: 0
)
