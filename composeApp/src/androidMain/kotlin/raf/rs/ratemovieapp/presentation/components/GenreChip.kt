package raf.rs.ratemovieapp.presentation.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import raf.rs.ratemovieapp.ui.theme.C_DarkSurface2
import raf.rs.ratemovieapp.ui.theme.C_OnDarkMuted

@Composable
fun GenreChip(
    text: String,
    modifier: Modifier = Modifier
) {
    AssistChip(
        onClick = { /* no-op */ },
        label = {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = C_OnDarkMuted
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = C_DarkSurface2,
            labelColor = C_OnDarkMuted
        ),
        modifier = modifier.padding(end = 6.dp)
    )
}