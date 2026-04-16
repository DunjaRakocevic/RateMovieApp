package raf.rs.ratemovieapp.presentation.movieslist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun MoviesListScreenStub(
    onOpenFilters: () -> Unit,
    onOpenDetails: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Movies List (stub)")

        Button(onClick = onOpenFilters) {
            Text("Open Filters")
        }

        Button(onClick = { onOpenDetails("tt1") }) {
            Text("Open Details (movieId=tt1)")
        }
    }
}
