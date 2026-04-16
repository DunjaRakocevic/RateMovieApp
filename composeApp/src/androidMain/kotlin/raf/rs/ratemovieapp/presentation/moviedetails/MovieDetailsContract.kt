package raf.rs.ratemovieapp.presentation.moviedetails

import raf.rs.ratemovieapp.data.model.CastMember
import raf.rs.ratemovieapp.data.model.MovieDetails
import raf.rs.ratemovieapp.data.model.MovieImage
import raf.rs.ratemovieapp.data.model.MovieVideo

object MovieDetailsContract {

    sealed class Status {
        data object Loading : Status()
        data class Error(val message: String) : Status()
        data object Success : Status()
    }

    data class State(
        val status: Status = Status.Loading,
        val details: MovieDetails? = null,
        val cast: List<CastMember> = emptyList(),
        val backdrops: List<MovieImage> = emptyList(),
        val trailers: List<MovieVideo> = emptyList()
    )

    sealed class Event {
        data object ScreenStarted : Event()
        data object Retry : Event()
        data object BackClicked : Event()
        data object PlayTrailerClicked : Event()
    }

    sealed class Effect {
        data object NavigateBack : Effect()
        data class OpenYoutube(val url: String) : Effect()
        data class ShowMessage(val message: String) : Effect()
    }
}