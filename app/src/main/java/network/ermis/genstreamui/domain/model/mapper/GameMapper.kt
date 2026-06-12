package network.ermis.genstreamui.domain.model.mapper

import network.ermis.genstreamui.domain.model.Discovery
import network.ermis.genstreamui.domain.model.DiscoverySection
import network.ermis.genstreamui.domain.model.Game
import network.ermis.genstreamui.domain.model.dto.res.DiscoveryDataDTO
import network.ermis.genstreamui.domain.model.dto.res.DiscoverySectionDTO
import network.ermis.genstreamui.domain.model.dto.res.GameDTO

/**
 * Map DTO mạng -> model core domain cho game/discovery. Đây là ranh giới data -> domain:
 * mọi field nullable từ DTO được quy về giá trị an toàn cho [Game] / [Discovery].
 */
fun GameDTO.toDomain(): Game = Game(
    id = id ?: 0,
    slug = slug.orEmpty(),
    title = title.orEmpty(),
    description = description.orEmpty(),
    tagline = tagline.orEmpty(),
    shortDescription = shortDescription.orEmpty(),
    coverImageUrl = coverImageUrl.orEmpty(),
    mainCapsule = mainCapsule.orEmpty(),
    portraitImage = portraitImage.orEmpty(),
    heroImage = heroImage.orEmpty(),
    headerImage = headerImage.orEmpty(),
    capsuleImage = capsuleImage.orEmpty(),
    backgroundImage = backgroundImage.orEmpty(),
    trailerUrl = trailerUrl.orEmpty(),
    screenshots = screenshots ?: emptyList(),
    categories = categories ?: emptyList(),
    platforms = platforms ?: emptyList(),
    publisher = publisher.orEmpty(),
    releaseYear = releaseYear ?: 0,
    featured = featured ?: false,
    hot = hot ?: false,
    recommended = recommended ?: false,
    isActive = isActive ?: false
)

fun DiscoverySectionDTO.toDomain(): DiscoverySection = DiscoverySection(
    category = category.orEmpty(),
    games = games?.map { it.toDomain() } ?: emptyList()
)

fun DiscoveryDataDTO.toDomain(): Discovery = Discovery(
    featured = featured?.map { it.toDomain() } ?: emptyList(),
    hot = hot?.map { it.toDomain() } ?: emptyList(),
    recommended = recommended?.map { it.toDomain() } ?: emptyList(),
    sections = sections?.map { it.toDomain() } ?: emptyList()
)
