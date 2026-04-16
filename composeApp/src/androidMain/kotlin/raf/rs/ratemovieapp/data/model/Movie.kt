package raf.rs.ratemovieapp.data.model

data class Movie(
    val id: Int,
    val title: String,
    val year: Int,
    val runtimeMinutes: Int,
    val imdbRating: Float,
    val imdbVotes: Int,
    val posterUrl: String?,
    val genres: List<Genre>
)

