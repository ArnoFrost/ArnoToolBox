package com.arno.tech.toolbox.util

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.*

sealed class DownloadResult {

    object Success : DownloadResult()

    data class Error(val message: String, val cause: Exception? = null) : DownloadResult()

    data class Progress(val progress: Float) : DownloadResult()
}

//传入url字符串进行下载
suspend fun HttpClient.download(url: String, fileOutputStream: FileOutputStream) = withContext(Dispatchers.IO) {
    this@download.get(url).bodyAsChannel().toInputStream().copyTo(fileOutputStream)
}

/**
 * 带有进度条下载文件
 *
 * @param url
 * @param tempFile
 * @return 结果
 */
suspend fun HttpClient.downloadFile(
    url: String,
    tempFile: File
): Flow<DownloadResult> {
    return runBlocking(Dispatchers.IO) {
        flow {
            prepareGet(url).execute { httpResponse ->
                val channel: ByteReadChannel = httpResponse.body()
                val total = httpResponse.contentLength() ?: 0
                val isSupportProgress = total > 0
                var fakeProgress = 0F
                val fakeMaxProgressValue = 0.8F

                while (!channel.isClosedForRead) {
                    val packet = channel.readRemaining(DEFAULT_BUFFER_SIZE.toLong())
                    while (!packet.isEmpty) {
                        val bytes = packet.readBytes()
                        tempFile.appendBytes(bytes)
                        if (isSupportProgress) {
                            println("Received ${tempFile.length()} bytes from $total")
                            val progress = (tempFile.length() / total).toFloat()
                            emit(DownloadResult.Progress(progress))
                        } else {
                            println("Received ${tempFile.length()} bytes")
                            // 如果没有做一个假的
                            fakeProgress += Random().nextFloat()
                            //约束最大显示80%
                            fakeProgress = fakeMaxProgressValue.coerceAtMost(fakeProgress)
                            emit(DownloadResult.Progress(fakeProgress))
                        }
                    }
                }
                if (httpResponse.status.isSuccess()) {
                    println("A file saved to ${tempFile.path}")
                    if (!isSupportProgress) {
                        emit(DownloadResult.Progress(1F))
                    }
                    emit(DownloadResult.Success)
                } else {
                    emit(DownloadResult.Error("File not downloaded"))
                }
            }
        }
    }
}