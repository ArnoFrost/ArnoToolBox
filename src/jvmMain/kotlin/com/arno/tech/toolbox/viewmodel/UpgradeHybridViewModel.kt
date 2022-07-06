package com.arno.tech.toolbox.viewmodel

import com.arno.tech.toolbox.model.UpgradeResult
import com.arno.tech.toolbox.util.DownloadResult
import com.arno.tech.toolbox.util.FileUtils
import com.arno.tech.toolbox.util.downloadFile
import com.arno.tech.toolbox.util.unzipFile
import io.ktor.client.*
import io.ktor.client.engine.java.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * 更新Hb模板 viewmodel
 *
 */
class UpgradeHybridViewModel : ViewController() {
    companion object {
        // hybrid 模板下载规则匹配
        //http://mjs.sinaimg.cn//wap/project/snal_v2/7.3.63/index/index.php
        //http://mjs.sinaimg.cn//wap/project/snal_v2/7.3.63-test/index/index.php
        private val HYBRID_PATTERN_REG = Regex("(\\d.\\d.\\d+)(-\\w+)?(?=/)")
        private val PROJECT_ASSERT_SUFFIX = "/SinaNews/src/main/assets/article_v2"
        private val COMMIT_DEFAULT_TEMPLATE = "升级正文 Hb 模板 "
    }

    private val _client = HttpClient(Java)
    val client: HttpClient
        get() = _client

    private val _rootProjectPath = MutableStateFlow("/Users/xuxin14/Desktop/SinaProjects/SinaNewsArticle")
    val rootProjectPath: Flow<String>
        get() = _rootProjectPath.asStateFlow()
    private val _downloadHybridUrl =
        MutableStateFlow("http://mjs.sinaimg.cn//wap/project/snal_v2/7.3.63/index/index.php")
    val downloadHybridUrl: Flow<String>
        get() = _downloadHybridUrl.asStateFlow()
    private val _cachePath = MutableStateFlow("/Users/xuxin14/Desktop/Temp")
    val cachePath: Flow<String>
        get() = _cachePath.asStateFlow()

    private val _isDownloading = MutableStateFlow(false)
    val isDownloading: Flow<Boolean>
        get() = _isDownloading.asStateFlow()

    private val _isClickable = MutableStateFlow(false)
    val isClickable: Flow<Boolean>
        get() = _isClickable.asStateFlow()
    private val _downloadProgress = MutableStateFlow(0F)
    val downloadProgress: Flow<Float>
        get() = _downloadProgress.asStateFlow()

    private val _logString = MutableStateFlow("")
    val logString: Flow<String>
        get() = _logString.asStateFlow()
    private val _logError = MutableStateFlow(false)
    val logError: Flow<Boolean>
        get() = _logError.asStateFlow()

    private val _isAutoUnZip = MutableStateFlow(false)
    val isAutoUnZip: Flow<Boolean>
        get() = _isAutoUnZip.asStateFlow()
    private val _isAutoReplace = MutableStateFlow(false)
    val isAutoReplace: Flow<Boolean>
        get() = _isAutoReplace.asStateFlow()
    private val _isAutoCommit = MutableStateFlow(false)
    val isAutoCommit: Flow<Boolean>
        get() = _isAutoCommit.asStateFlow()
    private val _isInAutoTask = MutableStateFlow(false)
    val isInAutoTask: Flow<Boolean>
        get() = _isInAutoTask.asStateFlow()

    fun onProjectRootChange(path: String?) {
        _rootProjectPath.update { path ?: "" }
        isDownloadClickable(rootPath = _rootProjectPath.value, _cachePath.value, _downloadHybridUrl.value)
    }

    fun onDownloadUrlChange(url: String?) {
        _downloadHybridUrl.update { url ?: "" }
        isDownloadClickable(rootPath = _rootProjectPath.value, _cachePath.value, _downloadHybridUrl.value)
    }

    fun onCachePathChange(path: String?) {
        _cachePath.update { path ?: "" }
        isDownloadClickable(rootPath = _rootProjectPath.value, _cachePath.value, _downloadHybridUrl.value)
    }

    private fun updateDownloadState(isDownloading: Boolean) {
        _isDownloading.update { isDownloading }
    }

    private fun updateDownloadProgress(progress: Float) {
        _downloadProgress.update { progress }
    }

    fun changeClickable(isClickable: Boolean) {
        _isClickable.update { isClickable }
    }

    fun validateDownloadUrl(url: String?): String? {
        // 必须为php或zip结尾
        val isEndWithSpecific = url?.endsWith("php") == true || url?.endsWith("zip") == true
        if (url.isNullOrEmpty() || !isEndWithSpecific) {
            return null
        }
        val versionNumber = HYBRID_PATTERN_REG.find(url)
        println("versionNumber = ${versionNumber?.value}")
        appendLogString("versionNumber = ${versionNumber?.value}")
        return versionNumber?.value
    }

    /**
     * Is ready to do download task
     *
     * @param rootPath
     * @param cache
     * @param url
     * @return is ready
     */
    private fun isDownloadClickable(rootPath: String?, cache: String?, url: String?): Boolean {
        return !_isDownloading.value && !rootPath.isNullOrBlank() && !cache.isNullOrEmpty() && validateDownloadUrl(url) != null

    }

