package com.arno.tech.toolbox.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/**
 * Class to be used for UI presenters and viewModels.
 * Clear will be automatically called if the ViewController is declared in a Koin scope bound to
 * navigation. It should be declared as follows:
 * ```
 * scope<T> {
 *     scoped { MyViewController() } bind Cleanable::class
 * }
 * ```
 */
open class ViewController : Cleanable {
    protected val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    protected val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    override fun clean() {
        coroutineScope.cancel()
        ioScope.cancel()
    }
}
