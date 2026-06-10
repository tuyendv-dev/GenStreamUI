package network.ermis.genstreamui.database.cache

import network.ermis.genstreamui.domain.model.User

/**
 * Lưu [User] core vào [SharedPrefCommon]. Dùng chung khi đăng nhập và sau mỗi lần
 * getUserInformation thành công.
 */
fun SharedPrefCommon.saveUser(user: User?) {
    user ?: return
    userId = user.id
    userName = user.displayName
    userEmail = user.email
    userAvatarUrl = user.avatarUrl
}

/**
 * Dựng lại [User] từ thông tin đã cache (dùng để hiển thị ngay trước khi API trả về).
 * Chỉ gồm các trường được cache; phần còn lại để mặc định.
 */
fun SharedPrefCommon.cachedUser(): User = User(
    id = userId,
    email = userEmail,
    displayName = userName,
    avatarUrl = userAvatarUrl
)
