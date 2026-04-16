package raf.rs.ratemovieapp.presentation.movieslist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import raf.rs.ratemovieapp.data.model.Movie
import raf.rs.ratemovieapp.presentation.movieslist.MoviesListContract.Effect
import raf.rs.ratemovieapp.presentation.movieslist.MoviesListContract.Event
import raf.rs.ratemovieapp.presentation.movieslist.MoviesListContract.State
import raf.rs.ratemovieapp.presentation.movieslist.MoviesListContract.Status

private val FilterRed = Color(0xFFE11D48)
private val StarGold = Color(0xFFFFCE1F)

@OptIn(ExperimentalMaterial3Api::class)
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
                is Effect.ShowMessage -> Unit
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🎬", fontSize = 20.sp)
                        Spacer(Modifier.width(8.dp))
                        Text("Premiere", fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    val filterLabel = if (s.activeFiltersCount > 0)
                        "⊞ Filter (${s.activeFiltersCount})" else "⊞ Filter"
                    Button(
                        onClick = { onEvent(Event.FilterClicked) },
                        colors = ButtonDefaults.buttonColors(containerColor = FilterRed),
                        shape = RoundedCornerShape(50.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(filterLabel, color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.width(8.dp))
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AssistChip(
                    onClick = { onEvent(Event.SortPillClicked) },
                    label = { Text("Sort: ${s.sort.label} ↓ ▼") }
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
                        items(s.movies, key = { it.id }) { movie ->
                            MovieListItem(movie = movie, onClick = { onEvent(Event.MovieClicked(movie.id)) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MovieListItem(movie: Movie, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(10.dp)
    ) {
        AsyncImage(
            model = movie.posterUrl,
            contentDescription = movie.title,
            modifier = Modifier
                .width(85.dp)
                .fillMaxHeight()
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(8.dp)
                ),
            contentScale = ContentScale.Crop
        )

        Spacer(Modifier.width(10.dp))

        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                movie.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                movie.year.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.semantics {
                    contentDescription = "IMDB rating ${movie.imdbRating} with ${movie.imdbVotes} votes"
                }
            ) {
                Text("★", color = StarGold, fontSize = 14.sp)
                Spacer(Modifier.width(4.dp))
                Text(
                    movie.imdbRating.toString(),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "${formatVotes(movie.imdbVotes)} votes",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                movie.genres.take(3).forEach { genre ->
                    AssistChip(
                        onClick = {},
                        label = { Text(genre.name, style = MaterialTheme.typography.labelSmall) },
                        shape = RoundedCornerShape(50.dp),
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            }
        }
    }
}

private fun formatVotes(votes: Int): String = when {
    votes >= 1_000_000 -> String.format("%.1fM", votes / 1_000_000f)
    votes >= 1_000 -> String.format("%.0fK", votes / 1_000f)
    else -> votes.toString()
}
