package com.arno.tech.toolbox.util

import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

suspend fun File.unzipFile(descDir: String) = suspendCoroutine<Boolean> { continuation ->
    continuation.resume(ZipUtil.unzip(this.absolutePath, descDir))
}

object ZipUtil {
    fun unzip(zipFile: String, descDir: String): Boolean {
        var result: Boolean
        try {

            val buffer = ByteArray(1024)
            var outputStream: OutputStream?
            var inputStream: InputStream?
            val zf = ZipFile(zipFile)
            val entries = zf.entries()
            while (entries.hasMoreElements()) {
                val zipEntry: ZipEntry = entries.nextElement() as ZipEntry
                val zipEntryName: String = zipEntry.name

                inputStream = zf.getInputStream(zipEntry)
                inputStream?.use { ips ->
                    val descFilePath: String = descDir + File.separator + zipEntryName
                    val descFile: File = createFile(descFilePath)
                    outputStream = FileOutputStream(descFile)
                    outputStream?.use { ops ->
                        var len: Int
                        while (ips.read(buffer).also { len = it } > 0) {
                            ops.write(buffer, 0, len)
                        }
                    }
                }
            }
            result = true
        } catch (e: Exception) {
            result = false
        }

        return result

    }

    private fun createFile(filePath: String): File {
        val file = File(filePath)
        val parentFile = file.parentFile!!
        if (!parentFile.exists()) {
            parentFile.mkdirs()
        }
        if (!file.exists()) {
            file.createNewFile()
        }
        return file
    }

    fun zip(files: List<File>, zipFilePath: String) {
        if (files.isEmpty()) return

        val zipFile = createFile(zipFilePath)
        val buffer = ByteArray(1024)
        var zipOutputStream: ZipOutputStream? = null
        var inputStream: FileInputStream? = null
        try {
            zipOutputStream = ZipOutputStream(FileOutputStream(zipFile))
            for (file in files) {
                if (!file.exists()) continue
                zipOutputStream.putNextEntry(ZipEntry(file.name))
                inputStream = FileInputStream(file)
                var len: Int
                while (inputStream.read(buffer).also { len = it } > 0) {
                    zipOutputStream.write(buffer, 0, len)
                }
                zipOutputStream.closeEntry()
            }
        } finally {
            inputStream?.close()
            zipOutputStream?.close()
        }
    }

    fun zipByFolder(fileDir: String, zipFilePath: String) {
        val folder = File(fileDir)
        if (folder.exists() && folder.isDirectory) {
            val files = folder.listFiles()
            val filesList: List<File> = files.toList()
            zip(filesList, zipFilePath)
        }
    }
}