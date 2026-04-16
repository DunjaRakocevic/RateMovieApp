package raf.rs.ratemovieapp.presentation.filter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import raf.rs.ratemovieapp.presentation.filter.FilterContract.Effect
import raf.rs.ratemovieapp.presentation.filter.FilterContract.Event
import raf.rs.ratemovieapp.presentation.filter.FilterContract.State
import raf.rs.ratemovieapp.presentation.filter.FilterContract.Status

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterScreen(
    state: StateFlow<State>,
    effects: Flow<Effect>,
    onEvent: (Event) -> Unit,
    initialDraft: FilterParams,
    onApply: (FilterParams) -> Unit,
    onBack: () -> Unit
) {
    val s by state.collectAsState()

    LaunchedEffect(initialDraft) { onEvent(Event.InitializeDraft(initialDraft)) }
    LaunchedEffect(Unit) { onEvent(Event.ScreenStarted) }

    LaunchedEffect(Unit) {
        effects.collect { eff ->
            when (eff) {
                is Effect.ReturnApplied -> onApply(eff.params)
                Effect.ReturnDismissed -> onBack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Filters") },
                navigationIcon = {
                    IconButton(onClick = { onEvent(Event.Back) }) {
                        Text("←")
                    }
                }
            )
        }
    ) { paddingValues: PaddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(12.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            when (val st = s.status) {
                Status.LoadingGenres -> Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }

                is Status.Error -> {
                    Text(st.message)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { onEvent(Event.RetryGenres) }) { Text("Retry") }
                }

                Status.Ready -> {
                    OutlinedTextField(
                        value = s.draft.query,
                        onValueChange = { onEvent(Event.QueryChanged(it)) },
                        label = { Text("Search") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(12.dp))
                    Text("Genre (single)", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = s.draft.genreId == null,
                            onClick = { onEvent(Event.GenreSelected(null)) },
                            label = { Text("All") }
                        )
                        s.genres.forEach { g ->
                            FilterChip(
                                selected = s.draft.genreId == g.id,
                                onClick = { onEvent(Event.GenreSelected(g.id)) },
                                label = { Text(g.name) }
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = s.draft.minYear?.toString() ?: "",
                            onValueChange = { onEvent(Event.MinYearChanged(it)) },
                            label = { Text("Min year") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = s.draft.maxYear?.toString() ?: "",
                            onValueChange = { onEvent(Event.MaxYearChanged(it)) },
                            label = { Text("Max year") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Minimum rating: ${String.format("%.1f", s.draft.minRating ?: 0f)}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Slider(
                        value = s.draft.minRating ?: 0f,
                        onValueChange = { onEvent(Event.MinRatingChanged(it)) },
                        valueRange = 0f..10f
                    )

                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { onEvent(Event.ClearAll) }) { Text("Clear All") }
                        Button(onClick = { onEvent(Event.Apply) }) { Text("Apply Filters") }
                    }
                }
            }
        }
    }
}
