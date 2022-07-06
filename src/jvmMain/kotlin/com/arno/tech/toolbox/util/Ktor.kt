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
import kotlin.math.roundToInt

sealed class DownloadResult {

    object Success : DownloadResult()

    data class Error(val message: String, val cause: Exception? = null) : DownloadResult()

    data class Progress(val progress: Int) : DownloadResult()
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

                while (!channel.isClosedForRead) {
                    val packet = channel.readRemaining(DEFAULT_BUFFER_SIZE.toLong())
                    while (!packet.isEmpty) {
                        val bytes = packet.readBytes()
                        tempFile.appendBytes(bytes)
                        println("Received ${tempFile.length()} bytes from $total")
                        if (total > 0) {
                            val progress = (tempFile.length() * 100F / total).roundToInt()
                            emit(DownloadResult.Progress(progress))
                        } else {
                            emit(DownloadResult.Error("total is zero!!"))
                            channel.cancel()
                            break
                        }
                    }
                }
                if (httpResponse.status.isSuccess()) {
                    println("A file saved to ${tempFile.path}")
                    emit(DownloadResult.Success)
                } else {
                    emit(DownloadResult.Error("File not downloaded"))
                }
            }
        }
    }
}