package network.ermis.genstreamui.domain.model.mapper

import network.ermis.genstreamui.domain.model.Game
import network.ermis.genstreamui.domain.model.entities.GameEntity

/**
 * Map giữa domain [Game] và [GameEntity] (Room). Đây là ranh giới domain <-> storage:
 * cả hai phía đều non-null nên ánh xạ 1:1, không cần xử lý default.
 */
fun Game.toEntity(): GameEntity = GameEntity(
    id = id,
    slug = slug,
    title = title,
    description = description,
    tagline = tagline,
    shortDescription = shortDescription,
    coverImageUrl = coverImageUrl,
    mainCapsule = mainCapsule,
    portraitImage = portraitImage,
    heroImage = heroImage,
    headerImage = headerImage,
    capsuleImage = capsuleImage,
    backgroundImage = backgroundImage,
    trailerUrl = trailerUrl,
    screenshots = screenshots,
    categories = categories,
    platforms = platforms,
    publisher = publisher,
    releaseYear = releaseYear,
    featured = featured,
    hot = hot,
    recommended = recommended,
    isActive = isActive
)

fun GameEntity.toDomain(): Game = Game(
    id = id,
    slug = slug,
    title = title,
    description = description,
    tagline = tagline,
    shortDescription = shortDescription,
    coverImageUrl = coverImageUrl,
    mainCapsule = mainCapsule,
    portraitImage = portraitImage,
    heroImage = heroImage,
    headerImage = headerImage,
    capsuleImage = capsuleImage,
    backgroundImage = backgroundImage,
    trailerUrl = trailerUrl,
    screenshots = screenshots,
    categories = categories,
    platforms = platforms,
    publisher = publisher,
    releaseYear = releaseYear,
    featured = featured,
    hot = hot,
    recommended = recommended,
    isActive = isActive
)
