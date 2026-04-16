package raf.rs.ratemovieapp.di

import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module
import raf.rs.ratemovieapp.presentation.filter.FilterViewModel
import raf.rs.ratemovieapp.presentation.moviedetails.MovieDetailsViewModel
import raf.rs.ratemovieapp.presentation.movieslist.MoviesListViewModel

val androidModule = module {
    viewModelOf(::MoviesListViewModel)
    viewModelOf(::FilterViewModel)
    viewModelOf(::MovieDetailsViewModel)
}
