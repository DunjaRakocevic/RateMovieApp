package raf.rs.ratemovieapp.data.repository

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.longOrNull
import raf.rs.ratemovieapp.data.model.CastMember
import raf.rs.ratemovieapp.data.model.Genre
import raf.rs.ratemovieapp.data.model.Movie
import raf.rs.ratemovieapp.data.model.MovieDetails
import raf.rs.ratemovieapp.data.model.MovieImage
import raf.rs.ratemovieapp.data.model.MovieVideo
import raf.rs.ratemovieapp.data.remote.ImageConfig
import raf.rs.ratemovieapp.data.remote.ImageUrlBuilder
import raf.rs.ratemovieapp.data.remote.MoviesApi
import raf.rs.ratemovieapp.presentation.filter.FilterParams

class MoviesRepositoryImpl(
    private val api: MoviesApi,
    private val json: Json
) : MoviesRepository {

    private val configMutex = Mutex()
    private var imageBuilder: ImageUrlBuilder? = null

    override suspend fun getMovies(
        pageSize: Int,
        sortBy: String,
        sortOrder: String,
        filters: FilterParams
    ): Result<List<Movie>> = runCatching {
        ensureImageBuilder()
        val response = json.parseToJsonElement(api.getMovies(
            pageSize = pageSize,
            sortBy = sortBy,
            sortOrder = sortOrder,
            genreId = filters.genreId,
            query = filters.query.takeIf { it.isNotBlank() },
            minYear = filters.minYear,
            maxYear = filters.maxYear,
            minRating = filters.minRating
        ))

        response.asItemArray().mapNotNull { movieObj ->
            val id = movieObj.int("id") ?: return@mapNotNull null
            Movie(
                id = id,
                title = movieObj.string("title") ?: "Unknown",
                year = movieObj.int("year") ?: movieObj.int("release_year") ?: 0,
                runtimeMinutes = movieObj.int("runtime_minutes")
                    ?: movieObj.int("runtime")
                    ?: movieObj.int("duration")
                    ?: 0,
                imdbRating = movieObj.float("imdb_rating") ?: 0f,
                imdbVotes = movieObj.int("imdb_votes") ?: 0,
                posterUrl = toPosterUrl(
                    movieObj.string("poster_path")
                        ?: movieObj.string("poster_file_path")
                        ?: movieObj.string("poster")
                        ?: movieObj.string("poster_url")
                ),
                genres = movieObj.genres()
            )
        }
    }

    override suspend fun getGenres(): Result<List<Genre>> = runCatching {
        json.parseToJsonElement(api.getGenres()).asItemArray().mapNotNull { genreObj ->
            val id = genreObj.int("id") ?: return@mapNotNull null
            Genre(id, genreObj.string("name") ?: "Unknown")
        }
    }

    override suspend fun getMovieDetails(id: Int): Result<MovieDetails> = runCatching {
        ensureImageBuilder()
        val obj = json.parseToJsonElement(api.getMovieDetails(id)).asObject()
        MovieDetails(
            id = obj.int("id") ?: id,
            title = obj.string("title") ?: "Unknown",
            overview = obj.string("overview") ?: "",
            year = obj.int("year") ?: obj.int("release_year") ?: 0,
            runtimeMinutes = obj.int("runtime_minutes") ?: obj.int("runtime") ?: 0,
            imdbRating = obj.float("imdb_rating") ?: 0f,
            imdbVotes = obj.int("imdb_votes") ?: 0,
            tmdbRating = obj.float("tmdb_rating") ?: obj.float("vote_average") ?: 0f,
            genres = obj.genres(),
            posterUrl = toPosterUrl(
                obj.string("poster_path")
                    ?: obj.string("poster_file_path")
                    ?: obj.string("poster")
                    ?: obj.string("poster_url")
            ),
            backdropUrl = toBackdropUrl(
                obj.string("backdrop_path")
                    ?: obj.string("backdrop_file_path")
                    ?: obj.string("backdrop")
                    ?: obj.string("backdrop_url")
            ),
            budget = obj.long("budget") ?: 0L,
            revenue = obj.long("revenue") ?: 0L,
            language = obj.string("language") ?: obj.string("original_language") ?: "",
            popularity = obj.float("popularity") ?: 0f
        )
    }

    override suspend fun getMovieCast(id: Int, pageSize: Int): Result<List<CastMember>> = runCatching {
        ensureImageBuilder()
        json.parseToJsonElement(api.getMovieCast(id, pageSize)).asItemArray().mapIndexed { index, castObj ->
            CastMember(
                id = castObj.int("id") ?: index,
                name = castObj.string("name") ?: castObj.string("character") ?: "Unknown",
                profileUrl = toPosterUrl(
                    castObj.string("profile_path")
                        ?: castObj.string("profile")
                        ?: castObj.string("profile_url")
                )
            )
        }
    }

    override suspend fun getMovieBackdrops(id: Int): Result<List<MovieImage>> = runCatching {
        ensureImageBuilder()
        json.parseToJsonElement(api.getMovieImages(id, type = "backdrop")).asItemArray().map { imageObj ->
            MovieImage(
                fileUrl = toBackdropUrl(
                    imageObj.string("file_path")
                        ?: imageObj.string("path")
                        ?: imageObj.string("url")
                )
            )
        }
    }

    override suspend fun getMovieTrailers(id: Int): Result<List<MovieVideo>> = runCatching {
        json.parseToJsonElement(api.getMovieVideos(id, type = "Trailer")).asItemArray().mapNotNull { videoObj ->
            val key = videoObj.string("youtube_key")
                ?: videoObj.string("key")
                ?: videoObj.string("video_key")

            val youtubeUrl = when {
                key.isNullOrBlank() -> videoObj.string("youtube_url") ?: videoObj.string("url")
                else -> "https://www.youtube.com/watch?v=$key"
            }

            youtubeUrl?.let {
                MovieVideo(
                    name = videoObj.string("name") ?: "Trailer",
                    youtubeUrl = it
                )
            }
        }
    }

    private suspend fun ensureImageBuilder(): ImageUrlBuilder {
        return configMutex.withLock {
            imageBuilder?.let { return@withLock it }

            val configJson = json.parseToJsonElement(api.getConfig()).asObject()
            val imageBaseUrl = configJson.string("image_base_url")
                ?: configJson.obj("images")?.string("secure_base_url")
                ?: configJson.obj("images")?.string("base_url")
                ?: ""

            val posterSize = configJson.firstStringFromArray("poster_sizes")
                ?: configJson.obj("images")?.firstStringFromArray("poster_sizes")
                ?: "w342"

            val backdropSize = configJson.firstStringFromArray("backdrop_sizes")
                ?: configJson.obj("images")?.firstStringFromArray("backdrop_sizes")
                ?: "w780"

            ImageUrlBuilder(
                ImageConfig(
                    baseUrl = imageBaseUrl,
                    posterSize = posterSize,
                    backdropSize = backdropSize
                )
            ).also { imageBuilder = it }
        }
    }

    private fun toPosterUrl(pathOrUrl: String?): String? {
        if (pathOrUrl.isNullOrBlank()) return null
        if (pathOrUrl.startsWith("http")) return pathOrUrl
        return imageBuilder?.poster(pathOrUrl) ?: pathOrUrl
    }

    private fun toBackdropUrl(pathOrUrl: String?): String? {
        if (pathOrUrl.isNullOrBlank()) return null
        if (pathOrUrl.startsWith("http")) return pathOrUrl
        return imageBuilder?.backdrop(pathOrUrl) ?: pathOrUrl
    }
}

