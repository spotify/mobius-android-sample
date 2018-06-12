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
package com.example.android.architecture.blueprints.todoapp.statistics.domain;

import static com.example.android.architecture.blueprints.todoapp.statistics.domain.StatisticsEffect.loadTasks;
import static com.example.android.architecture.blueprints.todoapp.statistics.domain.StatisticsState.failed;
import static com.example.android.architecture.blueprints.todoapp.statistics.domain.StatisticsState.loaded;
import static com.example.android.architecture.blueprints.todoapp.statistics.domain.StatisticsState.loading;
import static com.spotify.mobius.Effects.effects;
import static com.spotify.mobius.First.first;
import static com.spotify.mobius.Next.next;

import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.google.common.collect.ImmutableList;
import com.spotify.mobius.First;
import com.spotify.mobius.Next;
import javax.annotation.Nonnull;

public final class StatisticsLogic {

  private StatisticsLogic() {}

  @Nonnull
  public static First<StatisticsState, StatisticsEffect> init(StatisticsState state) {
    return state.map(
        loading -> first(state, effects(loadTasks())),
        First::first,
        failed -> first(loading(), effects(loadTasks())));
  }

  @Nonnull
  public static Next<StatisticsState, StatisticsEffect> update(
      StatisticsState state, StatisticsEvent event) {
    return event.map(
        tasksLoaded -> {
          ImmutableList<Task> tasks = tasksLoaded.tasks();
          int activeCount = 0;
          int completedCount = 0;
          for (Task task : tasks) {
            if (task.details().completed()) completedCount++;
            else activeCount++;
          }
          return next(loaded(activeCount, completedCount));
        },
        tasksLoadingFailed -> next(failed()));
  }
}
