import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.draw.blur
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import data.Squad2Data
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

@Composable
fun MainScreen(composeWindow: ComposeWindow, screen: Screen.Main, onChangeScreen: (Screen) -> Unit){
    val modifier = if(screen.alert == null) Modifier else Modifier.blur(radius = 3.dp)
    MainScreen(modifier, composeWindow,
        onNewSquad2 = {
            val newScreen = screen.copy(alertData = AlertData.TextFieldAlertData(
                screen, "New Data Name", "Enter Name", onSubmit = {
                    onChangeScreen.invoke(Screen.SQuAD2(Squad2Data.new(it)))
                }
            ))

            onChangeScreen.invoke(newScreen)
        },
        onSquad2 = { data ->
            val newScreen = if(data == null){
                screen.copy(alertData = AlertData.TextAlertData(
                    screen,
                    "Bad file",
                    "Could not open this file. Please try another file."
                ))
            } else {
                Screen.SQuAD2(data)
            }

            onChangeScreen.invoke(newScreen)
        })
}


@Composable
private fun MainScreen(modifier: Modifier = Modifier, composeWindow: ComposeWindow, onNewSquad2: () -> Unit, onSquad2: (Squad2Data?) -> Unit) {
    val fileChooser = rememberFileChooser()

    Box(modifier = modifier.fillMaxSize().padding(horizontal = 48.dp), contentAlignment = Alignment.Center) {
        Row {
            Section(modifier = Modifier.weight(1f),
                title = "SQuAD2 Dataset",
                description = "Create or edit dataset for training BERT Q&A with SQuAD2 dataset.",
                onNewClick = onNewSquad2,
                onPickFileClick = {
                    val action = fileChooser.showOpenDialog(composeWindow)
                    if(action == JFileChooser.APPROVE_OPTION){
                        val data = Squad2Data.open(fileChooser.selectedFile)
                        onSquad2.invoke(data)
                    }
                })

            Section(modifier = Modifier.weight(1f),
                title = "Text Classification Dataset",
                description = "Create or edit dataset for training BERT for text classification or Intent detection.",
                onNewClick = {

                },
                onPickFileClick = {

                })
        }
    }
}

@Composable
private fun rememberFileChooser(): JFileChooser {
    return rememberSaveable {
        val fc = JFileChooser()
        val filter = FileNameExtensionFilter("JSON files", "json")
        fc.fileFilter = filter
        fc
    }
}


@Composable
private fun Section(modifier: Modifier = Modifier, title: String, description: String, onNewClick: () -> Unit, onPickFileClick: () -> Unit){
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = title, fontSize = 25.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = description, fontSize = 17.sp, fontWeight = FontWeight.Normal, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onNewClick){
            Text(text = "Create New File")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onPickFileClick){
            Text(text = "Open Existing File")
        }
    }
}