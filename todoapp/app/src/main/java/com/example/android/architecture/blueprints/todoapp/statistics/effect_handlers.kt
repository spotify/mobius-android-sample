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

import android.content.Context
import com.example.android.architecture.blueprints.todoapp.data.source.local.TasksLocalDataSource
import com.example.android.architecture.blueprints.todoapp.util.schedulers.SchedulerProvider
import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer

fun createEffectHandler(context: Context): ObservableTransformer<StatisticsEffect, StatisticsEvent> {
    val localDataSource = TasksLocalDataSource.getInstance(context, SchedulerProvider.getInstance())
    return RxMobius.subtypeEffectHandler<StatisticsEffect, StatisticsEvent>()
            .addTransformer(StatisticsEffect.LoadTasks::class.java, loadTasksHandler(localDataSource))
            .build()
}

fun loadTasksHandler(localSource: TasksLocalDataSource): ObservableTransformer<StatisticsEffect.LoadTasks, StatisticsEvent> {
    return ObservableTransformer {
        it.flatMap {
            localSource.tasks.toObservable()
                    .take(1)
                    .map { StatisticsEvent.TasksLoaded(it) as StatisticsEvent }
                    .onErrorReturnItem(StatisticsEvent.TasksLoadingFailed)
        }
    }
}
