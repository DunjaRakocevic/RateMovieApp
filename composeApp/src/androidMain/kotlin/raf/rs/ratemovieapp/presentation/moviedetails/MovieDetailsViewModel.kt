package raf.rs.ratemovieapp.presentation.moviedetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import raf.rs.ratemovieapp.data.repository.MoviesRepository
import raf.rs.ratemovieapp.presentation.moviedetails.MovieDetailsContract.Event
import raf.rs.ratemovieapp.presentation.moviedetails.MovieDetailsContract.Effect
import raf.rs.ratemovieapp.presentation.moviedetails.MovieDetailsContract.Status
import raf.rs.ratemovieapp.presentation.moviedetails.MovieDetailsContract.State


class MovieDetailsViewModel(
    private val repo: MoviesRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    private val _effects = Channel<Effect>(Channel.BUFFERED)
    val effects: Flow<Effect> = _effects.receiveAsFlow()

    private var movieId: String? = savedStateHandle.get<String>(KEY_MOVIE_ID)
    private var loadJob: Job? = null

    fun setMovieId(id: String) {
        if (movieId == id) return
        movieId = id
        savedStateHandle[KEY_MOVIE_ID] = id
        load()
    }

    fun onEvent(event: Event) {
        when (event) {
            Event.ScreenStarted -> load()
            Event.Retry -> load()
            Event.BackClicked -> viewModelScope.launch { _effects.send(Effect.NavigateBack) }
            Event.PlayTrailerClicked -> playTrailer()
        }
    }

    private fun load() {
        val id = movieId ?: return
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _state.update { it.copy(status = Status.Loading) }

            runCatching {
                val detailsD = async { repo.getMovieDetails(id).getOrThrow() }
                val castD = async { repo.getMovieCast(id, pageSize = 10).getOrThrow() }
                val imagesD = async { repo.getMovieBackdrops(id).getOrThrow() }
                val videosD = async { repo.getMovieTrailers(id).getOrThrow() }

                val details = detailsD.await()
                val cast = castD.await()
                val images = imagesD.await()
                val videos = videosD.await()

                _state.update {
                    it.copy(
                        status = Status.Success,
                        details = details,
                        cast = cast,
                        backdrops = images,
                        trailers = videos
                    )
                }
            }.onFailure { e ->
                _state.update { it.copy(status = Status.Error(e.message ?: "Failed to load details")) }
            }
        }
    }

    private fun playTrailer() {
        val trailer = _state.value.trailers.firstOrNull()
        if (trailer == null) {
            viewModelScope.launch { _effects.send(Effect.ShowMessage("Trailer not available")) }
            return
        }
        viewModelScope.launch { _effects.send(Effect.OpenYoutube(trailer.youtubeUrl)) }
    }

    companion object {
        private const val KEY_MOVIE_ID = "details_movie_id"
    }
}
