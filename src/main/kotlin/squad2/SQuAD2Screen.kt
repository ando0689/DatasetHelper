import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import data.*
import squad2.CommonRowListScreen
import squad2.CommonRowStateHolder

@Composable
fun SQuAD2Screen(data: Squad2Data, onClose: (AlertData?) -> Unit) {
    var currentScreenData: CommonRowStateHolder? by remember { mutableStateOf(Squad2DataState(data)) }

    when(val state = currentScreenData){
        is Squad2DataState -> TopLevelDataScreen(state){
            currentScreenData = it
        }
        is DataState -> ParagraphScreen(state) {
            currentScreenData = it
        }
        is ParagraphState -> QuestionsScreen(state) {
            currentScreenData = it
        }
        is QaState -> AnsweresScreen(state) {
            currentScreenData = it
        }
        else -> onClose.invoke(null)
    }
}



@Composable
fun TopLevelDataScreen(state: Squad2DataState, onScreenDataChange: (CommonRowStateHolder?) -> Unit){
    val appBar: @Composable () -> Unit = {
        AppBar(
            text = state.name.text,
            onBack = { onScreenDataChange.invoke(null) },
            backIcon = Icons.Default.Close,
            onSave = {
                //TODO
            }
        )
    }

    Scaffold(topBar = appBar) {
        CommonRowListScreen(
            modifier = Modifier.fillMaxSize(),
            name = "SQuAD2 Dataset",
            description = "Add SQuAD v2.0 datasets. All of them will be saved in a single file. Usually we will need only one dataset per file",
            dataHolder = state.data,
            onCreateNewItem = { DataState(state) }){
            onScreenDataChange.invoke(it)
        }
    }
}

@Composable
fun ParagraphScreen(state: DataState, onScreenDataChange: (CommonRowStateHolder?) -> Unit){
    val appBar: @Composable () -> Unit = {
        AppBar(
            text = "Paragraphs",
            onBack = { onScreenDataChange.invoke(state.parent) },
            onSave = {
                //TODO
            }
        )
    }

    Scaffold(topBar = appBar) {
        CommonRowListScreen(
            modifier = Modifier.fillMaxSize(),
            name = "Paragraph",
            description = "Add Paragraphs to your Dataset. Each Paragraph will have its own set of Question/Answers ",
            dataHolder = state.paragraphs,
            onCreateNewItem = { ParagraphState(state) }){
            onScreenDataChange.invoke(it)
        }
    }

}

@Composable
fun QuestionsScreen(state: ParagraphState, onScreenDataChange: (CommonRowStateHolder?) -> Unit){
    val appBar: @Composable () -> Unit = {
        AppBar(
            text = "Qurstions",
            onBack = { onScreenDataChange.invoke(state.parent) },
            onSave = {
                //TODO
            }
        )
    }

    Scaffold(topBar = appBar) {
        CommonRowListScreen(
            modifier = Modifier.fillMaxSize(),
            name = "Question",
            description = state.context.text,
            dataHolder = state.qas,
            onCreateNewItem = { QaState(state) }){
            onScreenDataChange.invoke(it)
        }
    }
}

@Composable
fun AnsweresScreen(state: QaState, onScreenDataChange: (CommonRowStateHolder?) -> Unit){
    val appBar: @Composable () -> Unit = {
        AppBar(
            text = "Answers",
            onBack = { onScreenDataChange.invoke(state.parent) },
            onSave = {
                //TODO
            }
        )
    }

    Scaffold(topBar = appBar) {
        CommonRowListScreen(
            modifier = Modifier.fillMaxSize(),
            name = "Answer",
            description = state.question.text,
            dataHolder = state.answers,
            onCreateNewItem = { AnswerState(state) }) {
        }
    }
}


@Composable
fun AppBar(text: String, onBack: (AlertData?) -> Unit, backIcon: ImageVector = Icons.Default.ArrowBack, onSave: () -> Unit){
    TopAppBar {
        IconButton(onClick = {
            onBack.invoke(null)
        }, content = {
            Icon(backIcon, "Back")
        })

        Text(modifier = Modifier.weight(1f),
            text = text,
            style = MaterialTheme.typography.h5,
            textAlign = TextAlign.Center)

        IconButton(onClick = {
            onSave.invoke()
        }, content = {
            Icon(Icons.Filled.Done, "Save")
        })
    }
}