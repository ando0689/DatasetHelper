@file:OptIn(ExperimentalMaterialApi::class)

package squad2

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.launch
import simpleVerticalScrollbar

interface CommonRowStateHolder {
    val commonRowState: CommonRowState
}

@Composable
fun <T : CommonRowStateHolder>CommonRowListScreen(modifier: Modifier = Modifier, name: String, description: String? = null, dataHolder: MutableList<T>, onCreateNewItem: () -> T, onClick: (T) -> Unit){
    Column(modifier) {
        description?.let {
            Text(text = it, style = MaterialTheme.typography.caption)
            Spacer(Modifier.height(8.dp))
        }

        CommonRowList(name, dataHolder, onCreateNewItem, onClick)
    }
}

@Composable
fun <T : CommonRowStateHolder> CommonRowList(name: String, dataHolder: MutableList<T>, onCreateNewItem: () -> T, onClick: (T) -> Unit){
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val data: MutableList<T> = dataHolder

    val onAdd: () -> Unit = {
        data.filter { it.commonRowState.inEditMode }.forEach { it.commonRowState.submit() }
        data.add(onCreateNewItem())
        coroutineScope.launch {
            listState.scrollToItem(data.size - 1)
        }
    }

    LazyColumn(
        modifier = Modifier.simpleVerticalScrollbar(listState),
        state = listState) {
        items(data){ item ->
            CommonRowItem(item.commonRowState,
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
fun CommonRowItem(state: CommonRowState, onDelete: () -> Unit, onClick: () -> Unit){
    if(state.inEditMode){
        EditableRowItem(
            text = state.text,
            onTextChanged = {
                state.text = it
            },
            onCancel = {
                state.cancel()
            },
            onSubmit = {
                state.submit()
            })
    } else {
        RowItem(text = state.text, onEditClick = {
            state.inEditMode = true
        }, onDelete = {
            onDelete.invoke()
        }, onItemClick = onClick)
    }
}

@Composable
private fun RowItem(text: String, onEditClick: (String) -> Unit, onDelete: () -> Unit, onItemClick: () -> Unit){
    Surface(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        elevation = 4.dp,
        shape = MaterialTheme.shapes.small,
        color = Color(0xffcfd8dc)) {
        Row(
            modifier = Modifier.clickable { onItemClick.invoke() },
            verticalAlignment = Alignment.CenterVertically) {
            Text(modifier = Modifier.weight(1f).padding(start = 8.dp).padding(vertical = 4.dp),
                text = text,
                style = MaterialTheme.typography.body1)

            ItemActionIcon(Icons.Default.Edit) {
                onEditClick.invoke(text)
            }

            ItemActionIcon(Icons.Default.Delete) {
                onDelete.invoke()
            }
        }
    }
}

@Composable
private fun EditableRowItem(text: String, onTextChanged: (String) -> Unit, onSubmit: () -> Unit, onCancel: () -> Unit){
    Surface(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        elevation = 4.dp,
        shape = MaterialTheme.shapes.small,
        color = Color(0xffeceff1),
        border = BorderStroke(width = 3.dp, color = Color(0xffcfd8dc))
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BasicTextField(modifier = Modifier.weight(1f).padding(start = 8.dp).padding(vertical = 4.dp),
                value = text,
                textStyle = MaterialTheme.typography.body1,
                onValueChange = onTextChanged)

            ItemActionIcon(Icons.Default.Close) {
                onCancel.invoke()
            }

            ItemActionIcon(Icons.Default.Done) {
                onSubmit.invoke()
            }
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

class CommonRowState(value: String = "", editMode: Boolean = false) {
    private var internalText by mutableStateOf(value)
    var text by mutableStateOf(value)
    var inEditMode by mutableStateOf(editMode)

    fun submit(){
        internalText = text
        inEditMode = false
    }

    fun cancel(){
        text = internalText
        inEditMode = false
    }
}

fun main() = application {
    Window(
        title = "SQuAD2",
        state = rememberWindowState(width = 1024.dp, height = 640.dp),
        resizable = true,
        onCloseRequest = ::exitApplication) {
        //TODO
    }
}