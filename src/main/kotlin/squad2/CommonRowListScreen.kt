@file:OptIn(ExperimentalMaterialApi::class)

package squad2

import AppTheme
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.launch
import myColors
import simpleVerticalScrollbar

interface CommonRowStateHolder {
    val commonRowState: CommonRowState
    fun isValid(): Boolean = true
    fun errorText(): String = "Incorrect Value"

    fun submit() = commonRowState.apply {
        if(validate()) {
            internalText = text
            inEditMode = false
        }
    }

    fun validate(): Boolean = commonRowState.run {
        val isValid = isValid()
        errorText = if(isValid){ null } else { errorText() }
        isValid
    }

    fun cancel() = commonRowState.apply{
        text = internalText
        inEditMode = false
    }

}
class CommonRowState(value: String = "", note: String? = null, editMode: Boolean = false) {
    var internalText by mutableStateOf(value)
    var text by mutableStateOf(value)
    var inEditMode by mutableStateOf(editMode)
    var errorText by mutableStateOf<String?>(null)
    var noteText by mutableStateOf(note)
}

@Composable
fun CommonRowListHeader(modifier: Modifier = Modifier, text: String){
    var headerCollapsed by remember { mutableStateOf(true) }
    val headerMaxLines = if(headerCollapsed) 4 else Int.MAX_VALUE
    var textOverflow by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.background(color = MaterialTheme.colors.secondary.copy(alpha = 0.7f)).padding(8.dp).animateContentSize(),
        horizontalAlignment = Alignment.End) {
        SelectionContainer {
            Text(
                modifier = Modifier.padding(8.dp).fillMaxWidth(),
                text = text,
                style = MaterialTheme.typography.body2,
                maxLines = headerMaxLines,
                onTextLayout = { textLayoutResult ->
                    textOverflow = textLayoutResult.hasVisualOverflow
                },
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Justify)
        }

        if(textOverflow || !headerCollapsed){
            Icon(modifier = Modifier.clickable { headerCollapsed = !headerCollapsed },
                imageVector = if(headerCollapsed) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                contentDescription = if(headerCollapsed) "Expand" else "Collapse")
        }
    }
}

@Composable
fun <T : CommonRowStateHolder> CommonRowListScreen(modifier: Modifier = Modifier, header: (@Composable () -> Unit)? = null, name: String, description: String? = null, dataHolder: MutableList<T>, onCreateNewItem: () -> T, onClick: (T) -> Unit){
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val data: MutableList<T> = remember { dataHolder.onEach { it.validate() } }

    val onAdd: () -> Unit = {
        data.filter { it.commonRowState.inEditMode }.forEach { it.submit() }
        val newItem = onCreateNewItem().also { it.commonRowState.inEditMode = true }
        data.add(newItem)
        coroutineScope.launch {
            listState.scrollToItem(data.size - 1)
        }
    }

    LazyColumn(
        modifier = modifier.simpleVerticalScrollbar(listState),
        state = listState) {

        header?.let {
            item {
                it.invoke()
            }
        }

        description?.let {
            item {
                CommonRowListHeader(
                    modifier = Modifier.padding(8.dp),
                    text = it)
            }
        }

        items(data){ item ->
            CommonRowItem(
                stateHolder = item,
                onDelete = { data.remove(item) },
                onClick = { onClick.invoke(item) })
        }

        item {
            Row(modifier = Modifier.padding(16.dp)) {
                Spacer(modifier = Modifier.weight(1f))
                OutlinedButton(onClick = onAdd) {
                    Icon(Icons.Default.Add, "Add")
                    Text(text = "Add $name")
                }
            }
        }
    }
}

@Composable
fun CommonRowItem(modifier: Modifier = Modifier, stateHolder: CommonRowStateHolder, onDelete: () -> Unit, onClick: () -> Unit){
    val state = stateHolder.commonRowState

    Surface(modifier = modifier.animateContentSize()) {
        if(state.inEditMode){
            EditableRowItem(
                text = state.text,
                errorText = state.errorText,
                onTextChanged = { state.text = it },
                onCancel = { stateHolder.cancel() },
                onSubmit = { stateHolder.submit() },
            )
        } else {
            RowItem(
                text = state.text,
                errorText = state.errorText,
                noteText = state.noteText,
                onEditClick = { state.inEditMode = true },
                onDelete = { onDelete.invoke() },
                onItemClick = onClick
            )
        }
    }
}

