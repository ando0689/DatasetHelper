import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import data.Squad2Data

@Composable
fun SQuAD2Screen(data: Squad2Data, onClose: (AlertData?) -> Unit) {
    Scaffold(topBar = {
        AppBar(data, onClose, onSave = {

        })
    }) {

    }
}





@Composable
fun AppBar(data: Squad2Data, onClose: (AlertData?) -> Unit, onSave: () -> Unit){
    TopAppBar {
        IconButton(onClick = {
            onClose.invoke(null)
        }, content = {
            Icon(Icons.Default.Close, "Close")
        })

        Text(modifier = Modifier.weight(1f),
            text = data.name,
            style = MaterialTheme.typography.h5,
            textAlign = TextAlign.Center)

        IconButton(onClick = {
            onSave.invoke()
        }, content = {
            Icon(Icons.Filled.Done, "Done")
        })
    }
}