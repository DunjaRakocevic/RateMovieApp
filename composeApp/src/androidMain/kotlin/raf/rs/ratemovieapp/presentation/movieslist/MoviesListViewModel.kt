package raf.rs.ratemovieapp.presentation.movieslist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import raf.rs.ratemovieapp.data.repository.MoviesRepository
import raf.rs.ratemovieapp.presentation.filter.FilterParams
import raf.rs.ratemovieapp.presentation.movieslist.MoviesListContract.Effect
import raf.rs.ratemovieapp.presentation.movieslist.MoviesListContract.Event
import raf.rs.ratemovieapp.presentation.movieslist.MoviesListContract.SortOption
import raf.rs.ratemovieapp.presentation.movieslist.MoviesListContract.State
import raf.rs.ratemovieapp.presentation.movieslist.MoviesListContract.Status

class MoviesListViewModel(
    private val repo: MoviesRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val initialSort = savedStateHandle.get<String>(KEY_SORT)
        ?.let { saved -> SortOption.entries.firstOrNull { it.name == saved } }
        ?: SortOption.Rating
    private val initialFilters = savedStateHandle.get<FilterParams>(KEY_FILTERS)
        ?: FilterParams()

    private val _state = MutableStateFlow(
        State(
            sort = initialSort,
            appliedFilters = initialFilters,
            activeFiltersCount = initialFilters.appliedCount()
        )
    )
    val state: StateFlow<State> = _state.asStateFlow()

    private val _effects = Channel<Effect>(Channel.BUFFERED)
    val effects: Flow<Effect> = _effects.receiveAsFlow()

    private var loadJob: Job? = null
    private var started = false

    fun onEvent(event: Event) {
        when (event) {
            Event.ScreenStarted -> {
                if (!started) {
                    started = true
                    loadMovies()
                }
            }
            Event.Retry -> loadMovies()
            Event.SortPillClicked -> {
                val next = nextSort(_state.value.sort)
                _state.update { it.copy(sort = next) }
                savedStateHandle[KEY_SORT] = next.name
                loadMovies()
            }
            Event.FilterClicked -> {
                viewModelScope.launch { _effects.send(Effect.NavigateToFilters) }
            }
            is Event.FiltersApplied -> {
                val count = event.params.appliedCount()
                _state.update {
                    it.copy(
                        appliedFilters = event.params,
                        activeFiltersCount = count
                    )
                }
                savedStateHandle[KEY_FILTERS] = event.params
                loadMovies()
            }
            is Event.MovieClicked -> {
                viewModelScope.launch { _effects.send(Effect.NavigateToDetails(event.id)) }
            }
        }
    }

    private fun nextSort(current: SortOption): SortOption {
        val values = SortOption.entries
        val idx = values.indexOf(current)
        return values[(idx + 1) % values.size]
    }

    private fun loadMovies() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _state.update { it.copy(status = Status.Loading) }
            val s = _state.value

            repo.getMovies(
                pageSize = 30,
                sortBy = s.sort.apiField,
                sortOrder = "desc",
                filters = s.appliedFilters
            ).fold(
                onSuccess = { movies ->
                    if (movies.isEmpty()) {
                        _state.update {
                            it.copy(
                                status = Status.Empty,
                                movies = emptyList(),
                                moviesCountText = "0 movies"
                            )
                        }
                    } else {
                        _state.update {
                            it.copy(
                                status = Status.Success,
                                movies = movies,
                                moviesCountText = "${movies.size} movies"
                            )
                        }
                    }
                },
                onFailure = { e ->
                    _state.update {
                        it.copy(status = Status.Error(e.message ?: "Failed to load movies"))
                    }
                }
            )
        }
    }

    companion object {
        private const val KEY_SORT = "movies_sort"
        private const val KEY_FILTERS = "movies_filters"
    }
}