private fun JsonElement.asObject(): JsonObject =
    this as? JsonObject ?: JsonObject(emptyMap())

private fun JsonElement.asItemArray(): List<JsonObject> {
    return when (this) {
        is JsonArray -> this.mapNotNull { it as? JsonObject }
        is JsonObject -> {
            val items = this["results"]
                ?: this["items"]
                ?: this["movies"]
                ?: this["cast"]
                ?: this["images"]
                ?: this["videos"]
            (items as? JsonArray)?.mapNotNull { it as? JsonObject } ?: emptyList()
        }
        else -> emptyList()
    }
}

private fun JsonObject.obj(name: String): JsonObject? = this[name] as? JsonObject

private fun JsonObject.string(name: String): String? =
    (this[name] as? JsonPrimitive)?.contentOrNull

private fun JsonObject.int(name: String): Int? =
    (this[name] as? JsonPrimitive)?.intOrNull

private fun JsonObject.long(name: String): Long? {
    return (this[name] as? JsonPrimitive)?.longOrNull
}

private fun JsonObject.float(name: String): Float? =
    (this[name] as? JsonPrimitive)?.floatOrNull

private fun JsonObject.firstStringFromArray(name: String): String? {
    val arr = (this[name] as? JsonArray) ?: return null
    return arr.mapNotNull { (it as? JsonPrimitive)?.contentOrNull }
        .firstOrNull { it.isNotBlank() }
}

private fun JsonObject.genres(): List<Genre> {
    val rawGenres = this["genres"]
    return when (rawGenres) {
        is JsonArray -> rawGenres.mapNotNull { item ->
            when (item) {
                is JsonObject -> {
                    val id = item.int("id") ?: return@mapNotNull null
                    Genre(id, item.string("name") ?: "Unknown")
                }
                is JsonPrimitive -> {
                    if (item.isString) {
                        null
                    } else null
                }
                else -> null
            }
        }
        else -> emptyList()
    }
}
