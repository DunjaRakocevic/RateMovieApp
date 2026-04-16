package raf.rs.ratemovieapp.presentation.filter

import androidx.navigation.NavController
import raf.rs.ratemovieapp.navigation.NavResultKeys

fun NavController.returnFilters(params: FilterParams) {
    previousBackStackEntry?.savedStateHandle?.set(NavResultKeys.FILTER_PARAMS, params)
    popBackStack()
}