package network.ermis.genstreamui.domain.model.mapper

import network.ermis.genstreamui.domain.model.User
import network.ermis.genstreamui.domain.model.dto.res.UserDTO

/**
 * Map DTO mạng -> model core domain. Đây là ranh giới data -> domain:
 * mọi field nullable từ DTO được quy về giá trị an toàn cho [User].
 */
fun UserDTO.toDomain(): User = User(
    id = id ?: 0,
    email = email.orEmpty(),
    displayName = displayName.orEmpty(),
    avatarUrl = avatarUrl.orEmpty(),
    emailVerified = emailVerified ?: false,
    isActive = isActive ?: false,
    roles = roles ?: emptyList(),
    createdAt = createdAt.orEmpty()
)
