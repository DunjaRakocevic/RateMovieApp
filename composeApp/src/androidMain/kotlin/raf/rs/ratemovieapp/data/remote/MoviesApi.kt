package raf.rs.ratemovieapp.data.remote

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query
interface MoviesApi {
    @GET("movies")
    suspend fun getMovies(
        @Query("page_size") pageSize: Int,
        @Query("sort_by") sortBy: String,
        @Query("sort_order") sortOrder: String,
        @Query("genre_id") genreId: Int? = null,
        @Query("query") query: String? = null,
        @Query("min_year") minYear: Int? = null,
        @Query("max_year") maxYear: Int? = null,
        @Query("min_rating") minRating: Float? = null
    ): String

    @GET("genres")
    suspend fun getGenres(): String

    @GET("config")
    suspend fun getConfig(): String

    @GET("movies/{id}")
    suspend fun getMovieDetails(@Path("id") id: Int): String

    @GET("movies/{id}/cast")
    suspend fun getMovieCast(
        @Path("id") id: Int,
        @Query("page_size") pageSize: Int = 10
    ): String

    @GET("movies/{id}/images")
    suspend fun getMovieImages(
        @Path("id") id: Int,
        @Query("type") type: String = "backdrop"
    ): String

    @GET("movies/{id}/videos")
    suspend fun getMovieVideos(
        @Path("id") id: Int,
        @Query("type") type: String = "Trailer"
    ): String
}
