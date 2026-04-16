package raf.rs.ratemovieapp.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText

class MoviesApiImpl(
    private val client: HttpClient
) : MoviesApi {

    override suspend fun getMovies(
        pageSize: Int,
        sortBy: String,
        sortOrder: String,
        genreId: Int?,
        query: String?,
        minYear: Int?,
        maxYear: Int?,
        minRating: Float?
    ): String {
        return client.get("${BASE_URL}movies") {
            parameter("page_size", pageSize)
            parameter("sort_by", sortBy)
            parameter("sort_order", sortOrder)
            genreId?.let { parameter("genre_id", it) }
            query?.let { parameter("query", it) }
            minYear?.let { parameter("min_year", it) }
            maxYear?.let { parameter("max_year", it) }
            minRating?.let { parameter("min_rating", it) }
        }.bodyAsText()
    }

    override suspend fun getGenres(): String =
        client.get("${BASE_URL}genres").bodyAsText()

    override suspend fun getConfig(): String =
        client.get("${BASE_URL}config").bodyAsText()

    override suspend fun getMovieDetails(id: Int): String =
        client.get("${BASE_URL}movies/$id").bodyAsText()

    override suspend fun getMovieCast(id: Int, pageSize: Int): String {
        return client.get("${BASE_URL}movies/$id/cast") {
            parameter("page_size", pageSize)
        }.bodyAsText()
    }

    override suspend fun getMovieImages(id: Int, type: String): String {
        return client.get("${BASE_URL}movies/$id/images") {
            parameter("type", type)
        }.bodyAsText()
    }

    override suspend fun getMovieVideos(id: Int, type: String): String {
        return client.get("${BASE_URL}movies/$id/videos") {
            parameter("type", type)
        }.bodyAsText()
    }

    private companion object {
        const val BASE_URL = "https://rma.finlab.rs/"
    }
}
