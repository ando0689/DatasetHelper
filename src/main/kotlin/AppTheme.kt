import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val teal = Color(0xff00796b)
val tealDark = Color(0xff004c40)
val tealExtraDark = Color(0xff001710)
val tealVividLight = Color(0xffdbffff)
val tealExtraLight = Color(0xffe6f2f1)

val red = Color(0xffbf360c)
val lightRed = Color(0xfffbe9e7)
val pailRed = Color(0xffCF6679)

val darkGray = Color(0xff212121)
val gray = Color(0xff757575)
val tealPail = Color(0xffb2dfdb)

val appLightColors = lightColors(
    primary = teal,
    primaryVariant = tealDark,
    onBackground = tealExtraDark,
    surface = Color.White,
    onSurface = tealExtraDark,
    secondary = tealExtraLight,
    secondaryVariant = tealVividLight,
    onSecondary = tealExtraDark,
    error = red
)

val appDarkColors = darkColors(
    surface = darkGray,
    background = darkGray,
    primary = tealPail,
    secondary = gray,
    onSecondary = Color.White,
    onSurface = Color.White,
    primaryVariant = Color.Black,
    secondaryVariant = Color.Black
)

private val LocalColors = staticCompositionLocalOf { LightColorPalette }

private val LightColorPalette = MyColors(
    material = appLightColors,
    errorBackground = lightRed
)

private val DarkColorPalette = MyColors(
    material = appDarkColors,
    errorBackground = pailRed
)

data class MyColors(
    val material: Colors,
    val errorBackground: Color
) {
    val primary: Color get() = material.primary
    val primaryVariant: Color get() = material.primaryVariant
    val secondary: Color get() = material.secondary
    val secondaryVariant: Color get() = material.secondaryVariant
    val background: Color get() = material.background
    val surface: Color get() = material.surface
    val error: Color get() = material.error
    val onPrimary: Color get() = material.onPrimary
    val onSecondary: Color get() = material.onSecondary
    val onBackground: Color get() = material.onBackground
    val onSurface: Color get() = material.onSurface
    val onError: Color get() = material.onError
    val isLight: Boolean get() = material.isLight
}

val MaterialTheme.myColors: MyColors
    @Composable
    @ReadOnlyComposable
    get() = LocalColors.current

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    CompositionLocalProvider(LocalColors provides colors) {
        MaterialTheme(
            colors = colors.material,
            content = content,
        )
    }
}