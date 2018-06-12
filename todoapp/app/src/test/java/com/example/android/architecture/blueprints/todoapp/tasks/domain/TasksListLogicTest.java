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
package com.example.android.architecture.blueprints.todoapp.tasks.domain;

import static com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksListEffect.loadTasks;
import static com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksListEffect.navigateToTaskDetails;
import static com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksListEffect.refreshTasks;
import static com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksListEffect.saveTask;
import static com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksListEffect.showFeedback;
import static com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksListEvent.clearCompletedTasksRequested;
import static com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksListEvent.filterSelected;
import static com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksListEvent.navigateToTaskDetailsRequested;
import static com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksListEvent.refreshRequested;
import static com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksListEvent.taskCreated;
import static com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksListEvent.taskMarkedActive;
import static com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksListEvent.taskMarkedComplete;
import static com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksListEvent.tasksLoaded;
import static com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksListEvent.tasksRefreshed;
import static com.google.common.collect.ImmutableList.of;
import static com.spotify.mobius.test.InitSpec.assertThatFirst;
import static com.spotify.mobius.test.NextMatchers.hasEffects;
import static com.spotify.mobius.test.NextMatchers.hasModel;
import static com.spotify.mobius.test.NextMatchers.hasNoEffects;
import static com.spotify.mobius.test.NextMatchers.hasNoModel;
import static com.spotify.mobius.test.NextMatchers.hasNothing;
import static com.spotify.mobius.test.UpdateSpec.assertThatNext;

import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.TaskDetails;
import com.google.common.collect.ImmutableList;
import com.spotify.mobius.test.FirstMatchers;
import com.spotify.mobius.test.InitSpec;
import com.spotify.mobius.test.UpdateSpec;
import org.junit.Test;

public class TasksListLogicTest {

  private InitSpec<TasksListModel, TasksListEffect> initSpec = new InitSpec<>(TasksListLogic::init);

  private UpdateSpec<TasksListModel, TasksListEvent, TasksListEffect> updateSpec =
      new UpdateSpec<>(TasksListLogic::update);

  @Test
  public void initWithoutDataLoadsRemoteAndLocalData() {
    initSpec
        .when(TasksListModel.DEFAULT)
        .then(
            assertThatFirst(
                FirstMatchers.hasModel(TasksListModel.DEFAULT.withLoading(true)),
                FirstMatchers.hasEffects(refreshTasks(), loadTasks())));
  }

  @Test
  public void initWithDataDispatchesNoEffects() {
    initSpec
        .when(modelWithTasks(task("t1"), task("t2"), task("t3")))
        .then(
            assertThatFirst(
                FirstMatchers.hasModel(modelWithTasks(task("t1"), task("t2"), task("t3"))),
                FirstMatchers.hasEffects(loadTasks())));
  }

  @Test
  public void requestingRefreshesDispatchesLoadRemoteTasksEffect() {
    TasksListModel stateWithSomeTasks = modelWithTasks(task("t1"), task("t2"), task("t3"));
    updateSpec
        .given(stateWithSomeTasks)
        .when(refreshRequested())
        .then(
            assertThatNext(
                hasModel(stateWithSomeTasks.withLoading(true)), hasEffects(refreshTasks())));
  }

  @Test
  public void navigatingToATaskWithAValidIndexDispatchesNavigationEffect() {
    TasksListModel stateWithSomeTasks = modelWithTasks(task("t1"), task("t2"), task("t3"));
    updateSpec
        .given(stateWithSomeTasks)
        .when(navigateToTaskDetailsRequested("t3"))
        .then(assertThatNext(hasNoModel(), hasEffects(navigateToTaskDetails(task("t3")))));
  }

  @Test
  public void completingTasksUpdatesModelAndDispatchesSaveTaskEffect() {
    TasksListModel stateWithSomeTasks = modelWithTasks(task("t1"), task("t2"), task("t3"));
    updateSpec
        .given(stateWithSomeTasks)
        .when(taskMarkedComplete("t2"))
        .then(
            assertThatNext(
                hasModel(stateWithSomeTasks.withTaskAtIndex(task("t2", true), 1)),
                hasEffects(
                    saveTask(task("t2", true)), showFeedback(FeedbackType.MARKED_COMPLETE))));
  }

  @Test
  public void completingTasksWhileHavingAFilterUpdatesTheCorrectIndex() {
    TasksListModel stateWithSomeTasks =
        modelWithTasks(task("t1", true), task("t2"), task("t3"))
            .withTasksFilter(TasksFilterType.ACTIVE_TASKS);

    updateSpec
        .given(stateWithSomeTasks)
        .when(taskMarkedComplete("t2"))
        .then(
            assertThatNext(
                hasModel(stateWithSomeTasks.withTaskAtIndex(task("t2", true), 1)),
                hasEffects(saveTask(task("t2", true)))));
  }

