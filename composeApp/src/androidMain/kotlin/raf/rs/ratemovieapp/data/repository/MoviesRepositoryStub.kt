package raf.rs.ratemovieapp.data.repository

import kotlinx.coroutines.delay
import raf.rs.ratemovieapp.data.model.*
import raf.rs.ratemovieapp.presentation.filter.FilterParams
import kotlin.random.Random

class MoviesRepositoryStub : MoviesRepository {

    private val genres = listOf(
        Genre(1, "Action"),
        Genre(2, "Comedy"),
        Genre(3, "Drama"),
        Genre(4, "Sci-Fi"),
        Genre(5, "Thriller")
    )

    override suspend fun getMovies(
        pageSize: Int,
        sortBy: String,
        sortOrder: String,
        filters: FilterParams
    ): Result<List<Movie>> {
        delay(350)

        val base = (1..60).map { id ->
            val movieGenres = genres.shuffled().take((id % 3) + 1)
            Movie(
                id = id,
                title = "Movie $id",
                year = 1995 + (id % 30),
                runtimeMinutes = 80 + (id % 60),
                imdbRating = (5.0 + (id % 50) / 10.0).toFloat().coerceAtMost(10f),
                imdbVotes = 5_000 + id * 321,
                posterUrl = null,
                genres = movieGenres
            )
        }

        var list = base
            .filter { filters.query.isBlank() || it.title.contains(filters.query, ignoreCase = true) }
            .filter { filters.genreId == null || it.genres.any { g -> g.id == filters.genreId } }
            .filter { filters.minYear == null || it.year >= filters.minYear!! }
            .filter { filters.maxYear == null || it.year <= filters.maxYear!! }
            .filter { filters.minRating == null || it.imdbRating >= filters.minRating!! }

        // sort (minimal)
        list = when (sortBy) {
            "imdb_rating" -> list.sortedByDescending { it.imdbRating }
            "year" -> list.sortedByDescending { it.year }
            "title" -> list.sortedBy { it.title }
            "popularity" -> list.sortedByDescending { it.imdbVotes } // stub popularity proxy
            else -> list
        }

        if (sortOrder == "asc") list = list.reversed()

        return Result.success(list.take(pageSize))
    }

    override suspend fun getGenres(): Result<List<Genre>> {
        delay(200)
        return Result.success(genres)
    }

    override suspend fun getMovieDetails(id: Int): Result<MovieDetails> {
        delay(250)
        val movieGenres = genres.shuffled().take(3)
        return Result.success(
            MovieDetails(
                id = id,
                title = "Movie $id",
                overview = "Overview for Movie $id. This is a stub overview used to test UI states.",
                year = 2005 + (id % 15),
                runtimeMinutes = 95 + (id % 45),
                imdbRating = (6.5 + (id % 30) / 10.0).toFloat().coerceAtMost(10f),
                imdbVotes = 100_000 + id * 777,
                tmdbRating = (6.0 + Random.nextDouble(0.0, 2.5)).toFloat().coerceAtMost(10f),
                genres = movieGenres,
                posterUrl = null,
                backdropUrl = null,
                budget = 10_000_000L + id * 1234L,
                revenue = 50_000_000L + id * 5678L,
                language = "en",
                popularity = 100f + id
            )
        )
    }

    override suspend fun getMovieCast(id: Int, pageSize: Int): Result<List<CastMember>> {
        delay(200)
        return Result.success((1..pageSize).map { CastMember(it, "Actor $it", profileUrl = null) })
    }

    override suspend fun getMovieBackdrops(id: Int): Result<List<MovieImage>> {
        delay(150)
        return Result.success(listOf(MovieImage(null), MovieImage(null), MovieImage(null)))
    }

    override suspend fun getMovieTrailers(id: Int): Result<List<MovieVideo>> {
        delay(150)
        return Result.success(
            listOf(
                MovieVideo("Trailer", "https://www.youtube.com/watch?v=dQw4w9WgXcQ")
            )
        )
    }
}