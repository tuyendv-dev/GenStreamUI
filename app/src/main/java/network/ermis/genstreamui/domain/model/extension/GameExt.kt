package network.ermis.genstreamui.domain.model.extension

import network.ermis.genstreamui.domain.model.Game

/**
 * Các extension lấy URL ảnh của [network.ermis.genstreamui.domain.model.Game] với logic fallback dùng lại nhiều nơi,
 * tránh lặp chuỗi ifBlank rải rác ở Adapter/Fragment/Activity.
 */

/** Ảnh đại diện (cover/thumbnail) của game — dùng cho card trong list. */
fun Game.getGameImage(): String =
    mainCapsule.ifBlank { headerImage }
        .ifBlank { coverImageUrl }

/** Ảnh banner ngang lớn — ưu tiên heroImage, fallback dần. */
fun Game.getGameBanner(): String =
    heroImage.ifBlank { mainCapsule }
        .ifBlank { headerImage }
        .ifBlank { coverImageUrl }

/** Ảnh nền (background) cho màn chi tiết/chơi game. */
fun Game.getGameBackground(): String =
    heroImage.ifBlank { mainCapsule }
        .ifBlank { headerImage }
        .ifBlank { coverImageUrl }

/** Mô tả ngắn hiển thị UI — ưu tiên shortDescription, fallback dần. */
fun Game.getShortDescriptionExt(): String =
    shortDescription.ifBlank { tagline }
        .ifBlank { description }