@Composable
private fun RowItem(text: String, errorText: String? = null, noteText: String? = null, onEditClick: (String) -> Unit, onDelete: () -> Unit, onItemClick: () -> Unit){
    ErrorAwareContainer(errorText) {
        Surface(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            elevation = 4.dp,
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colors.secondary
        ) {
            Row(
                modifier = Modifier.clickable { onItemClick.invoke() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.weight(1f).padding(start = 8.dp).padding(vertical = 4.dp),
                    text = text,
                    maxLines = 2,
                    style = MaterialTheme.typography.body1,
                    overflow = TextOverflow.Ellipsis
                )

                noteText?.let {
                    Surface(
                        border = BorderStroke(width = 1.dp, color = MaterialTheme.colors.primaryVariant),
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colors.secondaryVariant.copy(alpha = 0.5f)
                        ) {
                        Text(
                            modifier = Modifier.padding(6.dp),
                            text = it,
                            style = MaterialTheme.typography.subtitle2)
                    }
                }

                ItemActionIcon(Icons.Default.Edit) {
                    onEditClick.invoke(text)
                }

                ItemActionIcon(Icons.Default.Delete) {
                    onDelete.invoke()
                }
            }
        }
    }
}

@Composable
private fun EditableRowItem(text: String, errorText: String? = null, onTextChanged: (String) -> Unit, onSubmit: () -> Unit, onCancel: () -> Unit){
    val color = if(errorText == null) MaterialTheme.colors.secondary else MaterialTheme.myColors.errorBackground
    val borderColor = if(errorText == null) MaterialTheme.colors.primary else MaterialTheme.colors.error

    ErrorAwareContainer(errorText) {
        Surface(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            shape = MaterialTheme.shapes.small,
            color = color,
            contentColor = MaterialTheme.myColors.onSurface,
            border = BorderStroke(width = 3.dp, color = borderColor),
            elevation = 4.dp,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BasicTextField(
                    modifier = Modifier.weight(1f).padding(start = 8.dp).padding(vertical = 4.dp),
                    value = text,
                    textStyle = MaterialTheme.typography.body1.copy(color = MaterialTheme.colors.onSecondary),
                    cursorBrush = SolidColor(MaterialTheme.colors.onSecondary),
                    onValueChange = onTextChanged
                )

                ItemActionIcon(Icons.Default.Close) {
                    onCancel.invoke()
                }

                ItemActionIcon(Icons.Default.Done) {
                    onSubmit.invoke()
                }
            }
        }
    }
}

@Composable
private fun ErrorAwareContainer(errorText: String?, content: @Composable () -> Unit){
    Column {
        content()
        errorText?.let {
            Text(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp), text = it, style = MaterialTheme.typography.caption.copy(color = MaterialTheme.colors.error))
        }
    }
}

@Composable
private fun ItemActionIcon(imageVector: ImageVector, onClick: () -> Unit){
    IconButton(
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        onClick = onClick,
        content = {
            Icon(imageVector, "ItemActionIcon")
        }
    )
}


fun main() = application {
    Window(
        title = "SQuAD2",
        state = rememberWindowState(width = 1024.dp, height = 640.dp),
        resizable = true,
        onCloseRequest = ::exitApplication) {
        AppTheme {
            Surface {
                CommonRowListScreen(
                    modifier = Modifier.fillMaxSize(),
                    name = "Test 123",
                    description = "VOLO was founded in 2006 in Armenia. VOLO is a software development company. ",
                    dataHolder = mutableListOf(TestDataHolder("hello"), TestDataHolder(("how are you"))),
                    onCreateNewItem = { TestDataHolder("new test")},
                    onClick = {}
                )
            }
        }

    }
}

class TestDataHolder(val name: String): CommonRowStateHolder{
    override val commonRowState: CommonRowState
        get() = CommonRowState(name)

}