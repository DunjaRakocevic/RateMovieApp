package raf.rs.ratemovieapp.di

import org.koin.dsl.module
import raf.rs.ratemovieapp.data.repository.MoviesRepository
import raf.rs.ratemovieapp.data.repository.MoviesRepositoryStub

val appModule = module {
    single<MoviesRepository> { MoviesRepositoryStub() }
}