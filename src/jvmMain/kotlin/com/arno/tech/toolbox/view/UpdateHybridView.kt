package com.arno.tech.toolbox.view

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
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
    val logScrollState = rememberScrollState(0)

    val isAutoUnZip = viewModel.isAutoUnZip.collectAsState(false)
    val isAutoReplace = viewModel.isAutoReplace.collectAsState(false)
    val isAutoCommit = viewModel.isAutoCommit.collectAsState(false)
    val isInAutoTask = viewModel.isInAutoTask.collectAsState(false)
    Column(modifier = Modifier.padding(10.dp)) {
        // 工程目录
        RootPathSelector(rootProjectPath, viewModel)
        Spacer(modifier = Modifier.size(10.dp))
        // 缓存路径
        CacheSelector(cachePath, viewModel)
        Spacer(modifier = Modifier.size(10.dp))

        //下载地址
        Download(downloadHybridUrl, viewModel, scope, cachePath, isDownloading, isClickable)
        Spacer(modifier = Modifier.size(10.dp))
        Row(horizontalArrangement = Arrangement.SpaceAround) {
            //自动任务选择
            AutoTaskSelector(isAutoUnZip, viewModel, isAutoReplace, isAutoCommit)
            //下载进度指示器
            if (isDownloading.value || isInAutoTask.value) {
                ProgressIndicator(downloadProgress)
            }
        }
        Divider(color = Color.Gray, modifier = Modifier.height(1.dp).fillMaxWidth())
        Spacer(modifier = Modifier.size(10.dp))
        //日志输出
        LogConsole(logScrollState, logString, scope)
    }

}

@Composable
private fun LogConsole(
    logScrollState: ScrollState,
    logString: State<String>,
    scope: CoroutineScope
) {
    Row {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(10.dp)
                .verticalScroll(logScrollState),
            text = logString.value,
            textAlign = TextAlign.Start,
            overflow = TextOverflow.Visible,
            color = Color.Gray
        )
        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterVertically)
                .fillMaxHeight(),
            adapter = rememberScrollbarAdapter(logScrollState)
        )
        // 自动滚动到队尾
        scope.launch {
            logScrollState.scrollTo(logScrollState.maxValue)
        }
    }
}

@Composable
private fun AutoTaskSelector(
    isAutoUnZip: State<Boolean>,
    viewModel: UpgradeHybridViewModel,
    isAutoReplace: State<Boolean>,
    isAutoCommit: State<Boolean>
) {
    Column {
        Text("下载完成后:")
        Row(horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            Text("自动解压缩:")
            Checkbox(checked = isAutoUnZip.value, onCheckedChange = {
                viewModel.onAutoUnZipClick(it)
            })
        }
        Row(horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            Text("自动替换资源:")
            Checkbox(checked = isAutoReplace.value, onCheckedChange = {
                viewModel.onAutoReplaceClick(it)
            })

        }
        Row(horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            Text("自动生成提交:")
            Checkbox(checked = isAutoCommit.value, onCheckedChange = {
                viewModel.onAutoCommitClick(it)
            })
        }
    }
}

@Composable
private fun ProgressIndicator(downloadProgress: State<Float>) {
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
        //todo 尺寸控制不住?
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
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(10.dp))
        DownloadResource(
            hints = "Hybrid下载地址",
            url = downloadHybridUrl.value,
            onDownLoadClick = {
                performClick(viewModel, downloadHybridUrl, scope, cachePath)
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

private fun performClick(
    viewModel: UpgradeHybridViewModel,
    downloadHybridUrl: State<String>,
    scope: CoroutineScope,
    cachePath: State<String>
) {
    println("click trigger")
    val versionNumber = viewModel.validateDownloadUrl(url = downloadHybridUrl.value)
    if (versionNumber == null) {
        println("not match versionNumber !!")
        return
    }
    viewModel.onTriggerClick()
    scope.launch {
        //1. step on download resource zip
        val file = withContext(Dispatchers.IO) {
            FileUtils.mkDir(cachePath.value + "/$versionNumber")
            File(cachePath.value + "/$versionNumber/index.zip")
        }
        viewModel.client.downloadFile(downloadHybridUrl.value, file).collect {
            viewModel.onDownloadStateChange(it)
        }

    }
}

@Composable
private fun CacheSelector(
    cachePath: State<String>,
    viewModel: UpgradeHybridViewModel
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
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
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
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
            Text("一键替换")
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
        Button(
            onClick = {
                onFileChanged(fileChooserDialog(hints))
            },
        ) {
            Text("...")
        }
        Spacer(modifier = Modifier.width(10.dp))
        TextField(
            value = defaultFilePath ?: "",
            readOnly = readOnly,
            onValueChange = onFileChanged,
            label = { Text(hints ?: "") }
        )


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