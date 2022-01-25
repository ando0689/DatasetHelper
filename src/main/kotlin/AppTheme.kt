import androidx.compose.material.MaterialTheme
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


val teal = Color(0xff00796b)
val tealDark = Color(0xff004c40)
val tealExtraDark = Color(0xff001710)
val tealVividLight = Color(0xffdbffff)
val tealExtraLight = Color(0xffe6f2f1)

val red = Color(0xffbf360c)

val appColors = lightColors(
    primary = teal,
    primaryVariant = tealDark,
    onBackground = tealExtraDark,
    onSurface = tealExtraDark,
    secondary = tealExtraLight,
    secondaryVariant = tealVividLight,
    onSecondary = tealExtraDark,
    error = red
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(colors = appColors) {
        content()
    }
}