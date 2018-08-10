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

/**
 * When isn't exhaustive when used as a statement. This is a workaround that allows us to make sure
 * it is exhaustive.
 *
 * Do exhaustive when(sealedClass) {
 *  is A -> doSomething()
 *  is B -> doSomethingElse()
 * }
 */
object Do {
    infix inline fun exhaustive(t: Any) = t
}