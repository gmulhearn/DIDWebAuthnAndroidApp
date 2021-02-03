package com.example.indytest.base

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

/**
 * Provider of coroutine dispatchers. Using the same naming as provided by kotlin's coroutine
 * library in [Dispatchers]
 */
interface DispatcherProvider {

    val Main: CoroutineDispatcher

    val IO: CoroutineDispatcher
}

class DefaultDispatcherProvider @Inject constructor() : DispatcherProvider {
    override val Main: CoroutineDispatcher = Dispatchers.Main
    override val IO: CoroutineDispatcher = Dispatchers.IO
}
