@file:JvmName("StatisticsLogic")
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
package com.example.android.architecture.blueprints.todoapp.statistics

import com.spotify.mobius.Effects.effects
import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Next
import com.spotify.mobius.Next.next

fun init(state: StatisticsState): First<StatisticsState, StatisticsEffect> {
    return when (state) {
        is StatisticsState.Loading -> first(state, effects<StatisticsEffect, StatisticsEffect.LoadTasks>(StatisticsEffect.LoadTasks))
        is StatisticsState.Loaded -> first(state)
        is StatisticsState.Failed -> first(StatisticsState.Loading, effects<StatisticsEffect, StatisticsEffect.LoadTasks>(StatisticsEffect.LoadTasks))
    }
}

fun update(state: StatisticsState, event: StatisticsEvent): Next<StatisticsState, StatisticsEffect> {
    return when (event) {
        is StatisticsEvent.TasksLoaded -> {
            event.tasks.filter { it.details.completed }.size.let {
                next<StatisticsState, StatisticsEffect>(StatisticsState.Loaded(event.tasks.size - it, it))
            }
        }
        is StatisticsEvent.TasksLoadingFailed -> next<StatisticsState, StatisticsEffect>(StatisticsState.Failed)
    }
}