  @Test
  public void activatingATaskUncompletesItAndSavesIt() {
    TasksListModel stateWithSomeTasks = modelWithTasks(task("t1", true), task("t2"), task("t3"));

    updateSpec
        .given(stateWithSomeTasks)
        .when(taskMarkedActive("t1"))
        .then(
            assertThatNext(
                hasModel(stateWithSomeTasks.withTaskAtIndex(task("t1"), 0)),
                hasEffects(saveTask(task("t1")), showFeedback(FeedbackType.MARKED_ACTIVE))));
  }

  @Test
  public void addingAFilterUpdatesTheCurrentFilter() {
    TasksListModel stateWithSomeTasks = modelWithTasks(task("t1", true), task("t2"), task("t3"));

    updateSpec
        .given(stateWithSomeTasks)
        .when(filterSelected(TasksFilterType.COMPLETED_TASKS))
        .then(
            assertThatNext(
                hasModel(stateWithSomeTasks.withTasksFilter(TasksFilterType.COMPLETED_TASKS)),
                hasNoEffects()));
  }

  @Test
  public void loadedTasksReplaceTheCurrentList() {
    TasksListModel stateWithSomeTasks = modelWithTasks(task("t1", true), task("t2"), task("t3"));

    ImmutableList<Task> receivedList = of(task("t4"), task("t5"));
    updateSpec
        .given(stateWithSomeTasks)
        .when(tasksLoaded(receivedList))
        .then(assertThatNext(hasModel(stateWithSomeTasks.withTasks(receivedList)), hasNoEffects()));
  }

  @Test
  public void loadedTasksReplaceTheCurrentNullList() {
    ImmutableList<Task> receivedList = of(task("t4"), task("t5"));
    updateSpec
        .given(TasksListModel.DEFAULT)
        .when(tasksLoaded(receivedList))
        .then(
            assertThatNext(
                hasModel(TasksListModel.DEFAULT.withTasks(receivedList)), hasNoEffects()));
  }

  @Test
  public void loadingEmptyTaskListLocallyWhileRefreshingShouldNotUpdateModel() {
    updateSpec
        .given(TasksListModel.DEFAULT.withLoading(true))
        .when(tasksLoaded(ImmutableList.of()))
        .then(assertThatNext(hasNothing()));
  }

  @Test
  public void loadingEmptyTaskListLocallyWhenNotRefreshingUpdatesModel() {
    updateSpec
        .given(TasksListModel.DEFAULT)
        .when(tasksLoaded(ImmutableList.of()))
        .then(
            assertThatNext(
                hasModel(TasksListModel.DEFAULT.withTasks(ImmutableList.of())), hasNoEffects()));
  }

  @Test
  public void createdTasksEndAtTailOfTheList() {
    TasksListModel stateWithSomeTasks = modelWithTasks(task("t1", true), task("t2"), task("t3"));

    updateSpec
        .given(stateWithSomeTasks)
        .when(taskCreated())
        .then(
            assertThatNext(
                hasNoModel(), hasEffects(showFeedback(FeedbackType.SAVED_SUCCESSFULLY))));
  }

  @Test
  public void clearingCompletedTasksShouldRemoveThemFromTheListAndDeleteThem() {
    TasksListModel stateWithSomeTasks =
        modelWithTasks(task("t1", true), task("t2", true), task("t3"));

    updateSpec
        .given(stateWithSomeTasks)
        .when(clearCompletedTasksRequested())
        .then(
            assertThatNext(
                hasModel(modelWithTasks(task("t3"))),
                hasEffects(
                    deleteTasks(task("t1", true), task("t2", true)),
                    showFeedback(FeedbackType.CLEARED_COMPLETED))));
  }

  @Test
  public void taskRefreshesReloadsTasksFromDatabase() {
    TasksListModel stateWithSomeTasks =
        modelWithTasks(task("t1", true), task("t2", true), task("t3"));
    updateSpec
        .given(stateWithSomeTasks)
        .when(tasksRefreshed())
        .then(
            assertThatNext(
                hasModel(stateWithSomeTasks.withLoading(false)), hasEffects(loadTasks())));
  }

  private TasksListEffect deleteTasks(Task... tasks) {
    return TasksListEffect.deleteTasks(ImmutableList.copyOf(tasks));
  }

  private TasksListModel modelWithTasks(Task... tasks) {
    return TasksListModel.builder().tasks(ImmutableList.copyOf(tasks)).build();
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
