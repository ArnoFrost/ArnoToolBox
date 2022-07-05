package com.arno.tech.toolbox.view

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.*
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileSystemView
import kotlin.random.Random

@OptIn(DelicateCoroutinesApi::class)
@Preview
@Composable
fun UpgradeHybridScreen() {
    val rootProjectPath = remember { mutableStateOf("") }
    val downloadHybridUrl = remember { mutableStateOf("") }
    val cachePath = remember { mutableStateOf("") }
    val isDownloading = remember { mutableStateOf(false) }
    val isClickable = remember { mutableStateOf(false) }
    val downloadProgress = remember { mutableStateOf(0F) }
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
                    suspend fun fakeDownload() {
                        downloadProgress.value = 0F
                        while (downloadProgress.value < 1) {
                            delay(300L)
                            var tempAddValue = Random.nextFloat()
                            // 最多1
                            if (downloadProgress.value + tempAddValue > 1) {
                                tempAddValue = 1 - downloadProgress.value
                            }
                            downloadProgress.value += tempAddValue
                        }
                    }
                    GlobalScope.launch(Dispatchers.Default) {
                        fakeDownload()
                    }

                },
                onUrlChanged = {
                    downloadHybridUrl.value = it
                    //当没开始下载时候可以开始执行下载
                    if (!isDownloading.value) {
                        isClickable.value = true
                    }
                },
                clickable = isClickable.value
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(modifier = Modifier.align(Alignment.CenterHorizontally), text = "下载进度: ${downloadProgress.value * 100} % ")
        LinearProgressIndicator(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            progress = downloadProgress.value
        )

    }

}

@Composable
fun DownloadResource(
    url: String?,
    onDownLoadClick: () -> Unit,
    onUrlChanged: (String) -> Unit,
    hints: String? = null,
    clickable: Boolean = false,
) {
    Row {
        TextField(
            value = url ?: "",
            onValueChange = onUrlChanged,
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