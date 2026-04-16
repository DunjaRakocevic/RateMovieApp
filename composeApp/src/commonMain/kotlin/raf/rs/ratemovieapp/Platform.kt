package raf.rs.ratemovieapp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform