package raf.rs.ratemovieapp.presentation.filter

import raf.rs.ratemovieapp.data.model.Genre

object FilterContract {

    sealed class Status {
        data object LoadingGenres : Status()
        data class Error(val message: String) : Status()
        data object Ready : Status()
    }

    data class State(
        val status: Status = Status.LoadingGenres,
        val genres: List<Genre> = emptyList(),
        val draft: FilterParams = FilterParams()
    )

    sealed class Event {
        data object ScreenStarted : Event()
        data class InitializeDraft(val value: FilterParams) : Event()
        data class QueryChanged(val value: String) : Event()
        data class GenreSelected(val genreId: Int?) : Event()
        data class MinYearChanged(val value: String) : Event()
        data class MaxYearChanged(val value: String) : Event()
        data class MinRatingChanged(val value: Float) : Event()
        data object ClearAll : Event()
        data object Apply : Event()
        data object Back : Event()
        data object RetryGenres : Event()
    }

    sealed class Effect {
        data class ReturnApplied(val params: FilterParams) : Effect()
        data object ReturnDismissed : Effect()
    }
}
