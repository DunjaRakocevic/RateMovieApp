package raf.rs.ratemovieapp.presentation.movieslist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import raf.rs.ratemovieapp.navigation.NavResultKeys
import raf.rs.ratemovieapp.presentation.filter.FilterParams

@Composable
fun ObserveFilterResult(
    navController: NavController,
    onFiltersApplied: (FilterParams) -> Unit
) {
    val entry = navController.currentBackStackEntry ?: return
    val handle = entry.savedStateHandle

    LaunchedEffect(handle) {
        handle.getStateFlow<FilterParams?>(NavResultKeys.FILTER_PARAMS, null)
            .collect { params ->
                if (params != null) {
                    onFiltersApplied(params)
                    handle[NavResultKeys.FILTER_PARAMS] = null // consume
                }
            }
    }
}