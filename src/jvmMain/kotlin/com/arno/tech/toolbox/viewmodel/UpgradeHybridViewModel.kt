package com.arno.tech.toolbox.viewmodel

import io.ktor.client.*
import io.ktor.client.engine.java.*
import kotlinx.coroutines.flow.*

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
    }

    private val _client = HttpClient(Java)
    val client: HttpClient
        get() = _client

    private val _rootProjectPath = MutableStateFlow("")
    val rootProjectPath: Flow<String>
        get() = _rootProjectPath.asStateFlow()
    private val _downloadHybridUrl = MutableStateFlow("")
    val downloadHybridUrl: Flow<String>
        get() = _downloadHybridUrl.asStateFlow()
    private val _cachePath = MutableStateFlow("")
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

    fun updateDownloadState(isDownloading: Boolean) {
        _isDownloading.update { isDownloading }
    }

    fun updateDownloadProgress(progress: Float) {
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
    fun isDownloadClickable(rootPath: String?, cache: String?, url: String?): Boolean {
        return !_isDownloading.value && !rootPath.isNullOrBlank() && !cache.isNullOrEmpty() && validateDownloadUrl(url) != null

    }

    fun appendLogString(string: String?) {
        _logString.update { _logString.value + "\n" + string }
    }

    fun clearLogString() {
        _logString.update { "" }
    }

    fun onDownloadClick() {
        changeClickable(false)
        updateDownloadState(true)
        clearLogString()
    }
}