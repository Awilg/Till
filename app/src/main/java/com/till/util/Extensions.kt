package com.till.util

fun <K, V> MutableMap<K, V>.mergeReduceInPlace(vararg otherMap: Map<K, V>, reduce: (V, V) -> V) =
    otherMap.forEach { otherEntry ->
        otherEntry.forEach {
            merge(it.key, it.value, reduce)
        }
    }