package raf.rs.ratemovieapp.presentation.filter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import raf.rs.ratemovieapp.data.repository.MoviesRepository
import raf.rs.ratemovieapp.presentation.filter.FilterContract.Effect
import raf.rs.ratemovieapp.presentation.filter.FilterContract.Event
import raf.rs.ratemovieapp.presentation.filter.FilterContract.State
import raf.rs.ratemovieapp.presentation.filter.FilterContract.Status

class FilterViewModel(
    private val repo: MoviesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    private val _effects = Channel<Effect>(Channel.BUFFERED)
    val effects: Flow<Effect> = _effects.receiveAsFlow()

    private var started = false

    fun onEvent(event: Event) {
        when (event) {
            Event.ScreenStarted -> {
                if (!started) {
                    started = true
                    loadGenres()
                }
            }

            Event.RetryGenres -> loadGenres()

            is Event.QueryChanged ->
                _state.update { it.copy(draft = it.draft.copy(query = event.value)) }

            is Event.GenreSelected ->
                _state.update { it.copy(draft = it.draft.copy(genreId = event.genreId)) }

            is Event.MinRatingChanged ->
                _state.update { it.copy(draft = it.draft.copy(minRating = event.value)) }

            Event.ClearAll ->
                _state.update { it.copy(draft = FilterParams()) }

            Event.Apply ->
                viewModelScope.launch { _effects.send(Effect.ReturnApplied(_state.value.draft)) }

            Event.Back ->
                viewModelScope.launch { _effects.send(Effect.ReturnDismissed) }
        }
    }

    private fun loadGenres() {
        viewModelScope.launch {
            _state.update { it.copy(status = Status.LoadingGenres) }

            repo.getGenres().fold(
                onSuccess = { genres ->
                    _state.update { it.copy(status = Status.Ready, genres = genres) }
                },
                onFailure = { e ->
                    _state.update { it.copy(status = Status.Error(e.message ?: "Failed to load genres")) }
                }
            )
        }
    }
}