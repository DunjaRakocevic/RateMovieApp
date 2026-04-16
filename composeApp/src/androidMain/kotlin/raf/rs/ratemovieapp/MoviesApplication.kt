package raf.rs.ratemovieapp

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import raf.rs.ratemovieapp.di.androidModule
import raf.rs.ratemovieapp.di.appModule

class MoviesApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MoviesApplication)
            modules(appModule, androidModule)
        }
    }
}