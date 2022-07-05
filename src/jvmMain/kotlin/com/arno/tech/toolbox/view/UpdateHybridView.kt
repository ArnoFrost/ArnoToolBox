package com.arno.tech.toolbox.view

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.AwtWindow
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileSystemView

@Preview
@Composable
fun UpgradeHybridScreen() {
    val rootProjectPath = remember { mutableStateOf("") }
    val downloadHybridUrl = remember { mutableStateOf("") }
    Column {
        Row(
            modifier = Modifier.fillMaxSize().padding(10.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "工程目录:"
            )
            Spacer(modifier = Modifier.width(10.dp))
            ChooseFilePath(
                defaultFilePath = rootProjectPath.value,
                onFileChanged = { rootProjectPath.value = it },
            )
        }

        Row(
            modifier = Modifier.fillMaxSize().padding(10.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "资源下载地址:"
            )
            Spacer(modifier = Modifier.width(10.dp))
            ChooseFilePath(
                defaultFilePath = rootProjectPath.value,
                onFileChanged = { rootProjectPath.value = it },
            )
        }
    }

}

@Composable
fun DownloadResource(url: String? = "") {

}

@Composable
fun ChooseFilePath(
    modifier: Modifier? = null,
    defaultFilePath: String?,
    onFileChanged: (String) -> Unit,
    hints: String? = null
) {
//    val isFileChooserOpen = remember { mutableStateOf(false) }
    Row {
        TextField(
            value = defaultFilePath ?: "",
            readOnly = true,//不能修改
            onValueChange = onFileChanged,
            placeholder = { Text("请输入SinaNews工程路径") }
        )
        Spacer(modifier = Modifier.width(10.dp))
        Button(
            onClick = {
//                isFileChooserOpen.value = true
                onFileChanged(fileChooserDialog("请选择SinaNews工程路径"))
            },
        ) {
            Text("...")
        }
//        if (isFileChooserOpen.value) {
//            FileDialog(
//                onCloseRequest = {
//                    isFileChooserOpen.value = false
//                    it?.let(onFileChanged)
//                    println("Result $it")
//                }
//            )
//        }
    }

}

@Composable
private fun FileDialog(
    parent: Frame? = null,
    onCloseRequest: (result: String?) -> Unit
) = AwtWindow(
    create = {
        object : FileDialog(parent, "Choose a file", LOAD) {
            override fun setVisible(value: Boolean) {
                super.setVisible(value)
                if (value) {
                    onCloseRequest(file)
                }
            }
        }
    },
    dispose = FileDialog::dispose
)

@Composable
fun FileChooserDialog(
    title: String,
    mode: Int = JFileChooser.FILES_AND_DIRECTORIES,
    currentPath: File? = null,
    onResult: (result: File) -> Unit
) {
    val fileChooser = JFileChooser(FileSystemView.getFileSystemView())
//    fileChooser.currentDirectory = File(System.getProperty("user.dir"))
    fileChooser.apply {
        dialogTitle = title
        fileSelectionMode = mode
        isAcceptAllFileFilterUsed = true
        selectedFile = null
        currentDirectory = currentPath
    }
    if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
        val file = fileChooser.selectedFile
        println("choose file or folder is: $file")
        onResult(file)
    } else {
        println("No Selection ")
    }
}

fun fileChooserDialog(
    title: String?
): String {
    val fileChooser = JFileChooser(FileSystemView.getFileSystemView())
    fileChooser.currentDirectory = File(System.getProperty("user.dir"))
    fileChooser.dialogTitle = title
    fileChooser.fileSelectionMode = JFileChooser.FILES_AND_DIRECTORIES
    fileChooser.isAcceptAllFileFilterUsed = true
    fileChooser.selectedFile = null
    fileChooser.currentDirectory = null
    val file = if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
        fileChooser.selectedFile.toString()
    } else {

        ""

    }

    return file

}