package com.example.android.architecture.blueprints.todoapp.util

import com.spotify.mobius.Connection
import com.spotify.mobius.Next
import com.spotify.mobius.Update
import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer

private fun <M, E, F> updateWrapper(u: (M, E) -> Next<M, F>): Update<M, E, F> = Update { m: M, e: E -> u(m, e) }

fun <M, E, F> loopFactory(u: (M, E) -> Next<M, F>, fh: ObservableTransformer<F, E>) = RxMobius.loop(updateWrapper(u), fh)

class PartialConnection<T>(val onModelChange: (T) -> Unit) {
    fun onDispose(dispose: () -> Unit): Connection<T> = object : Connection<T> {
        override fun accept(value: T) {
            onModelChange(value)
        }

        override fun dispose() {
            dispose()
        }
    }
}
fun <T> onNew(onModelChange: (T) -> Unit) : PartialConnection<T> = PartialConnection(onModelChange)