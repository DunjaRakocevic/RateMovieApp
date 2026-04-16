package raf.rs.ratemovieapp.di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import raf.rs.ratemovieapp.presentation.filter.FilterViewModel
import raf.rs.ratemovieapp.presentation.moviedetails.MovieDetailsViewModel
import raf.rs.ratemovieapp.presentation.movieslist.MoviesListViewModel

val androidModule = module {
    viewModel { MoviesListViewModel(get(), get()) }
    viewModel { FilterViewModel(get(), get()) }
    viewModel { MovieDetailsViewModel(get(), get()) }
}
