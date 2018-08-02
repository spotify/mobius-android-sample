/*
 * -\-\-
 * --
 * Copyright (c) 2017-2018 Spotify AB
 * --
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * -/-/-
 */
package com.example.android.architecture.blueprints.todoapp.util

import com.spotify.mobius.Connectable
import com.spotify.mobius.Connection
import com.spotify.mobius.Next
import com.spotify.mobius.Update
import com.spotify.mobius.functions.Consumer
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

fun <T> onAccept(onModelChange: (T) -> Unit): PartialConnection<T> = PartialConnection(onModelChange)

fun <I, J, O> Connectable<I, O>.contramap(block: (J) -> I) = ContramapConnectable(block, this)

class ContramapConnectable<I, J, O> (
        private val map: (J) -> I,
        private val delegate: Connectable<I, O>) : Connectable<J, O> {
    override fun connect(output: Consumer<O>): Connection<J> {
        val connection = delegate.connect(output)
        return onAccept<J> { connection.accept(map(it))}
                .onDispose { connection.dispose() }
    }
}

