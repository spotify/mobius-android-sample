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
package com.example.android.architecture.blueprints.todoapp.statistics.ui

import com.example.android.architecture.blueprints.todoapp.statistics.StatisticsEffect
import com.example.android.architecture.blueprints.todoapp.statistics.StatisticsEvent
import com.example.android.architecture.blueprints.todoapp.statistics.StatisticsState
import com.example.android.architecture.blueprints.todoapp.statistics.init
import com.example.android.architecture.blueprints.todoapp.statistics.update
import com.example.android.architecture.blueprints.todoapp.util.loopFactory
import com.spotify.mobius.android.MobiusAndroid
import io.reactivex.ObservableTransformer

fun createController(
        effectHandler: ObservableTransformer<StatisticsEffect, StatisticsEvent>,
        defaultState: StatisticsState) = MobiusAndroid.controller(createLoop(effectHandler), defaultState)

private fun createLoop(effectHandler: ObservableTransformer<StatisticsEffect, StatisticsEvent>) = loopFactory(::update, effectHandler).init(::init)