package raf.rs.ratemovieapp.presentation.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import raf.rs.ratemovieapp.ui.theme.C_OnDarkMuted
import raf.rs.ratemovieapp.ui.theme.C_Yellow

@Composable
fun RatingRow(
    rating: Float,
    votes: Int
) {
    Row {
        Text("★", color = C_Yellow, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.width(6.dp))
        Text(
            text = String.format("%.1f", rating),
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = "${formatVotes(votes)} votes",
            style = MaterialTheme.typography.bodySmall,
            color = C_OnDarkMuted
        )
    }
}

private fun formatVotes(votes: Int): String {
    return when {
        votes >= 1_000_000 -> String.format("%.1fM", votes / 1_000_000f)
        votes >= 1_000 -> String.format("%.0fK", votes / 1_000f)
        else -> votes.toString()
    }
}