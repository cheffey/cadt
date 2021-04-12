package org.chef.cadt.util

import org.chef.cadt.exception.WithUselessTraceException
import org.openqa.selenium.NoSuchSessionException

/**
 * Created by Chef.Xie
 */
object ExceptionUtil {
    fun <V> tryOrNull(attempt: () -> V): V? {
        return try {
            attempt.invoke()
        } catch (t: Throwable) {
            null
        }
    }

    fun tryRunIgnoredException(attempt: () -> Unit) {
        try {
            attempt.invoke()
        } catch (ignored: Throwable) {
        }
    }

    fun tryRunHandleException(attempt: () -> Unit) {
        try {
            attempt.invoke()
        } catch (e: WithUselessTraceException) {
            e.message?.let { System.err.println(it) }
        } catch (e: NoSuchSessionException) {
            println("Session is EXPIRED. Try connect with command: reconnect")
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
}

interface ThrowableRunnable {
    @Throws(Throwable::class)
    fun run()
}