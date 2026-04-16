package raf.rs.ratemovieapp.presentation.moviedetails

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import raf.rs.ratemovieapp.presentation.moviedetails.MovieDetailsContract.Effect
import raf.rs.ratemovieapp.presentation.moviedetails.MovieDetailsContract.Event
import raf.rs.ratemovieapp.presentation.moviedetails.MovieDetailsContract.State
import raf.rs.ratemovieapp.presentation.moviedetails.MovieDetailsContract.Status

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailsScreen(
    state: StateFlow<State>,
    effects: Flow<Effect>,
    onEvent: (Event) -> Unit,
    onBack: () -> Unit
) {
    val s by state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) { onEvent(Event.ScreenStarted) }

    LaunchedEffect(Unit) {
        effects.collect { eff ->
            when (eff) {
                Effect.NavigateBack -> onBack()
                is Effect.OpenYoutube -> {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(eff.url))
                    context.startActivity(intent)
                }
                is Effect.ShowMessage -> {
                    // možeš kasnije Snackbar
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Details") },
                navigationIcon = {
                    IconButton(onClick = { onEvent(Event.BackClicked) }) { Text("←") }
                }
            )
        }
    ) { paddingValues: PaddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            when (val st = s.status) {
                Status.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }

                is Status.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(st.message)
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { onEvent(Event.Retry) }) { Text("Retry") }
                    }
                }

                Status.Success -> {
                    val d = s.details ?: return@Box

                    Column(Modifier.padding(12.dp)) {
                        Text(d.title, style = MaterialTheme.typography.headlineSmall)
                        Text("${d.year} • ${d.runtimeMinutes} min")

                        Spacer(Modifier.height(8.dp))
                        Text("IMDB: ${d.imdbRating} (${d.imdbVotes} votes)")
                        Text("TMDB: ${d.tmdbRating}")

                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { onEvent(Event.PlayTrailerClicked) }) {
                            Text("Play Trailer")
                        }

                        Spacer(Modifier.height(12.dp))
                        Text(d.overview)
                    }
                }
            }
        }
    }
}