    fun onTriggerClick() {
        changeClickable(false)
        updateDownloadState(true)
        clearLogString()
        val versionNumber = validateDownloadUrl(url = _downloadHybridUrl.value)
        if (versionNumber == null) {
            appendLogString("not match versionNumber !!", true)
            return
        }
        ioScope.launch {
            //1. step on download resource zip
            val file = withContext(Dispatchers.IO) {
                FileUtils.mkDir(_cachePath.value + "/$versionNumber")
                File(_cachePath.value + "/$versionNumber/index.zip")
            }
//            val totalStep = getTotalStep()
            client.downloadFile(_downloadHybridUrl.value, file)
                .onStart {
                    appendLogString("开始下载: >>>>>>>>>>>>>>>>>>")
                    println("开始下载: >>>>>>>>>>>>>>>>>>")
                }
                // download resource
                .onEach {
                    onDownloadStateChange(it)
                }
                //判定加过滤step 2 条件
                .filter { it is DownloadResult.Success && _isAutoUnZip.value && _cachePath.value.isNotEmpty() }
                .map {
                    appendLogString("开始解压缩: >>>>>>>>>>>>>>>>>>")
                    println("开始解压缩: >>>>>>>>>>>>>>>>>>")
                    withContext(Dispatchers.IO) {
                        file.unzipFile(_cachePath.value)
                    }
                }
                //判定加过滤step 3 条件
                .filter { _isAutoReplace.value && it }
                .map {
                    appendLogString("开始替换文件: >>>>>>>>>>>>>>>>>>")
                    replaceFile(file, _rootProjectPath.value + PROJECT_ASSERT_SUFFIX)
                }
                //判定加过滤step 4 条件
                .filter { _isAutoCommit.value }
                .map {
                    appendLogString("开始提交: >>>>>>>>>>>>>>>>>>")
                    println("开始提交: >>>>>>>>>>>>>>>>>>")
                    doGitCommit(COMMIT_DEFAULT_TEMPLATE + versionNumber, _rootProjectPath.value)
                }.onCompletion {
                    it?.let {
                        appendLogString("发生异常终止: <<<<<<<<<<< $it", true)
                        println("发生异常终止: <<<<<<<<<<< $it")
                    }
                    appendLogString("操作结束: >>>>>>>>>>>>>>>>>>")
                    println("提交结束: >>>>>>>>>>>>>>>>>>")
                }
                .collect()

        }
    }

    private suspend fun replaceFile(file: File, dstDir: String): Boolean {
        return withContext(Dispatchers.IO) {
            FileUtils.moveFilesTo(file, File(dstDir))
        }
    }

    private suspend fun doGitCommit(commitMessage: String, gitRoot: String): UpgradeResult {
        // TODO: 2022/7/6
        return withContext(Dispatchers.IO) {
            UpgradeResult.Finish
        }
    }

    private fun getTotalStep(): Int {
        var totalStep = 1

        _isAutoUnZip.value.let {
            totalStep += 1
        }
        _isAutoReplace.value.let {
            totalStep += 1
        }
        _isAutoCommit.value.let {
            totalStep += 1
        }
        return totalStep
    }

    fun onAutoUnZipClick(isOpen: Boolean) {
        _isAutoUnZip.update { isOpen }
        //协同操作
        if (!isOpen) {
            _isAutoReplace.update { false }
            _isAutoCommit.update { false }
        }
        appendLogString("自动压缩 ${if (isOpen) "打开" else "关闭"}")
    }

    fun onAutoReplaceClick(isOpen: Boolean) {
        _isAutoReplace.update { isOpen }
        //协同操作
        if (isOpen) {
            _isAutoUnZip.update { true }
        } else {
            _isAutoCommit.update { false }
        }
        appendLogString("自动替换 ${if (isOpen) "打开" else "关闭"}")
    }

    fun onAutoCommitClick(isOpen: Boolean) {
        _isAutoCommit.update { isOpen }
        //协同操作
        if (isOpen) {
            _isAutoUnZip.update { true }
            _isAutoReplace.update { true }
        }

        appendLogString("自动提交 ${if (isOpen) "打开" else "关闭"}")
    }


    //region 主实现流程
    /**
     * 1. 当下载事件触发
     *
     * @param it
     */
    private fun onDownloadStateChange(it: DownloadResult): Flow<DownloadResult> {
        when (it) {
            is DownloadResult.Success -> {
                changeClickable(true)
                updateDownloadState(false)
                appendLogString("download success .")
            }
            is DownloadResult.Error -> {
                changeClickable(true)
                updateDownloadState(false)
                appendLogString("download error!!! $it", true)
            }
            is DownloadResult.Progress -> {
                updateDownloadProgress(it.progress)
            }
        }
        return flowOf(it)
    }
    //endregion

    private fun appendLogString(string: String?, isError: Boolean = false) {
        _logString.update { _logString.value + "\n" + string }
        _logError.update { isError }
    }

    private fun clearLogString() {
        _logString.update { "" }
        _logError.update { false }
    }
}