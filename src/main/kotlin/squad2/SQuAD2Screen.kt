import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import squad2.CommonRowItem
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

    val onScreenDataChange: (CommonRowStateHolder?) -> Unit = {
        currentScreenData = it
    }

    val appBar: @Composable () -> Unit = { ScreenAppBar(currentScreenData, onScreenDataChange) }

    Scaffold(topBar = appBar) {
        Crossfade(targetState = currentScreenData){ state ->
            when(state){
                is Squad2DataState -> TopLevelDataScreen(state, topLevelDataScreenListState, onScreenDataChange)
                is DataState -> ParagraphScreen(state, paragraphScreenListState, onScreenDataChange)
                is PlainParagraphState -> QuestionsScreen(state, questionsScreenListState, onScreenDataChange)
                is GroupParagraphState -> QuestionGroupsScreen(state, questionsScreenListState, onScreenDataChange)
                is PlainQaState -> AnswersScreen(state, answersScreenListState, onScreenDataChange)
                is GroupQaState -> GroupedQuestionsScreen(state, answersScreenListState, onScreenDataChange)
                else -> onClose.invoke(null)
            }
        }
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
        onCreateNewItem = { ParagraphState.new(state) }){
        onScreenDataChange.invoke(it)
    }
}

@Composable
fun QuestionsScreen(state: PlainParagraphState, listState: LazyListState, onScreenDataChange: (CommonRowStateHolder?) -> Unit){
    CommonRowListScreen(
        modifier = Modifier.fillMaxSize(),
        listState = listState,
        header = {
            CommonRowListHeader(modifier = Modifier.padding(bottom = 8.dp), text = state.getSummery())
        },
        name = "Question",
        description = state.context.text,
        dataHolder = state.questions,
        onCreateNewItem = { PlainQaState(state) }){
        onScreenDataChange.invoke(it)
    }
}

@Composable
fun AnswersScreen(state: PlainQaState, listState: LazyListState, onScreenDataChange: (CommonRowStateHolder?) -> Unit){
    CommonRowListScreen(
        modifier = Modifier.fillMaxSize(),
        listState = listState,
        header = {
            CommonRowListHeader(text = state.context)
        },
        name = "Answer",
        description = state.question.text,
        dataHolder = state.answers,
        onCreateNewItem = { PlainAnswerState(state) }) { }
}


@Composable
fun QuestionGroupsScreen(state: GroupParagraphState, listState: LazyListState, onScreenDataChange: (CommonRowStateHolder?) -> Unit){
    CommonRowListScreen(
        modifier = Modifier.fillMaxSize(),
        listState = listState,
        header = {
            CommonRowListHeader(modifier = Modifier.padding(bottom = 8.dp), text = state.getSummery())
        },
        name = "Question Group",
        description = state.context.text,
        dataHolder = state.questionGroups,
        onCreateNewItem = { GroupQaState(state) }){
        onScreenDataChange.invoke(it)
    }
}

@Composable
fun GroupedQuestionsScreen(state: GroupQaState, listState: LazyListState, onScreenDataChange: (CommonRowStateHolder?) -> Unit){
    CommonRowListScreen(
        modifier = Modifier.fillMaxSize(),
        listState = listState,
        header = {
            Column {
                CommonRowListHeader(modifier = Modifier.padding(bottom = 8.dp), text = state.context)
                Text(modifier = Modifier.padding(start = 16.dp, top = 8.dp), text = "Answer", style = MaterialTheme.typography.h6, color = MaterialTheme.myColors.primaryVariant)
                CommonRowItem(stateHolder = state.answer, deleteAllowed = false)
                Text(modifier = Modifier.padding(start = 16.dp, top = 8.dp), text = "Questions", style = MaterialTheme.typography.h6, color = MaterialTheme.myColors.primaryVariant)
            }
        },
        name = "Question to the group",
        dataHolder = state.questions,
        onCreateNewItem = { GroupQuestionItemState(state) }) { }
}


@Composable
fun ScreenAppBar(state: CommonRowStateHolder?, onScreenDataChange: (CommonRowStateHolder?) -> Unit){
    when(state){
        is Squad2DataState -> AppBar(
            text = state.fileName,
            additionalContent = {
                Text(text = "Group Questions")
                Switch(

                    checked = state.groupQuestions, onCheckedChange = {
                        state.save()
                        val data = state.toSquad2Data()
                        val newState = Squad2DataState(data, !state.groupQuestions)
                        onScreenDataChange.invoke(newState)
                    })
            },
            onBack = { onScreenDataChange.invoke(null) },
            backIcon = Icons.Default.Close,
            onSave = { state.save() }
        )
        is DataState -> AppBar(
            text = "Paragraphs",
            onBack = { onScreenDataChange.invoke(state.parent) },
            onSave = { state.save() }
        )
        is PlainParagraphState -> AppBar(
            text = "Questions",
            onBack = { onScreenDataChange.invoke(state.parent) },
            onSave = { state.save() }
        )
        is GroupParagraphState -> AppBar(
            text = "Grouped Questions",
            onBack = { onScreenDataChange.invoke(state.parent) },
            onSave = { state.save() }
        )
        is PlainQaState -> AppBar(
            text = "Answers",
            onBack = {
                state.setNoteIfNoAnswers()
                onScreenDataChange.invoke(state.parent)
            },
            onSave = { state.save() }
        )
        is GroupQaState -> AppBar(
            text = "Questions Group",
            onBack = {
                state.setNote()
                onScreenDataChange.invoke(state.parent)
            },
            onSave = { state.save() }
        )
    }
}


@Composable
fun AppBar(modifier: Modifier = Modifier, text: String,
           onBack: (AlertData?) -> Unit,
           backIcon: ImageVector = Icons.Default.ArrowBack,
           onSave: () -> Unit,
           additionalContent: (@Composable () -> Unit)? = null){

    TopAppBar(modifier = modifier) {
        IconButton(onClick = {
            onBack.invoke(null)
        }, content = {
            Icon(backIcon, "Back")
        })

        Text(modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
            text = text,
            style = MaterialTheme.typography.h5,
            textAlign = TextAlign.Start)

        additionalContent?.invoke()

        IconButton(onClick = {
            onSave.invoke()
        }, content = {
            Icon(Icons.Filled.Done, "Save")
        })
    }
}