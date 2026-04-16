package raf.rs.ratemovieapp.presentation.moviedetails

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
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
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(Unit) { onEvent(Event.ScreenStarted) }

    LaunchedEffect(Unit) {
        effects.collect { eff ->
            when (eff) {
                Effect.NavigateBack -> onBack()
                is Effect.OpenYoutube -> uriHandler.openUri(eff.url)
                is Effect.ShowMessage -> Unit
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
                    val scroll = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scroll)
                    ) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            AsyncImage(
                                model = d.backdropUrl,
                                contentDescription = d.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(56.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                                    .clickable { onEvent(Event.PlayTrailerClicked) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("▶", style = MaterialTheme.typography.titleLarge)
                            }
                            AsyncImage(
                                model = d.posterUrl,
                                contentDescription = "Poster",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(width = 110.dp, height = 155.dp)
                                    .offset(x = 16.dp, y = 160.dp)
                                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                            )
                        }

                        Spacer(Modifier.height(110.dp))

                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(d.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Text("${d.year} • ${d.runtimeMinutes} min")

                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("★")
                                    Spacer(Modifier.width(4.dp))
                                    Text("${d.imdbRating} (${d.imdbVotes})")
                                }
                                Text("TMDB ${d.tmdbRating}")
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                d.genres.take(4).forEach { genre ->
                                    AssistChip(onClick = {}, label = { Text(genre.name) })
                                }
                            }

                            Text(d.overview)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                InfoBadge("Budget", "$${d.budget}")
                                InfoBadge("Revenue", "$${d.revenue}")
                                InfoBadge("Language", d.language)
                                InfoBadge("Popularity", "${d.popularity}")
                            }

                            Text("Images", style = MaterialTheme.typography.titleMedium)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                s.backdrops.take(3).forEach { image ->
                                    AsyncImage(
                                        model = image.fileUrl,
                                        contentDescription = "Backdrop",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .width(180.dp)
                                            .height(100.dp)
                                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                    )
                                }
                            }

                            Text("Cast", style = MaterialTheme.typography.titleMedium)
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                s.cast.take(10).forEach { actor ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        AsyncImage(
                                            model = actor.profileUrl,
                                            contentDescription = actor.name,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(54.dp)
                                                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                                        )
                                        Spacer(Modifier.width(10.dp))
                                        Text(actor.name)
                                    }
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoBadge(label: String, value: String) {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}
