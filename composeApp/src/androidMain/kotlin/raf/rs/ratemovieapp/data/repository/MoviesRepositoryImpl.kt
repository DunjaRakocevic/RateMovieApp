package raf.rs.ratemovieapp.data.repository

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
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
    private val genresMutex = Mutex()
    private var genreNamesById: Map<Int, String>? = null

    override suspend fun getMovies(
        pageSize: Int,
        sortBy: String,
        sortOrder: String,
        filters: FilterParams
    ): Result<List<Movie>> = runCatching {
        ensureImageBuilder()
        val genresById = ensureGenresById()
        val genreIdsByName = genresById.entries.associate { it.value.lowercase() to it.key }
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

        val movies = response.asItemArray().mapNotNull { movieObj ->
            val id = movieObj.string("imdbId")
                ?: movieObj.string("imdb_id")
                ?: movieObj.string("id")
                ?: return@mapNotNull null
            Movie(
                id = id,
                title = movieObj.string("title") ?: "Unknown",
                year = movieObj.int("year") ?: movieObj.int("release_year") ?: 0,
                runtimeMinutes = movieObj.runtime(),
                imdbRating = movieObj.float("imdbRating")
                    ?: movieObj.float("imdb_rating")
                    ?: 0f,
                imdbVotes = movieObj.int("imdbVotes")
                    ?: movieObj.int("imdb_votes")
                    ?: 0,
                posterUrl = toPosterUrl(
                    movieObj.string("posterPath")
                        ?: movieObj.string("poster_path")
                        ?: movieObj.string("poster_file_path")
                        ?: movieObj.string("poster")
                        ?: movieObj.string("poster_url")
                ),
                genres = movieObj.genres(genresById, genreIdsByName)
            )
        }

        coroutineScope {
            movies.map { movie ->
                async {
                    enrichMovieFromDetailsIfMissing(
                        movie = movie,
                        genresById = genresById,
                        genreIdsByName = genreIdsByName
                    )
                }
            }.awaitAll()
        }
    }

    override suspend fun getGenres(): Result<List<Genre>> = runCatching {
        json.parseToJsonElement(api.getGenres()).asItemArray().mapNotNull { genreObj ->
            val id = genreObj.int("id") ?: return@mapNotNull null
            Genre(id, genreObj.string("name") ?: "Unknown")
        }
    }

    override suspend fun getMovieDetails(id: String): Result<MovieDetails> = runCatching {
        ensureImageBuilder()
        val genresById = ensureGenresById()
        val genreIdsByName = genresById.entries.associate { it.value.lowercase() to it.key }
        val obj = json.parseToJsonElement(api.getMovieDetails(id)).asObject()
        MovieDetails(
            id = obj.string("imdbId")
                ?: obj.string("imdb_id")
                ?: obj.string("id")
                ?: id,
            title = obj.string("title") ?: "Unknown",
            overview = obj.string("overview") ?: "",
            year = obj.int("year") ?: obj.int("release_year") ?: 0,
            runtimeMinutes = obj.runtime(),
            imdbRating = obj.float("imdbRating")
                ?: obj.float("imdb_rating")
                ?: 0f,
            imdbVotes = obj.int("imdbVotes")
                ?: obj.int("imdb_votes")
                ?: 0,
            tmdbRating = obj.float("tmdbRating")
                ?: obj.float("tmdb_rating")
                ?: obj.float("vote_average")
                ?: 0f,
            genres = obj.genres(genresById, genreIdsByName),
            posterUrl = toPosterUrl(
                obj.string("posterPath")
                    ?: obj.string("poster_path")
                    ?: obj.string("poster_file_path")
                    ?: obj.string("poster")
                    ?: obj.string("poster_url")
            ),
            backdropUrl = toBackdropUrl(
                obj.string("backdropPath")
                    ?: obj.string("backdrop_path")
                    ?: obj.string("backdrop_file_path")
                    ?: obj.string("backdrop")
                    ?: obj.string("backdrop_url")
            ),
            budget = obj.long("budget") ?: 0L,
            revenue = obj.long("revenue") ?: 0L,
            language = obj.string("languageCode")
                ?: obj.string("language")
                ?: obj.string("original_language")
                ?: "",
            popularity = obj.float("popularity") ?: 0f
        )
    }

    override suspend fun getMovieCast(id: String, pageSize: Int): Result<List<CastMember>> = runCatching {
        ensureImageBuilder()
        json.parseToJsonElement(api.getMovieCast(id, pageSize)).asItemArray().mapIndexed { index, castObj ->
            CastMember(
                id = castObj.int("id")
                    ?: castObj.string("imdbId")?.digitsAsIntOrNull()
                    ?: index,
                name = castObj.string("name") ?: castObj.string("character") ?: "Unknown",
                profileUrl = toPosterUrl(
                    castObj.string("profilePath")
                        ?: castObj.string("profile_path")
                        ?: castObj.string("profile")
                        ?: castObj.string("profile_url")
                )
            )
        }
    }

    override suspend fun getMovieBackdrops(id: String): Result<List<MovieImage>> = runCatching {
        ensureImageBuilder()
        val response = json.parseToJsonElement(api.getMovieImages(id, type = "backdrop"))
        val images = response.asBackdropArray()
        images.map { imageObj ->
            MovieImage(
                fileUrl = toBackdropUrl(
                    imageObj.string("filePath")
                        ?: imageObj.string("file_path")
                        ?: imageObj.string("path")
                        ?: imageObj.string("url")
                )
            )
        }
    }

    override suspend fun getMovieTrailers(id: String): Result<List<MovieVideo>> = runCatching {
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

            val configElement = json.parseToJsonElement(api.getConfig())
            val configJson = configElement.asObject()
            val imageBaseUrl = configJson.string("image_base_url")
                ?: configElement.configValue("image_base_url")
                ?: configJson.obj("images")?.string("secure_base_url")
                ?: configJson.obj("images")?.string("base_url")
                ?: ""

            val posterSize = configJson.firstStringFromArray("poster_sizes")
                ?: configElement.firstCommaSeparatedValue("poster_sizes")
                ?: configJson.obj("images")?.firstStringFromArray("poster_sizes")
                ?: "w342"

            val backdropSize = configJson.firstStringFromArray("backdrop_sizes")
                ?: configElement.firstCommaSeparatedValue("backdrop_sizes")
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

    private suspend fun ensureGenresById(): Map<Int, String> {
        return genresMutex.withLock {
            genreNamesById?.let { return@withLock it }
            val genres = json.parseToJsonElement(api.getGenres()).asItemArray()
                .mapNotNull { genreObj ->
                    val id = genreObj.int("id") ?: return@mapNotNull null
                    val name = genreObj.string("name") ?: return@mapNotNull null
                    id to name
                }
                .toMap()
            genres.also { genreNamesById = it }
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

    private suspend fun enrichMovieFromDetailsIfMissing(
        movie: Movie,
        genresById: Map<Int, String>,
        genreIdsByName: Map<String, Int>
    ): Movie {
        if (movie.runtimeMinutes > 0 && movie.genres.isNotEmpty()) return movie

        val details = runCatching {
            json.parseToJsonElement(api.getMovieDetails(movie.id)).asObject()
        }.getOrNull() ?: return movie

        val runtime = if (movie.runtimeMinutes > 0) movie.runtimeMinutes else details.runtime()
        val genres = if (movie.genres.isNotEmpty()) movie.genres else details.genres(genresById, genreIdsByName)

        if (runtime == movie.runtimeMinutes && genres == movie.genres) return movie
        return movie.copy(runtimeMinutes = runtime, genres = genres)
    }
}

private fun JsonElement.asObject(): JsonObject =
    this as? JsonObject ?: JsonObject(emptyMap())

private fun JsonElement.configValue(key: String): String? {
    val entries = this as? JsonArray ?: return null
    return entries.firstNotNullOfOrNull { entry ->
        val obj = entry as? JsonObject ?: return@firstNotNullOfOrNull null
        if (obj.string("key") != key) return@firstNotNullOfOrNull null
        obj.string("value")
    }
}

private fun JsonElement.firstCommaSeparatedValue(key: String): String? =
    configValue(key)
        ?.split(",")
        ?.map { it.trim() }
        ?.firstOrNull { it.isNotBlank() }

private fun JsonElement.asBackdropArray(): List<JsonObject> = when (this) {
    is JsonObject -> {
        val backdrops = this["backdrops"] as? JsonArray ?: return emptyList()
        backdrops.mapNotNull { it as? JsonObject }
    }
    else -> asItemArray()
}

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

private const val MAX_GENRE_ID_COLLISION_ATTEMPTS = 10_000

private fun JsonObject.genres(
    genresById: Map<Int, String>,
    genreIdsByName: Map<String, Int>
): List<Genre> {
    val result = mutableListOf<Genre>()
    val usedIds = mutableSetOf<Int>()
    val usedNames = mutableSetOf<String>()

    fun addById(id: Int) {
        val name = genresById[id] ?: "Unknown"
        if (usedIds.add(id)) {
            result += Genre(id, name)
            usedNames += name.lowercase()
        }
    }

    fun addByName(nameRaw: String) {
        val name = nameRaw.trim()
        if (name.isBlank()) return
        val normalizedName = name.lowercase()
        val mappedId = genreIdsByName[normalizedName]
        if (mappedId != null) {
            addById(mappedId)
            return
        }
        if (usedNames.add(normalizedName)) {
            var unknownId = unknownGenreId(name)
            var attempts = 0
            while ((genresById.containsKey(unknownId) || usedIds.contains(unknownId)) && attempts < MAX_GENRE_ID_COLLISION_ATTEMPTS) {
                unknownId -= 1
                attempts += 1
            }
            if (genresById.containsKey(unknownId) || usedIds.contains(unknownId)) {
                return
            }
            result += Genre(unknownId, name)
            usedIds += unknownId
        }
    }

    fun parseElement(element: JsonElement?) {
        when (element) {
            is JsonObject -> {
                val id = element.int("id")
                val name = element.string("name")
                when {
                    id != null && id > 0 -> addById(id)
                    !name.isNullOrBlank() -> addByName(name)
                }
            }
            is JsonPrimitive -> {
                val raw = element.contentOrNull?.trim().orEmpty()
                if (raw.isBlank()) return
                val asId = element.intOrNull ?: raw.toIntOrNull()
                if (asId != null && asId > 0) {
                    addById(asId)
                } else {
                    raw.split(",")
                        .map { it.trim() }
                        .filter { it.isNotBlank() }
                        .forEach(::addByName)
                }
            }
            is JsonArray -> element.forEach(::parseElement)
            else -> Unit
        }
    }

    parseElement(this["genres"])
    parseElement(this["genre_ids"])
    parseElement(this["genreIds"])
    parseElement(this["genre_names"])
    parseElement(this["genreNames"])
    parseElement(this["genre"])

    return result
}

private fun unknownGenreId(name: String): Int {
    val hash = name.lowercase().hashCode().toLong()
    val magnitude = kotlin.math.abs(hash).coerceAtLeast(1L)
    return (-magnitude).coerceIn(Int.MIN_VALUE.toLong(), -1L).toInt()
}

private fun String.digitsAsIntOrNull(): Int? {
    val digits = filter { it.isDigit() }
    if (digits.isBlank()) return null
    return digits.toIntOrNull()
}

/** Tries every known key name for runtime, accepting both Int and String values like "120 min". */
private fun JsonObject.runtime(): Int {
    val keys = listOf(
        "runtime_minutes",   // most common snake_case key from the API
        "runtimeMinutes",    // camelCase variant some endpoints return
        "runtime_min",
        "runtimeMins",
        "runtimeInMinutes",
        "runtime",
        "duration_minutes",
        "durationMinutes",
        "duration_min",
        "durationMins",
        "duration",
        "length_minutes",
        "length"
    )
    for (key in keys) {
        val prim = this[key] as? JsonPrimitive ?: continue
        val asInt = prim.intOrNull
        if (asInt != null && asInt > 0) return asInt
        val asFloat = prim.floatOrNull
        if (asFloat != null && asFloat > 0f) return asFloat.toInt()
        val asStr = prim.contentOrNull
        if (!asStr.isNullOrBlank()) {
            val extracted = asStr.filter { it.isDigit() }.toIntOrNull()
            if (extracted != null && extracted > 0) return extracted
        }
    }
    return 0
}
