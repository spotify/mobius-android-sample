package com.example.android.architecture.blueprints.todoapp.util

fun <E> List<E>.change(index: Int, item: E) = this.toMutableList().apply { set(index, item) }.toList()
fun <E> List<E>.change(entry: Pair<Int, E>): List<E> = change(entry.first, entry.second)
fun <E> List<E>.without(items: List<E>): List<E> = toMutableList().apply { removeAll(items) }.toList()

sealed class Either<L, R> {
    data class Left<L, Any>(val value: L) : Either<L, Any>()
    data class Right<Any, R>(val value: R) : Either<Any, R>()

    companion object {
        fun <L, R> left(value: L): Either<L, R> = Either.Left(value)
        fun <L, R> right(value: R): Either<L, R> = Either.Right(value)
    }
}

fun <L, R, T> Either<L, R>.fold(
        l: (L) -> T,
        r: (R) -> T) =
        when(this) {
            is Either.Left -> l(this.value)
            is Either.Right -> r(this.value)
        }