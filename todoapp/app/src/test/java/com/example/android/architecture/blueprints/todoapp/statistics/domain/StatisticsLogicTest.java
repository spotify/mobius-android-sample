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

import static com.example.android.architecture.blueprints.todoapp.statistics.StatisticsEffect.loadTasks;
import static com.example.android.architecture.blueprints.todoapp.statistics.StatisticsEvent.tasksLoaded;
import static com.example.android.architecture.blueprints.todoapp.statistics.StatisticsEvent.tasksLoadingFailed;
import static com.example.android.architecture.blueprints.todoapp.statistics.StatisticsState.failed;
import static com.example.android.architecture.blueprints.todoapp.statistics.StatisticsState.loaded;
import static com.example.android.architecture.blueprints.todoapp.statistics.StatisticsState.loading;
import static com.google.common.collect.ImmutableList.of;
import static com.spotify.mobius.test.InitSpec.assertThatFirst;
import static com.spotify.mobius.test.NextMatchers.hasModel;
import static com.spotify.mobius.test.NextMatchers.hasNoEffects;
import static com.spotify.mobius.test.UpdateSpec.assertThatNext;

import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.TaskDetails;
import com.example.android.architecture.blueprints.todoapp.statistics.StatisticsEffect;
import com.example.android.architecture.blueprints.todoapp.statistics.StatisticsEvent;
import com.example.android.architecture.blueprints.todoapp.statistics.StatisticsLogic;
import com.example.android.architecture.blueprints.todoapp.statistics.StatisticsState;
import com.spotify.mobius.test.FirstMatchers;
import com.spotify.mobius.test.InitSpec;
import com.spotify.mobius.test.UpdateSpec;
import org.junit.Test;

public class StatisticsLogicTest {

  private InitSpec<StatisticsState, StatisticsEffect> initSpec =
      new InitSpec<>(StatisticsLogic::init);
  private UpdateSpec<StatisticsState, StatisticsEvent, StatisticsEffect> updateSpec =
      new UpdateSpec<>(StatisticsLogic::update);

  @Test
  public void initDispatchLoadTaskEffectWhenLoadingOrFailed() {
    initSpec
        .when(loading())
        .then(
            assertThatFirst(
                FirstMatchers.hasModel(loading()), FirstMatchers.hasEffects(loadTasks())));

    initSpec
        .when(failed())
        .then(
            assertThatFirst(
                FirstMatchers.hasModel(loading()), FirstMatchers.hasEffects(loadTasks())));
  }

  @Test
  public void initDispatchesNoEffectsOnStateRestore() {
    initSpec
        .when(loaded(5, 10))
        .then(assertThatFirst(FirstMatchers.hasModel(loaded(5, 10)), FirstMatchers.hasNoEffects()));
  }

  @Test
  public void tasksLoadedShouldUpdateStateWithTaskCounts() {
    updateSpec
        .given(loading())
        .when(tasksLoaded(of(task("t1"), task("t2"), task("t3", true))))
        .then(assertThatNext(hasModel(loaded(2, 1)), hasNoEffects()));
  }

  @Test
  public void noTasksShouldHaveZeroCounts() {
    updateSpec
        .given(loading())
        .when(tasksLoaded(of()))
        .then(assertThatNext(hasModel(loaded(0, 0)), hasNoEffects()));
  }

  @Test
  public void noActiveTasksShouldHaveZeroForActiveCount() {
    updateSpec
        .given(loading())
        .when(tasksLoaded(of(task("t3", true))))
        .then(assertThatNext(hasModel(loaded(0, 1)), hasNoEffects()));
  }

  @Test
  public void noCompletedTasksShouldHaveZeroForCompletedCount() {
    updateSpec
        .given(loading())
        .when(tasksLoaded(of(task("t1"), task("t2"))))
        .then(assertThatNext(hasModel(loaded(2, 0)), hasNoEffects()));
  }

  @Test
  public void failureToLoadTasksChangesStateToFailed() {
    updateSpec
        .given(loading())
        .when(tasksLoadingFailed())
        .then(assertThatNext(hasModel(failed()), hasNoEffects()));
  }

  private Task task(String title) {
    return task(title, false);
  }

  private Task task(String title, boolean completed) {
    return Task.create(
        title.toLowerCase(),
        TaskDetails.create(title, title.concat(title).concat(title), completed));
  }
}
