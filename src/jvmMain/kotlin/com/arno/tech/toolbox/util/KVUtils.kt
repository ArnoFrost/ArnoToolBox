package com.arno.tech.toolbox.util

import com.arno.tech.toolbox.model.config.UpgradeConfig
import com.toddway.shelf.*
import kotlinx.serialization.InternalSerializationApi
import java.io.File

object KVUtils {
    private const val PROPERTIES_FOLDER = "/.config/arnotoolbox"
    private val CACHE_FOLDER = System.getProperty("user.home") + PROPERTIES_FOLDER

    @OptIn(InternalSerializationApi::class)
    val shelf by lazy {
        val kotlinxSerializer = KotlinxSerializer().apply {
            register(UpgradeConfig.serializer())
        }
        Shelf(FileStorage(File(CACHE_FOLDER)), kotlinxSerializer)
    }

    fun saveValue(key: String, obj: Any) {
        shelf.item(key).put(obj)
    }

    inline fun <reified T> getValue(key: String): T? {
        return shelf.item(key).get()
    }
}