// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import com.arno.tech.toolbox.view.UpgradeHybridScreen
import com.arno.tech.toolbox.viewmodel.UpgradeHybridViewModel

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        state = WindowState(
            position = WindowPosition.Aligned(Alignment.Center),
            size = DpSize(600.dp, 900.dp)
        ),
        title = "Hybrid资源替换工具"
    ) {
        UpgradeHybridApp()
    }
}

@Composable
@Preview
fun UpgradeHybridApp() {
    val viewModel = UpgradeHybridViewModel()
    viewModel.restoreUserSettings()
    MaterialTheme {
        UpgradeHybridScreen(viewModel)
    }
}
