package network.ermis.genstreamui.domain.model.mapper

import network.ermis.genstreamui.domain.model.Session
import network.ermis.genstreamui.domain.model.dto.res.SessionDTO

/**
 * Map DTO mạng -> model core domain cho phiên chơi. Ranh giới data -> domain:
 * mọi field nullable từ [SessionDTO] được quy về giá trị an toàn cho [Session].
 */
fun SessionDTO.toDomain(): Session = Session(
    id = id ?: 0,
    userId = userId ?: 0,
    subscriptionId = subscriptionId ?: 0,
    status = status.orEmpty(),
    billingMode = billingMode.orEmpty(),
    startedAt = startedAt.orEmpty(),
    modeStartedAt = modeStartedAt.orEmpty(),
    endedAt = endedAt.orEmpty(),
    durationSeconds = durationSeconds ?: 0,
    overageSeconds = overageSeconds ?: 0,
    createdAt = createdAt.orEmpty(),
    serverId = serverId ?: 0,
    ipAddress = ipAddress.orEmpty(),
    sunshinePort = sunshinePort ?: 0,
    node = node.orEmpty(),
    proxmoxVmid = proxmoxVmid ?: 0,
    vmUuid = vmUuid.orEmpty(),
    lastError = lastError.orEmpty(),
    readyAt = readyAt.orEmpty(),
    lastHeartbeatAt = lastHeartbeatAt.orEmpty(),
    stoppedAt = stoppedAt.orEmpty(),
    userEmail = userEmail.orEmpty(),
    serverHostname = serverHostname.orEmpty()
)
