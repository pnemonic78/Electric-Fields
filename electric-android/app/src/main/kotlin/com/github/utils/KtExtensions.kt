package com.github.utils

fun <E> List<E>.copy(): List<E> {
    return ArrayList(this)
}

fun <E> MutableList<E>.copyMutable(): List<E> {
    return ArrayList(this)
}
