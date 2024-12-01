package org.doctorate.aktool.utils

fun <E> MutableCollection<E>.replace(elements: List<E>) = this.apply {
    clear()
    addAll(elements)
}

fun <E> MutableCollection<E>.replace(element: E) = this.apply {
    clear()
    add(element)
}

fun <E> MutableCollection<E>.replace(vararg elements: E) = this.apply {
    clear()
    addAll(elements)
}