package raf.rs.ratemovieapp.presentation.movieslist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import raf.rs.ratemovieapp.presentation.movieslist.MoviesListContract.Effect
import raf.rs.ratemovieapp.presentation.movieslist.MoviesListContract.Event
import raf.rs.ratemovieapp.presentation.movieslist.MoviesListContract.State
import raf.rs.ratemovieapp.presentation.movieslist.MoviesListContract.Status



@Composable
fun MoviesListScreen(
    state: StateFlow<State>,
    effects: Flow<Effect>,
    onEvent: (Event) -> Unit,
    onNavigateToFilters: () -> Unit,
    onNavigateToDetails: (Int) -> Unit
) {
    val s by state.collectAsState()

    LaunchedEffect(Unit) { onEvent(Event.ScreenStarted) }

    LaunchedEffect(Unit) {
        effects.collect { eff ->
            when (eff) {
                Effect.NavigateToFilters -> onNavigateToFilters()
                is Effect.NavigateToDetails -> onNavigateToDetails(eff.id)
                is Effect.ShowMessage -> { /* optional: Snackbar later */ }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Movies") },
                actions = {
                    BadgedBox(
                        badge = {
                            if (s.activeFiltersCount > 0) {
                                Badge { Text(s.activeFiltersCount.toString()) }
                            }
                        }
                    ) {
                        IconButton(onClick = { onEvent(Event.FilterClicked) }) {
                            Text(
                                if (s.activeFiltersCount > 0) "Filter (${s.activeFiltersCount})" else "Filter"
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Sort pill + count
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AssistChip(
                    onClick = { onEvent(Event.SortPillClicked) },
                    label = { Text("Sort: ${s.sort.label}") }
                )
                Text(s.moviesCountText, style = MaterialTheme.typography.bodyMedium)
            }

            when (val st = s.status) {
                Status.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }

                Status.Empty -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No movies found")
                }

                is Status.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(st.message)
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { onEvent(Event.Retry) }) { Text("Retry") }
                    }
                }

                Status.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(s.movies) { movie ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onEvent(Event.MovieClicked(movie.id)) }
                            ) {
                                Column(Modifier.padding(12.dp)) {
                                    Text(movie.title, style = MaterialTheme.typography.titleMedium)
                                    Text("${movie.year} • ${movie.runtimeMinutes} min")
                                    Text("IMDB: ${movie.imdbRating} (${movie.imdbVotes} votes)")
                                    Spacer(Modifier.height(6.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        movie.genres.take(3).forEach { g ->
                                            AssistChip(onClick = {}, label = { Text(g.name) })
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}