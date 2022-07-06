package com.arno.tech.toolbox.view

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.arno.tech.toolbox.util.DownloadResult
import com.arno.tech.toolbox.util.FileUtils
import com.arno.tech.toolbox.util.downloadFile
import com.arno.tech.toolbox.viewmodel.UpgradeHybridViewModel
import kotlinx.coroutines.CoroutineScope
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
    val logString = viewModel.logString.collectAsState("")

    // TODO: 2022/7/6  待完善后续
    val isAutoUnZip = remember { mutableStateOf(false) }
    val isAutoReplace = remember { mutableStateOf(false) }
    val isAutoCommit = remember { mutableStateOf(false) }
    Column {
        RootPathSelector(rootProjectPath, viewModel)
        Spacer(modifier = Modifier.width(10.dp))

        CacheSelector(cachePath, viewModel)
        Spacer(modifier = Modifier.width(10.dp))

        Download(downloadHybridUrl, viewModel, scope, cachePath, isDownloading, isClickable)
        Spacer(modifier = Modifier.width(10.dp))
        //todo 尺寸控制不住?
        if (isDownloading.value) {
            DownloadIndicator(downloadProgress)
        }
        //region Auto Task Checkbox
        Text("下载完成后:")
        Row(horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {

            Checkbox(checked = isAutoUnZip.value, onCheckedChange = {
                isAutoUnZip.value = it
                viewModel.appendLogString("isAutoUnZip $it")
            })
            Text("自动解压缩:")

            Checkbox(checked = isAutoReplace.value, onCheckedChange = {
                isAutoReplace.value = it
                viewModel.appendLogString("isAutoReplace $it")
            })
            Text("自动替换资源:")

            Checkbox(checked = isAutoCommit.value, onCheckedChange = {
                isAutoCommit.value = it
                viewModel.appendLogString("isAutoCommit $it")
            })
            Text("自动生成提交:")
        }
        //endregion
        //region log console
        TextField(
            modifier = Modifier.fillMaxWidth().fillMaxHeight().padding(10.dp),
            value = logString.value,
            enabled = false,
            readOnly = true,
            onValueChange = {},
            shape = MaterialTheme.shapes.small.copy(bottomEnd = ZeroCornerSize, bottomStart = ZeroCornerSize),
        )
        //endregion

    }

}

@Composable
private fun DownloadIndicator(downloadProgress: State<Float>) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(5F).wrapContentHeight().align(Alignment.CenterVertically)) {
            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                text = "下载进度: ${downloadProgress.value * 100} % "
            )
            LinearProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                progress = downloadProgress.value
            )
        }
        Spacer(Modifier.width(10.dp))
//        CircularProgressIndicator(
//            modifier = Modifier.weight(1F).size(10.dp),
//            strokeWidth = 6.dp
//        )
    }
}

@Composable
private fun Download(
    downloadHybridUrl: State<String>,
    viewModel: UpgradeHybridViewModel,
    scope: CoroutineScope,
    cachePath: State<String>,
    isDownloading: State<Boolean>,
    isClickable: State<Boolean>,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(10.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(10.dp))
        DownloadResource(
            hints = "Hybrid下载地址",
            url = downloadHybridUrl.value,
            onDownLoadClick = {
                println("click download")
                val versionNumber = viewModel.validateDownloadUrl(url = downloadHybridUrl.value)
                if (versionNumber == null) {
                    println("not match versionNumber !!")
                    return@DownloadResource
                }
                viewModel.onDownloadClick()
                scope.launch {
                    val file = withContext(Dispatchers.IO) {
                        FileUtils.mkDir(cachePath.value + "/$versionNumber")
                        File(cachePath.value + "/$versionNumber/index.zip")
                    }
                    viewModel.client.downloadFile(downloadHybridUrl.value, file).collect {
                        when (it) {
                            is DownloadResult.Success -> {
                                viewModel.changeClickable(true)
                                viewModel.updateDownloadState(false)
                                println("download success .")
                            }
                            is DownloadResult.Error -> {
                                viewModel.changeClickable(true)
                                viewModel.updateDownloadState(false)
                                println("download error!!! $it")
                            }
                            is DownloadResult.Progress -> {
                                viewModel.updateDownloadProgress(it.progress)
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
}

@Composable
private fun CacheSelector(
    cachePath: State<String>,
    viewModel: UpgradeHybridViewModel
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(10.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        FileChooser(
            defaultFilePath = cachePath.value,
            onFileChanged = { viewModel.onCachePathChange(it) },
            hints = "下载资源目录"
        )
    }
}

@Composable
private fun RootPathSelector(
    rootProjectPath: State<String>,
    viewModel: UpgradeHybridViewModel
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(10.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(10.dp))
        FileChooser(
            defaultFilePath = rootProjectPath.value,
            onFileChanged = { viewModel.onProjectRootChange(it) },
            hints = "SinaNews工程目录"
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
            label = { Text(hints ?: "") }
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
            label = { Text(hints ?: "") }
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