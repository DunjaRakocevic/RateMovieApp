package raf.rs.ratemovieapp.presentation.filter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import raf.rs.ratemovieapp.presentation.filter.FilterContract.Effect
import raf.rs.ratemovieapp.presentation.filter.FilterContract.Event
import raf.rs.ratemovieapp.presentation.filter.FilterContract.State
import raf.rs.ratemovieapp.presentation.filter.FilterContract.Status
import raf.rs.ratemovieapp.ui.theme.C_DarkBg
import raf.rs.ratemovieapp.ui.theme.C_DarkSurface
import raf.rs.ratemovieapp.ui.theme.C_DarkSurface2
import raf.rs.ratemovieapp.ui.theme.C_Divider
import raf.rs.ratemovieapp.ui.theme.C_OnDark
import raf.rs.ratemovieapp.ui.theme.C_OnDarkMuted
import raf.rs.ratemovieapp.ui.theme.C_Primary
import raf.rs.ratemovieapp.ui.theme.C_Secondary
import raf.rs.ratemovieapp.ui.theme.C_Yellow

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = C_Secondary,
        unfocusedBorderColor = C_Divider,
        cursorColor = C_Secondary,
        focusedTextColor = C_OnDark,
        unfocusedTextColor = C_OnDark,
        focusedPlaceholderColor = C_OnDarkMuted,
        unfocusedPlaceholderColor = C_OnDarkMuted
    )

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
        containerColor = C_DarkBg,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = C_DarkSurface,
                    titleContentColor = C_OnDark
                ),
                title = { Text("Filter Movies", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { onEvent(Event.Back) }) {
                        Text("←", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = C_OnDark)
                    }
                },
                actions = {
                    TextButton(onClick = { onEvent(Event.ClearAll) }) {
                        Text("Clear All", color = C_Secondary, fontWeight = FontWeight.SemiBold)
                    }
                }
            )
        }
    ) { paddingValues: PaddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            // SEARCH
            Text(
                "SEARCH",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            OutlinedTextField(
                value = s.draft.query,
                onValueChange = { onEvent(Event.QueryChanged(it)) },
                placeholder = { Text("Search by movie title...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(50.dp),
                colors = fieldColors
            )

            // GENRE
            Text(
                "GENRE",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            when (val st = s.status) {
                Status.LoadingGenres -> Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }

                is Status.Error -> Column {
                    Text(st.message)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { onEvent(Event.RetryGenres) }) { Text("Retry") }
                }

                Status.Ready -> {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        FilterChip(
                            selected = s.draft.genreId == null,
                            onClick = { onEvent(Event.GenreSelected(null)) },
                            label = { Text("All") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = C_Primary,
                                selectedLabelColor = C_OnDark,
                                containerColor = C_DarkSurface2,
                                labelColor = C_OnDarkMuted
                            )
                        )
                        s.genres.forEach { g ->
                            FilterChip(
                                selected = s.draft.genreId == g.id,
                                onClick = { onEvent(Event.GenreSelected(g.id)) },
                                label = { Text(g.name) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = C_Primary,
                                    selectedLabelColor = C_OnDark,
                                    containerColor = C_DarkSurface2,
                                    labelColor = C_OnDarkMuted
                                )
                            )
                        }
                    }
                }
            }

            // YEAR RANGE
            Text(
                "YEAR RANGE",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "From",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(
                        value = s.draft.minYear?.toString() ?: "",
                        onValueChange = { onEvent(Event.MinYearChanged(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = TextStyle(
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            fontSize = 16.sp
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(8.dp),
                        colors = fieldColors
                    )
                }
                Text("—", style = MaterialTheme.typography.titleMedium)
                Column(Modifier.weight(1f)) {
                    Text(
                        "To",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(
                        value = s.draft.maxYear?.toString() ?: "",
                        onValueChange = { onEvent(Event.MaxYearChanged(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = TextStyle(
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            fontSize = 16.sp
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(8.dp),
                        colors = fieldColors
                    )
                }
            }

            // MINIMUM RATING
            Text(
                "MINIMUM RATING",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Slider(
                    value = s.draft.minRating ?: 0f,
                    onValueChange = { onEvent(Event.MinRatingChanged(it)) },
                    valueRange = 0f..10f,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = C_Secondary,
                        activeTrackColor = C_Secondary,
                        inactiveTrackColor = C_Divider
                    )
                )
                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("★", color = C_Yellow)
                        Text(
                            String.format("%.1f", s.draft.minRating ?: 0f),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            // Apply button
            Button(
                onClick = { onEvent(Event.Apply) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = C_Primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "Apply Filters",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = C_OnDark
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
