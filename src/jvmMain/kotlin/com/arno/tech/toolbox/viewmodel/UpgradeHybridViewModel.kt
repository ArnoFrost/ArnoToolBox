package com.arno.tech.toolbox.viewmodel

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.flow.*
import io.ktor.client.engine.java.*


class UpgradeHybridViewModel : ViewController() {
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


    private val _client = HttpClient(Java)
    val client: HttpClient
        get() = _client

    fun onProjectRootChange(path: String?) {
        _rootProjectPath.update { path ?: "" }
    }

    fun onDownloadUrlChange(url: String?) {
        _downloadHybridUrl.update { url ?: "" }
    }

    fun onCachePathChange(path: String?) {
        _cachePath.update { path ?: "" }
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
}