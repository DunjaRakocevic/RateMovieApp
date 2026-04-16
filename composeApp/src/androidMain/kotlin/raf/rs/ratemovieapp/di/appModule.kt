package raf.rs.ratemovieapp.di

//import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import raf.rs.ratemovieapp.data.remote.MoviesApi
import raf.rs.ratemovieapp.data.remote.MoviesApiImpl
import raf.rs.ratemovieapp.data.repository.MoviesRepository
import raf.rs.ratemovieapp.data.repository.MoviesRepositoryImpl

val appModule = module {
    single<Json> {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
    }

    single<HttpClient> {
        val jsonConfig = get<Json>()
        HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(jsonConfig)
            }
        }
    }

    single<MoviesApi> { MoviesApiImpl(get<HttpClient>()) }
    single<MoviesRepository> { MoviesRepositoryImpl(get<MoviesApi>(), get<Json>()) }
//    single<MoviesApi> { get<Ktorfit>().create() }
}
