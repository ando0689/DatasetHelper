import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import data.*
import squad2.CommonRowListHeader
import squad2.CommonRowListScreen
import squad2.CommonRowStateHolder

@Composable
fun SQuAD2Screen(data: Squad2Data, onClose: (AlertData?) -> Unit) {
    var currentScreenData: CommonRowStateHolder? by remember { mutableStateOf(Squad2DataState(data)) }

    val topLevelDataScreenListState = rememberLazyListState()
    val paragraphScreenListState = rememberLazyListState()
    val questionsScreenListState = rememberLazyListState()
    val answersScreenListState = rememberLazyListState()

    val appBar: @Composable () -> Unit = { ScreenAppBar(currentScreenData) { parent ->
        currentScreenData = parent
    } }

    Scaffold(topBar = appBar) {
        Crossfade(targetState = currentScreenData){ state ->
            when(state){
                is Squad2DataState -> TopLevelDataScreen(state, topLevelDataScreenListState){
                    currentScreenData = it
                }
                is DataState -> ParagraphScreen(state, paragraphScreenListState) {
                    currentScreenData = it
                }
                is ParagraphState -> QuestionsScreen(state, questionsScreenListState) {
                    currentScreenData = it
                }
                is QaState -> AnswersScreen(state, answersScreenListState) {
                    currentScreenData = it
                }
                else -> onClose.invoke(null)
            }
        }
    }
}

@Composable
fun ScreenAppBar(state: CommonRowStateHolder?, onScreenDataChange: (CommonRowStateHolder?) -> Unit){
    when(state){
        is Squad2DataState -> AppBar(
            text = state.path.text,
            onBack = { onScreenDataChange.invoke(null) },
            backIcon = Icons.Default.Close,
            onSave = { state.save() }
        )
        is DataState -> AppBar(
            text = "Paragraphs",
            onBack = { onScreenDataChange.invoke(state.parent) },
            onSave = { state.save() }
        )
        is ParagraphState -> AppBar(
            text = "Questions",
            onBack = { onScreenDataChange.invoke(state.parent) },
            onSave = { state.save() }
        )
        is QaState -> AppBar(
            text = "Answers",
            onBack = {
                state.setNoteIfNoAnswers()
                onScreenDataChange.invoke(state.parent)
            },
            onSave = { state.save() }
        )
    }
}

@Composable
fun TopLevelDataScreen(state: Squad2DataState, listState: LazyListState, onScreenDataChange: (CommonRowStateHolder?) -> Unit){
    CommonRowListScreen(
        modifier = Modifier.fillMaxSize(),
        listState = listState,
        header = {
            CommonRowListHeader(modifier = Modifier.padding(bottom = 8.dp), text = state.getSummery())
        },
        name = "SQuAD2 Dataset",
        dataHolder = state.data,
        onCreateNewItem = { DataState(state) }){
        onScreenDataChange.invoke(it)
    }
}

@Composable
fun ParagraphScreen(state: DataState, listState: LazyListState, onScreenDataChange: (CommonRowStateHolder?) -> Unit){
    CommonRowListScreen(
        modifier = Modifier.fillMaxSize(),
        listState = listState,
        header = {
            CommonRowListHeader(modifier = Modifier.padding(bottom = 8.dp), text = state.getSummery())
        },
        name = "Paragraph",
        dataHolder = state.paragraphs,
        onCreateNewItem = { ParagraphState(state) }){
        onScreenDataChange.invoke(it)
    }
}

@Composable
fun QuestionsScreen(state: ParagraphState, listState: LazyListState, onScreenDataChange: (CommonRowStateHolder?) -> Unit){
    CommonRowListScreen(
        modifier = Modifier.fillMaxSize(),
        listState = listState,
        header = {
            CommonRowListHeader(modifier = Modifier.padding(bottom = 8.dp), text = state.getSummery())
        },
        name = "Question",
        description = state.context.text,
        dataHolder = state.qas,
        onCreateNewItem = { QaState(state) }){
        onScreenDataChange.invoke(it)
    }
}

@Composable
fun AnswersScreen(state: QaState, listState: LazyListState, onScreenDataChange: (CommonRowStateHolder?) -> Unit){
    CommonRowListScreen(
        modifier = Modifier.fillMaxSize(),
        listState = listState,
        header = {
            CommonRowListHeader(text = state.context)
        },
        name = "Answer",
        description = state.question.text,
        dataHolder = state.answers,
        onCreateNewItem = { AnswerState(state) }) {
    }
}


@Composable
fun AppBar(modifier: Modifier = Modifier, text: String, onBack: (AlertData?) -> Unit, backIcon: ImageVector = Icons.Default.ArrowBack, onSave: () -> Unit){
    TopAppBar(modifier = modifier) {
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