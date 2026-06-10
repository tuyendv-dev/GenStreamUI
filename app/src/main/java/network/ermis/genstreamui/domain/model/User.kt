package network.ermis.genstreamui.domain.model

/**
 * Model core người dùng ở tầng domain — UI/ViewModel làm việc trực tiếp với model này
 * thay vì DTO. Map từ [network.ermis.genstreamui.domain.model.dto.res.UserDTO] qua UserMapper.
 *
 * Các trường non-null (có default) để UI khỏi phải xử lý null rải rác.
 */
data class User(
    val id: Int = 0,
    val email: String = "",
    val displayName: String = "",
    val avatarUrl: String = "",
    val emailVerified: Boolean = false,
    val isActive: Boolean = false,
    val roles: List<String> = emptyList(),
    val createdAt: String = ""
)
