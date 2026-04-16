package raf.rs.ratemovieapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.koin.androidx.compose.koinViewModel
import raf.rs.ratemovieapp.presentation.filter.FilterScreen
import raf.rs.ratemovieapp.presentation.filter.FilterViewModel
import raf.rs.ratemovieapp.presentation.filter.returnFilters
import raf.rs.ratemovieapp.presentation.movieslist.MoviesListContract
import raf.rs.ratemovieapp.presentation.movieslist.MoviesListScreen
import raf.rs.ratemovieapp.presentation.movieslist.MoviesListViewModel
import raf.rs.ratemovieapp.presentation.movieslist.ObserveFilterResult
import raf.rs.ratemovieapp.presentation.moviedetails.MovieDetailsScreen
import raf.rs.ratemovieapp.presentation.moviedetails.MovieDetailsViewModel

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Destinations.MOVIES_LIST
    ) {
        composable(Destinations.MOVIES_LIST) {
            val vm: MoviesListViewModel = koinViewModel()

            ObserveFilterResult(navController) { params ->
                vm.onEvent(MoviesListContract.Event.FiltersApplied(params))
            }

            MoviesListScreen(
                state = vm.state,
                effects = vm.effects,
                onEvent = vm::onEvent,
                onNavigateToFilters = { navController.navigate(Destinations.FILTER) },
                onNavigateToDetails = { id -> navController.navigate("${Destinations.MOVIE_DETAILS}/$id") }
            )
        }

        composable(Destinations.FILTER) {
            val vm: FilterViewModel = koinViewModel()

            FilterScreen(
                state = vm.state,
                effects = vm.effects,
                onEvent = vm::onEvent,
                onApply = { params -> navController.returnFilters(params) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "${Destinations.MOVIE_DETAILS}/{${Destinations.ARG_MOVIE_ID}}",
            arguments = listOf(navArgument(Destinations.ARG_MOVIE_ID) { type = NavType.IntType })
        ) { entry ->
            val id = entry.arguments?.getInt(Destinations.ARG_MOVIE_ID) ?: return@composable
            val vm: MovieDetailsViewModel = koinViewModel()

            LaunchedEffect(id) { vm.setMovieId(id) }

            MovieDetailsScreen(
                state = vm.state,
                effects = vm.effects,
                onEvent = vm::onEvent,
                onBack = { navController.popBackStack() }
            )
        }
    }
}