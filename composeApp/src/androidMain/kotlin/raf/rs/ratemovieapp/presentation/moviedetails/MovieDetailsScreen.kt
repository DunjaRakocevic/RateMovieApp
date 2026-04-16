package raf.rs.ratemovieapp.presentation.moviedetails

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import raf.rs.ratemovieapp.presentation.components.formatVotes
import raf.rs.ratemovieapp.presentation.moviedetails.MovieDetailsContract.Effect
import raf.rs.ratemovieapp.presentation.moviedetails.MovieDetailsContract.Event
import raf.rs.ratemovieapp.presentation.moviedetails.MovieDetailsContract.State
import raf.rs.ratemovieapp.presentation.moviedetails.MovieDetailsContract.Status

private val FilterRed = Color(0xFFE11D48)
private val StarGold = Color(0xFFFFCE1F)
private val TmdbBlue = Color(0xFF2196F3)

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

    Box(modifier = Modifier.fillMaxSize()) {
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
                    // Backdrop + poster + title
                    Box(modifier = Modifier.fillMaxWidth()) {
                        // Backdrop
                        AsyncImage(
                            model = d.backdropUrl,
                            contentDescription = d.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )

                        // Play button
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(64.dp)
                                .background(FilterRed, CircleShape)
                                .clickable { onEvent(Event.PlayTrailerClicked) }
                                .semantics { contentDescription = "Play trailer" },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("▶", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }

                        // Poster (overflows below backdrop)
                        AsyncImage(
                            model = d.posterUrl,
                            contentDescription = "Poster",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(width = 110.dp, height = 155.dp)
                                .offset(x = 16.dp, y = 140.dp)
                                .background(
                                    MaterialTheme.colorScheme.surface,
                                    RoundedCornerShape(8.dp)
                                )
                        )

                        // Title + year next to poster (overlapping below backdrop)
                        Column(
                            modifier = Modifier
                                .offset(x = 140.dp, y = 145.dp)
                                .padding(end = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                d.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                "${d.year} • ${d.runtimeMinutes} min",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.LightGray
                            )
                        }
                    }

                    // Spacer for poster + title overflow
                    Spacer(Modifier.height(80.dp))

                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Ratings row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics {
                                    contentDescription =
                                        "IMDB rating ${d.imdbRating} with ${d.imdbVotes} votes"
                                }
                        ) {
                            Text("★", color = StarGold, fontSize = 18.sp)
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "${d.imdbRating}/10",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "${formatVotes(d.imdbVotes)} votes",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.weight(1f))
                            Text(
                                "TMDB ${d.tmdbRating}",
                                color = TmdbBlue,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Genre chips (red)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            d.genres.take(4).forEach { genre ->
                                Box(
                                    modifier = Modifier
                                        .background(FilterRed, RoundedCornerShape(50.dp))
                                        .padding(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        genre.name,
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        // OVERVIEW
                        Text(
                            "OVERVIEW",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(d.overview)

                        // INFO
                        Text(
                            "INFO",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            InfoBadge("Budget", formatCurrency(d.budget))
                            InfoBadge("Revenue", formatCurrency(d.revenue))
                            InfoBadge("Language", d.language.uppercase())
                            InfoBadge("Popularity", String.format("%.1f", d.popularity))
                        }

                        // IMAGES
                        Text(
                            "IMAGES",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            s.backdrops.take(3).forEach { image ->
                                AsyncImage(
                                    model = image.fileUrl,
                                    contentDescription = "Backdrop image",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .width(180.dp)
                                        .height(100.dp)
                                        .background(
                                            MaterialTheme.colorScheme.surfaceVariant,
                                            RoundedCornerShape(8.dp)
                                        )
                                )
                            }
                        }

                        // ACTORS
                        Text(
                            "ACTORS",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Column {
                            s.cast.take(10).forEachIndexed { index, actor ->
                                if (index > 0) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = actor.profileUrl,
                                        contentDescription = actor.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(50.dp)
                                            .background(
                                                MaterialTheme.colorScheme.surfaceVariant,
                                                CircleShape
                                            )
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Text(actor.name, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }

                        Spacer(Modifier.height(24.dp))
                    }
                }
            }
        }

        // Floating back button (always on top)
        Box(
            modifier = Modifier
                .padding(12.dp)
                .size(40.dp)
                .background(Color.Black.copy(alpha = 0.55f), CircleShape)
                .clickable { onEvent(Event.BackClicked) },
            contentAlignment = Alignment.Center
        ) {
            Text("←", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
private fun InfoBadge(label: String, value: String) {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(2.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

private fun formatCurrency(amount: Long): String = when {
    amount <= 0L -> "N/A"
    amount >= 1_000_000_000L -> String.format("\$%.1fB", amount / 1_000_000_000f)
    amount >= 1_000_000L -> String.format("\$%.0fM", amount / 1_000_000f)
    amount >= 1_000L -> String.format("\$%.0fK", amount / 1_000f)
    else -> "\$$amount"
}
