package raf.rs.ratemovieapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = C_Secondary,
    onPrimary = Color(0xFF102A0D),

    secondary = C_Primary,
    onSecondary = Color.White,

    tertiary = C_Purple,
    onTertiary = Color.White,

    background = C_DarkBg,
    onBackground = C_OnDark,

    surface = C_DarkSurface,
    onSurface = C_OnDark,

    surfaceVariant = C_DarkSurface2,
    onSurfaceVariant = C_OnDarkMuted,

    error = C_Orange,
    onError = Color.Black
)

private val LightColors = lightColorScheme(
    primary = C_Primary,
    onPrimary = Color.White,
    secondary = C_Secondary,
    onSecondary = Color(0xFF102A0D),
    tertiary = C_Purple,
    onTertiary = Color.White,

    background = C_Cream,
    onBackground = Color(0xFF1B1B1B),

    surface = Color.White,
    onSurface = Color(0xFF1B1B1B),

    surfaceVariant = C_Lavender,
    onSurfaceVariant = Color(0xFF1B1B1B),

    error = C_Orange,
    onError = Color.White
)

/**
 * Po mocku, tamna tema je default.
 */
@Composable
fun AppTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
//package raf.rs.ratemovieapp.ui.theme
//
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.darkColorScheme
//import androidx.compose.material3.lightColorScheme
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.graphics.Color
//
//private val LightColors = lightColorScheme(
//    primary = GreenPrimary,
//    onPrimary = Color.White,
//
//    secondary = GreenSecondary,
//    onSecondary = Color(0xFF102A0D),
//
//    tertiary = Purple,
//    onTertiary = Color.White,
//
//    background = CreamBackground,
//    onBackground = Color(0xFF1B1B1B),
//
//    surface = Color.White,
//    onSurface = Color(0xFF1B1B1B),
//
//    surfaceVariant = Lavender,
//    onSurfaceVariant = Color(0xFF1B1B1B),
//
//    error = AccentOrange,
//    onError = Color.White
//)
//
//private val DarkColors = darkColorScheme(
//    primary = GreenSecondary,
//    onPrimary = Color(0xFF0B1B08),
//
//    secondary = GreenPrimary,
//    onSecondary = Color.White,
//
//    tertiary = AccentViolet,
//    onTertiary = Color.Black,
//
//    background = DarkIndigo,
//    onBackground = Color(0xFFF2E8CF),
//
//    surface = Color(0xFF1B1024),
//    onSurface = Color(0xFFF2E8CF),
//
//    surfaceVariant = DeepPurple,
//    onSurfaceVariant = Color(0xFFEEC7FC),
//
//    error = AccentOrange,
//    onError = Color.Black
//)
//
//@Composable
//fun AppTheme(
//    darkTheme: Boolean = false,
//    content: @Composable () -> Unit
//) {
//    MaterialTheme(
//        colorScheme = if (darkTheme) DarkColors else LightColors,
//        content = content
//    )
//}