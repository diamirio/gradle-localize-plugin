package com.tailoredapps.gradle.localize.util

import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

fun <T> Iterable<T>.forEachParallel(action: suspend (T) -> Unit): Unit = runBlocking {
    map { async { action(it) } }
        .forEach { it.await() }
}
