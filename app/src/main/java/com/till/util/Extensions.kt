package com.till.util

import timber.log.Timber

fun <K, V> MutableMap<K, V>.mergeReduceInPlace(vararg otherMap: Map<K, V>, reduce: (V, V) -> V) =
    otherMap.forEach { otherEntry ->
        otherEntry.forEach {
            Timber.v("Merging [${it.key}, ${it.value}]...")
            merge(it.key, it.value, reduce)
        }
    }