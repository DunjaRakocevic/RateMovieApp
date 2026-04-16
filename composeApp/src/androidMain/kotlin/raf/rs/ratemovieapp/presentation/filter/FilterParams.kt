package raf.rs.ratemovieapp.presentation.filter

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FilterParams(
    val query: String = "",
    val genreId: Int? = null,
    val minYear: Int? = null,
    val maxYear: Int? = null,
    val minRating: Float? = null
) : Parcelable {
    fun appliedCount(): Int {
        var c = 0
        if (query.isNotBlank()) c++
        if (genreId != null) c++
        if (minYear != null || maxYear != null) c++
        if (minRating != null) c++
        return c
    }
}