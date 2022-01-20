// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.material.MaterialTheme
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.unit.dp

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import data.Squad2Data
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue


sealed class Screen(val alert: AlertData?) {
    data class Main(val alertData: AlertData? = null): Screen(alertData)
    data class SQuAD2(val data: Squad2Data, val alertData: AlertData? = null): Screen(alertData)
}

@Composable
fun rememberScreen(initialScreen: Screen = Screen.Main()): MutableState<Screen>{
    return rememberSaveable { mutableStateOf(initialScreen) }
}

@Composable
@Preview
fun App(composeWindow: ComposeWindow) {
    var currentScreen by rememberScreen()

    MaterialTheme {
        when(val screen = currentScreen){
            is Screen.Main -> MainScreen(composeWindow, screen){
                currentScreen = it
            }
            is Screen.SQuAD2 -> SQuAD2Screen(screen.data, onClose = {
                currentScreen = Screen.Main(alertData = it)
            })
        }

        currentScreen.alert?.let {
            Alert(it){
                currentScreen = it.parent
            }
        }
    }
}


fun main() = application {
    Window(
        title = "SQuAD2",
        state = rememberWindowState(width = 1024.dp, height = 640.dp),
        resizable = false,
        onCloseRequest = ::exitApplication) {
        App(this.window)
    }
}


//val file = File("${System.getProperty("user.home")}/Desktop", "testfile.txt")
//file.writeText("hello file")
//println(file)