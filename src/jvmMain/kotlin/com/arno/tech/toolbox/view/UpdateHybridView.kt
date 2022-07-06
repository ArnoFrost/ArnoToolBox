package com.arno.tech.toolbox.view

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.arno.tech.toolbox.util.DownloadResult
import com.arno.tech.toolbox.util.downloadFile
import com.arno.tech.toolbox.viewmodel.UpgradeHybridViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileSystemView

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        UpgradeHybridApp()
    }
}

@Composable
@Preview
fun UpgradeHybridApp() {
    val viewModel = UpgradeHybridViewModel()

    MaterialTheme {
        UpgradeHybridScreen(viewModel)
    }
}

@OptIn(DelicateCoroutinesApi::class)
@Preview
@Composable
fun UpgradeHybridScreen(viewModel: UpgradeHybridViewModel) {
    val scope = rememberCoroutineScope()

    val rootProjectPath = viewModel.rootProjectPath.collectAsState("")
    val downloadHybridUrl = viewModel.downloadHybridUrl.collectAsState("")
    val cachePath = viewModel.cachePath.collectAsState("")
    val isDownloading = viewModel.isDownloading.collectAsState(false)
    val isClickable = viewModel.isClickable.collectAsState(false)
    val downloadProgress = viewModel.downloadProgress.collectAsState(0F)
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
                onFileChanged = { viewModel.onProjectRootChange(it) },
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
                onFileChanged = { viewModel.onCachePathChange(it) },
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
                    viewModel.changeClickable(false)
                    scope.launch {
                        val file = withContext(Dispatchers.IO) {
                            File(cachePath.value+"/123")
                        }
                        viewModel.client.downloadFile(downloadHybridUrl.value, file).collect {
                            when (it) {
                                is DownloadResult.Success -> {
                                    viewModel.changeClickable(true)
                                    viewModel.updateDownloadState(false)
                                }
                                is DownloadResult.Error -> {
                                    viewModel.changeClickable(true)
                                    viewModel.updateDownloadState(false)
                                }
                                is DownloadResult.Progress -> {
                                    viewModel.updateDownloadProgress(it.progress.toFloat())
                                }
                            }
                        }
                    }

                },
                onUrlChanged = {
                    viewModel.onDownloadUrlChange(it)
                    //当没开始下载时候可以开始执行下载
                    if (!isDownloading.value) {
                        viewModel.changeClickable(true)
                    }
                },
                clickable = isClickable.value
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(modifier = Modifier.align(Alignment.CenterHorizontally), text = "下载进度: ${downloadProgress.value} % ")
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