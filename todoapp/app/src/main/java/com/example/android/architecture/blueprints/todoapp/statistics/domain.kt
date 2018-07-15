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

import com.example.android.architecture.blueprints.todoapp.data.Task

sealed class StatisticsState {

    object Loading : StatisticsState()

    data class Loaded(@get:JvmName("activeCount") val activeCount: Int, @get:JvmName("completedCount") val completedCount: Int) : StatisticsState()

    object Failed : StatisticsState()

    companion object {
        @JvmStatic
        fun loading() = Loading as StatisticsState

        @JvmStatic
        fun loaded(activeCount: Int, completedCount: Int) = Loaded(activeCount, completedCount) as StatisticsState

        @JvmStatic
        fun failed() = Failed as StatisticsState
    }
}

sealed class StatisticsEvent {

    data class TasksLoaded(@get:JvmName("tasks") val tasks: List<Task>) : StatisticsEvent()

    object TasksLoadingFailed : StatisticsEvent()

    companion object {
        @JvmStatic
        fun tasksLoaded(tasks: List<Task>) = TasksLoaded(tasks) as StatisticsEvent

        @JvmStatic
        fun tasksLoadingFailed() = TasksLoadingFailed as StatisticsEvent
    }
}

sealed class StatisticsEffect {
    object LoadTasks : StatisticsEffect()

    companion object {
        @JvmStatic
        fun loadTasks() = LoadTasks as StatisticsEffect
    }
}
