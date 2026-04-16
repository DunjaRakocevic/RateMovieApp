package raf.rs.ratemovieapp.presentation.filter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun FilterScreenStub(
    onApply: (FilterParams) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Filter (stub)")

        Button(onClick = { onApply(FilterParams(query = "test", minRating = 7.5f)) }) {
            Text("Apply (returns sample params)")
        }

        Button(onClick = onBack) {
            Text("Back (dismiss)")
        }
    }
}