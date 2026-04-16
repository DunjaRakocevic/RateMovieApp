package raf.rs.ratemovieapp.data.repository

import raf.rs.ratemovieapp.data.model.*
import raf.rs.ratemovieapp.presentation.filter.FilterParams

interface MoviesRepository {
    suspend fun getMovies(
        pageSize: Int,
        sortBy: String,
        sortOrder: String,
        filters: FilterParams
    ): Result<List<Movie>>

    suspend fun getGenres(): Result<List<Genre>>

    suspend fun getMovieDetails(id: Int): Result<MovieDetails>
    suspend fun getMovieCast(id: Int, pageSize: Int): Result<List<CastMember>>
    suspend fun getMovieBackdrops(id: Int): Result<List<MovieImage>>
    suspend fun getMovieTrailers(id: Int): Result<List<MovieVideo>>
}