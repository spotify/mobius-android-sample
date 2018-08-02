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
package com.example.android.architecture.blueprints.todoapp.tasks

import com.example.android.architecture.blueprints.todoapp.tasks.domain.*
import com.example.android.architecture.blueprints.todoapp.util.loopFactory
import com.spotify.mobius.EventSource
import com.spotify.mobius.MobiusLoop
import com.spotify.mobius.android.AndroidLogger
import com.spotify.mobius.android.MobiusAndroid
import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer

fun createController(
        effectHandler: ObservableTransformer<TasksListEffect, TasksListEvent>,
        eventSource: EventSource<TasksListEvent>,
        defaultModel: TasksListModel): MobiusLoop.Controller<TasksListModel, TasksListEvent> {

    return MobiusAndroid.controller(createLoop(eventSource, effectHandler), defaultModel)
}

private fun createLoop(
        eventSource: EventSource<TasksListEvent>,
        effectHandler: ObservableTransformer<TasksListEffect, TasksListEvent>): MobiusLoop.Factory<TasksListModel, TasksListEvent, TasksListEffect> {

    return loopFactory(::update, effectHandler)
            .init(::init)
            .eventSource(eventSource)
            .logger(AndroidLogger.tag("TasksList"))
}
