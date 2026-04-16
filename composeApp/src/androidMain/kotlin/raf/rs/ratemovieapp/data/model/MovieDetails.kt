package raf.rs.ratemovieapp.data.model


data class MovieDetails(
    val id: String,
    val title: String,
    val overview: String,
    val year: Int,
    val runtimeMinutes: Int,
    val imdbRating: Float,
    val imdbVotes: Int,
    val tmdbRating: Float,
    val genres: List<Genre>,
    val posterUrl: String?,
    val backdropUrl: String?,
    val budget: Long,
    val revenue: Long,
    val language: String,
    val popularity: Float
)
