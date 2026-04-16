package raf.rs.ratemovieapp.presentation.movieslist

import raf.rs.ratemovieapp.data.model.Movie
import raf.rs.ratemovieapp.presentation.filter.FilterParams

object MoviesListContract {

    enum class SortOption(val apiField: String, val label: String) {
        Rating("imdb_rating", "Rating"),
        Year("year", "Year"),
        Title("title", "Title"),
        Popularity("popularity", "Popularity"),
    }

    sealed class Status {
        data object Loading : Status()
        data object Empty : Status()
        data class Error(val message: String) : Status()
        data object Success : Status()
    }

    data class State(
        val status: Status = Status.Loading,
        val movies: List<Movie> = emptyList(),
        val sort: SortOption = SortOption.Rating,
        val appliedFilters: FilterParams = FilterParams(),
        val moviesCountText: String = "0 movies",
        val activeFiltersCount: Int = 0
    )

    sealed class Event {
        data object ScreenStarted : Event()
        data object Retry : Event()
        data object SortPillClicked : Event()
        data object FilterClicked : Event()
        data class FiltersApplied(val params: FilterParams) : Event()
        data class MovieClicked(val id: Int) : Event()
    }

    sealed class Effect {
        data object NavigateToFilters : Effect()
        data class NavigateToDetails(val id: Int) : Effect()
        data class ShowMessage(val message: String) : Effect()
    }
}