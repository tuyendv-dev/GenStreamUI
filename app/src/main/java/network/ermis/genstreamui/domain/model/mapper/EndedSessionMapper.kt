package network.ermis.genstreamui.domain.model.mapper

import network.ermis.genstreamui.domain.model.EndedSession
import network.ermis.genstreamui.domain.model.Session
import network.ermis.genstreamui.domain.model.dto.res.EndSessionDataDTO

/** Map data của POST /sessions/{id}/end -> [EndedSession]; tái dùng [SessionDTO.toDomain]. */
fun EndSessionDataDTO.toDomain(): EndedSession = EndedSession(
    session = session?.toDomain() ?: Session(),
    consumedHours = consumedHours.orEmpty(),
    overageSeconds = overageSeconds ?: 0
)
