package network.ermis.genstreamui.domain.model.mapper

import network.ermis.genstreamui.domain.model.Discovery
import network.ermis.genstreamui.domain.model.DiscoverySection
import network.ermis.genstreamui.domain.model.Game
import network.ermis.genstreamui.domain.model.GameSearchResult
import network.ermis.genstreamui.domain.model.GameTrailer
import network.ermis.genstreamui.domain.model.TrailerStream
import network.ermis.genstreamui.domain.model.dto.res.DiscoveryDataDTO
import network.ermis.genstreamui.domain.model.dto.res.DiscoverySectionDTO
import network.ermis.genstreamui.domain.model.dto.res.GameDTO
import network.ermis.genstreamui.domain.model.dto.res.GameTrailerDTO
import network.ermis.genstreamui.domain.model.dto.res.ResSearchGames
import network.ermis.genstreamui.domain.model.dto.res.TrailerStreamDTO

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
    detailedDescription = detailedDescription.orEmpty(),
    coverImageUrl = coverImageUrl.orEmpty(),
    mainCapsule = mainCapsule.orEmpty(),
    portraitImage = portraitImage.orEmpty(),
    heroImage = heroImage.orEmpty(),
    headerImage = headerImage.orEmpty(),
    capsuleImage = capsuleImage.orEmpty(),
    backgroundImage = backgroundImage.orEmpty(),
    trailerUrl = trailerUrl.orEmpty(),
    screenshots = screenshots ?: emptyList(),
    trailers = trailers?.map { it.toDomain() } ?: emptyList(),
    categories = categories ?: emptyList(),
    platforms = platforms ?: emptyList(),
    publisher = publisher.orEmpty(),
    developers = developers ?: emptyList(),
    requiredAge = requiredAge ?: 0,
    supportedLanguages = supportedLanguages.orEmpty(),
    releaseDateText = releaseDateText.orEmpty(),
    releaseYear = releaseYear ?: 0,
    featured = featured ?: false,
    hot = hot ?: false,
    recommended = recommended ?: false,
    isActive = isActive ?: false,
    steamAppid = steamAppid ?: 0
)

fun GameTrailerDTO.toDomain(): GameTrailer = GameTrailer(
    name = name.orEmpty(),
    video = video.orEmpty(),
    thumbnail = thumbnail.orEmpty(),
    category = category ?: 0,
    adaptive = adaptive?.map { it.toDomain() } ?: emptyList(),
    microtrailer = microtrailer?.map { it.toDomain() } ?: emptyList()
)

fun TrailerStreamDTO.toDomain(): TrailerStream = TrailerStream(
    url = url.orEmpty(),
    encoding = encoding.orEmpty(),
    type = type.orEmpty()
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

/** Map response search/filter -> model domain. [total] ưu tiên pagination.total, fallback số item. */
fun ResSearchGames.toDomain(): GameSearchResult {
    val games = data?.map { it.toDomain() } ?: emptyList()
    return GameSearchResult(
        games = games,
        total = pagination?.total ?: games.size,
        page = pagination?.page ?: 1,
        totalPages = pagination?.totalPages ?: 1
    )
}
