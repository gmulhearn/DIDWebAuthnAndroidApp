package com.example.indytest.base

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

/**
 * Implementation of CoroutineScope that has a default error handler and cleans up on [destroy]
 */
class MSCoroutineScope @Inject constructor(
    dispatchers: DispatcherProvider
) : CoroutineScope {

    private val parentJob = SupervisorJob()

    /**
     * The context of this scope, which runs on dispatchers.Main by default.
     */
    override val coroutineContext: CoroutineContext = dispatchers.Main + parentJob + CoroutineExceptionHandler { _, throwable ->
        if (throwable !is CancellationException) {
            // Default error handler for coroutine context that logs to timber.
            println(throwable)
            println(throwable.localizedMessage)
            println(throwable.message)
            println(throwable.cause)

        }
    }

    /**
     * Cancel any currently running jobs.
     */
    fun cancelJobs() {
        coroutineContext.cancelChildren()
    }

    /**
     * Cancel any currently running jobs and prevents any further coroutines
     * from being launched in this scope again.
     */
    fun destroy() {
        coroutineContext.cancelChildren()
        coroutineContext.cancel() // no further work will be done
        parentJob.cancel()
    }
}
