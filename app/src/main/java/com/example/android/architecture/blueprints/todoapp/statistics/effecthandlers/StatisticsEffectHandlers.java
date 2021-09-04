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
package com.example.android.architecture.blueprints.todoapp.statistics.effecthandlers;

import static com.example.android.architecture.blueprints.todoapp.statistics.domain.StatisticsEvent.tasksLoadingFailed;

import android.content.Context;
import com.example.android.architecture.blueprints.todoapp.data.source.local.TasksLocalDataSource;
import com.example.android.architecture.blueprints.todoapp.statistics.domain.StatisticsEffect;
import com.example.android.architecture.blueprints.todoapp.statistics.domain.StatisticsEffect.LoadTasks;
import com.example.android.architecture.blueprints.todoapp.statistics.domain.StatisticsEvent;
import com.example.android.architecture.blueprints.todoapp.util.schedulers.SchedulerProvider;
import com.google.common.collect.ImmutableList;
import com.spotify.mobius.rx2.RxMobius;
import io.reactivex.ObservableTransformer;

public class StatisticsEffectHandlers {
  public static ObservableTransformer<StatisticsEffect, StatisticsEvent> createEffectHandler(
      Context context) {
    TasksLocalDataSource localSource =
        TasksLocalDataSource.getInstance(context, SchedulerProvider.getInstance());
    return RxMobius.<StatisticsEffect, StatisticsEvent>subtypeEffectHandler()
        .addTransformer(LoadTasks.class, loadTasksHandler(localSource))
        .build();
  }

  private static ObservableTransformer<LoadTasks, StatisticsEvent> loadTasksHandler(
      TasksLocalDataSource localSource) {
    return effects ->
        effects.flatMap(
            loadTasks ->
                localSource
                    .getTasks()
                    .toObservable()
                    .take(1)
                    .map(ImmutableList::copyOf)
                    .map(StatisticsEvent::tasksLoaded)
                    .onErrorReturnItem(tasksLoadingFailed()));
  }
}
