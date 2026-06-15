package network.ermis.genstreamui.domain.model.mapper

import network.ermis.genstreamui.domain.model.ConnectionToken
import network.ermis.genstreamui.domain.model.dto.res.ConnectionTokenDTO

/**
 * Map DTO mạng -> model core domain cho token kết nối. Quy mọi field nullable về giá trị
 * an toàn cho [ConnectionToken]; làm phẳng [ConnectionTokenDTO.vmEndpoint] thành host/port.
 */
fun ConnectionTokenDTO.toDomain(): ConnectionToken = ConnectionToken(
    token = token.orEmpty(),
    expiresIn = expiresIn ?: 0,
    host = vmEndpoint?.host.orEmpty(),
    port = vmEndpoint?.port ?: 0,
    sessionId = sessionId ?: 0
)
