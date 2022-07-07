package com.arno.tech.toolbox.model.config

import kotlinx.serialization.Serializable

@Serializable
data class UpgradeConfig(
    val rootPath: String? = "",
    val cachePath: String? = "",
    val isAutoUnzip: Boolean = true,
    val isAutoReplace: Boolean = true,
    val isAutoCommit: Boolean = true
)