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
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileSystemView

@Preview
@Composable
fun UpgradeHybridScreen() {
    val rootProjectPath = remember { mutableStateOf("") }
    val downloadHybridUrl = remember { mutableStateOf("") }
    val cachePath = remember { mutableStateOf("") }
    val isDownloading = remember { mutableStateOf(false) }
    val isClickable = remember { mutableStateOf(false) }
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "工程目录:"
            )
            Spacer(modifier = Modifier.width(10.dp))
            FileChooser(
                defaultFilePath = rootProjectPath.value,
                onFileChanged = { rootProjectPath.value = it },
            )
        }
        Spacer(modifier = Modifier.width(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "下载缓存目录:"
            )
            Spacer(modifier = Modifier.width(10.dp))
            FileChooser(
                defaultFilePath = cachePath.value,
                onFileChanged = { cachePath.value = it },
            )
        }
        Spacer(modifier = Modifier.size(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "资源下载地址:"
            )
            Spacer(modifier = Modifier.width(10.dp))
            DownloadResource(
                url = downloadHybridUrl.value,
                onDownLoadClick = {
                    println("click download")
                    isClickable.value = false
                    // TODO: 2022/7/5 下载流程待实现
                },
                onValueChanged = {
                    downloadHybridUrl.value = it
                    //当没开始下载时候可以开始执行下载
                    if (!isDownloading.value) {
                        isClickable.value = true
                    }
                },
                clickable = isClickable.value
            )
        }
    }

}

@Composable
fun DownloadResource(
    url: String?,
    onDownLoadClick: () -> Unit,
    onValueChanged: (String) -> Unit,
    hints: String? = null,
    clickable: Boolean = false,
) {
    Row {
        TextField(
            value = url ?: "",
            onValueChange = onValueChanged,
            placeholder = { Text(hints ?: "") }
        )
        Spacer(modifier = Modifier.width(10.dp))
        Button(enabled = clickable, onClick = onDownLoadClick) {
            Text("下载")
        }
    }
}

@Composable
fun FileChooser(
    modifier: Modifier? = null,
    defaultFilePath: String?,
    onFileChanged: (String) -> Unit,
    hints: String? = null,
    readOnly: Boolean = false
) {
    Row {
        TextField(
            value = defaultFilePath ?: "",
            readOnly = readOnly,
            onValueChange = onFileChanged,
            placeholder = { Text(hints ?: "") }
        )
        Spacer(modifier = Modifier.width(10.dp))
        Button(
            onClick = {
                onFileChanged(fileChooserDialog(hints))
            },
        ) {
            Text("...")
        }
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
    return if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
        fileChooser.selectedFile.toString()
    } else {
        ""
    }
}


//@Composable
//private fun FileDialog(
//    parent: Frame? = null,
//    onCloseRequest: (result: String?) -> Unit
//) = AwtWindow(
//    create = {
//        object : FileDialog(parent, "Choose a file", LOAD) {
//            override fun setVisible(value: Boolean) {
//                super.setVisible(value)
//                if (value) {
//                    onCloseRequest(file)
//                }
//            }
//        }
//    },
//    dispose = FileDialog::dispose
//)
//
//@Composable
//fun FileChooserDialog(
//    title: String,
//    mode: Int = JFileChooser.FILES_AND_DIRECTORIES,
//    currentPath: File? = null,
//    onResult: (result: File) -> Unit
//) {
//    val fileChooser = JFileChooser(FileSystemView.getFileSystemView())
////    fileChooser.currentDirectory = File(System.getProperty("user.dir"))
//    fileChooser.apply {
//        dialogTitle = title
//        fileSelectionMode = mode
//        isAcceptAllFileFilterUsed = true
//        selectedFile = null
//        currentDirectory = currentPath
//    }
//    if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
//        val file = fileChooser.selectedFile
//        println("choose file or folder is: $file")
//        onResult(file)
//    } else {
//        println("No Selection ")
//    }
//}