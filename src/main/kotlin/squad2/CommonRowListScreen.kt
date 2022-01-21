@file:OptIn(ExperimentalMaterialApi::class)

package squad2

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.launch
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
class CommonRowState(value: String = "", editMode: Boolean = false) {
    var internalText by mutableStateOf(value)
    var text by mutableStateOf(value)
    var inEditMode by mutableStateOf(editMode)
    var errorText by mutableStateOf<String?>(null)
}


@Composable
fun <T : CommonRowStateHolder>CommonRowListScreen(modifier: Modifier = Modifier,
                                                  name: String,
                                                  description: String? = null,
                                                  dataHolder: MutableList<T>, onCreateNewItem: () -> T, onClick: (T) -> Unit){
    Column(modifier) {
        description?.let {
            Text(
                modifier = Modifier.padding(8.dp).fillMaxWidth().background(color = MaterialTheme.colors.secondary).padding(16.dp),
                text = it,
                style = MaterialTheme.typography.body2,
                textAlign = TextAlign.Justify)
        }

        CommonRowList(name, dataHolder, onCreateNewItem, onClick)
    }
}

@Composable
fun <T : CommonRowStateHolder> CommonRowList(name: String, dataHolder: MutableList<T>, onCreateNewItem: () -> T, onClick: (T) -> Unit){
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val data: MutableList<T> = remember { dataHolder.also { it.forEach { it.validate() } } }

    val onAdd: () -> Unit = {
        data.filter { it.commonRowState.inEditMode }.forEach { it.submit() }
        val newItem = onCreateNewItem().also { it.commonRowState.inEditMode = true }
        data.add(newItem)
        coroutineScope.launch {
            listState.scrollToItem(data.size - 1)
        }
    }

    LazyColumn(
        modifier = Modifier.simpleVerticalScrollbar(listState),
        state = listState) {
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
                onEditClick = { state.inEditMode = true },
                onDelete = { onDelete.invoke() },
                onItemClick = onClick
            )
        }
    }

}

@Composable
private fun RowItem(text: String, errorText: String? = null, onEditClick: (String) -> Unit, onDelete: () -> Unit, onItemClick: () -> Unit){
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
    val color = if(errorText == null) MaterialTheme.colors.secondary else Color(0xfffbe9e7)
    val borderColor = if(errorText == null) MaterialTheme.colors.primary else MaterialTheme.colors.error

    ErrorAwareContainer(errorText) {
        Surface(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            shape = MaterialTheme.shapes.small,
            color = color,
            border = BorderStroke(width = 3.dp, color = borderColor),
            elevation = 4.dp,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BasicTextField(
                    modifier = Modifier.weight(1f).padding(start = 8.dp).padding(vertical = 4.dp),
                    value = text,
                    textStyle = MaterialTheme.typography.body1,
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
        CommonRowListScreen(
            Modifier.fillMaxSize(),
            "Test 123",
            "VOLO was founded in 2006 in Armenia. VOLO is a software development company. ",
        mutableListOf(TestDataHolder("hello"), TestDataHolder(("how are you"))),
            { TestDataHolder("new test")},
            {}
        )
    }
}

class TestDataHolder(val name: String): CommonRowStateHolder{
    override val commonRowState: CommonRowState
        get() = CommonRowState(name)

}