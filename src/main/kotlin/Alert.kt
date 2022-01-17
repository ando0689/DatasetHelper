import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class AlertData(
    val parent: Screen, val title: String, val message: String
)

@Composable
fun Alert(data: AlertData, onDismiss: () -> Unit){
    Surface(color = Color(0x44000000)) {
        Box(modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center){

            Surface(elevation = 16.dp, shape = MaterialTheme.shapes.small) {
                Column(modifier = Modifier
                    .padding(horizontal = 48.dp, vertical = 24.dp).widthIn(max = 240.dp),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = data.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center)

                    Spacer(modifier = Modifier.padding(8.dp))

                    Text(text = data.message,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center)

                    Spacer(modifier = Modifier.padding(8.dp))

                    Button(onClick = onDismiss){
                        Text("OK")
                    }
                }
            }
        }
    }
}