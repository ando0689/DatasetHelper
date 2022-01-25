import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue

sealed class AlertData {
    open lateinit var parent: Screen

    data class TextAlertData(
        override var parent: Screen, val title: String, val message: String
    ): AlertData()

    data class TextFieldAlertData(
        override var parent: Screen, val title: String, val hint: String = "", val onSubmit: (String) -> Unit,
    ): AlertData()
}

@Composable
fun Alert(data: AlertData?, onDismiss: () -> Unit){
    when(data) {
        is AlertData.TextAlertData -> TextAlert(data, onDismiss)
        is AlertData.TextFieldAlertData -> TextFieldAlert(data, onDismiss)
    }
}

@Composable
fun TextAlert(data: AlertData.TextAlertData, onDismiss: () -> Unit){
    BaseAlert(data.title) {
        Text(text = data.message,
            fontSize = 14.sp,
            textAlign = TextAlign.Center)

        Spacer(modifier = Modifier.padding(8.dp))

        Button(onClick = onDismiss){
            Text("OK")
        }
    }
}



@Composable
fun TextFieldAlert(data: AlertData.TextFieldAlertData, onDismiss: () -> Unit){
    var value by remember { mutableStateOf("") }

    BaseAlert(data.title) {
        OutlinedTextField(
            value = value,
            singleLine = true,
            onValueChange = {
                value = it
            }, label = {
                Text(text = data.hint)
            })

        Spacer(modifier = Modifier.padding(8.dp))

        Row {
            Button(onClick = onDismiss){
                Text("Cancel")
            }
            Spacer(modifier = Modifier.padding(8.dp))
            Button(onClick = {
                if(value.isNotBlank()) {
                    onDismiss.invoke()
                    data.onSubmit.invoke(value)
                }
            }){
                Text("OK")
            }
        }
    }
}

@Composable
fun BaseAlert(title: String, content: @Composable () -> Unit){
    Surface(color = Color(0x44000000)) {
        Box(modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center){
            Surface(elevation = 16.dp, shape = MaterialTheme.shapes.small) {
                Column(modifier = Modifier
                    .padding(horizontal = 48.dp, vertical = 24.dp)
                    .widthIn(max = 240.dp),
                    horizontalAlignment = Alignment.CenterHorizontally) {

                    Text(text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center)

                    Spacer(modifier = Modifier.padding(8.dp))

                    content()
                }
            }
        }
    }
}