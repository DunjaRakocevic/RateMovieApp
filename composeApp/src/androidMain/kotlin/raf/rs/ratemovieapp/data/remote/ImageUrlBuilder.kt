package raf.rs.ratemovieapp.data.remote

data class ImageConfig(
    val baseUrl: String,
    val posterSize: String,
    val backdropSize: String,
)



class ImageUrlBuilder(private val config: ImageConfig) {
    fun poster(filePath: String?): String? = filePath?.let { "${config.baseUrl}${config.posterSize}$it" }
    fun backdrop(filePath: String?): String? = filePath?.let { "${config.baseUrl}${config.backdropSize}$it" }


}