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
package com.example.android.architecture.blueprints.todoapp.addedittask

import com.example.android.architecture.blueprints.todoapp.addedittask.domain.Effect
import com.example.android.architecture.blueprints.todoapp.addedittask.domain.Event
import com.example.android.architecture.blueprints.todoapp.addedittask.domain.Model
import com.example.android.architecture.blueprints.todoapp.addedittask.domain.update
import com.example.android.architecture.blueprints.todoapp.util.loopFactory
import com.spotify.mobius.MobiusLoop
import com.spotify.mobius.android.AndroidLogger
import com.spotify.mobius.android.MobiusAndroid
import io.reactivex.ObservableTransformer

fun createController(
        effectHandlers: ObservableTransformer<Effect, Event>,
        defaultModel: Model): MobiusLoop.Controller<Model, Event> {
    return MobiusAndroid.controller(createLoop(effectHandlers), defaultModel)
}

private fun createLoop(effectHandlers: ObservableTransformer<Effect, Event>): MobiusLoop.Factory<Model, Event, Effect> {
    return loopFactory(::update, effectHandlers)
            .logger(AndroidLogger.tag("Add/Edit Tasks"))
}
