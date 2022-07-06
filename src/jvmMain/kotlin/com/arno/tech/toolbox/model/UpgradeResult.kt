package com.arno.tech.toolbox.model

sealed class UpgradeResult {
    object Finish : UpgradeResult()

    data class Error(val message: String, val cause: Exception? = null) : UpgradeResult()

    data class Progress(val progress: Float) : UpgradeResult()
}