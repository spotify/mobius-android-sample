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

import static com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksListEffect.deleteTasks;
import static com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksListEffect.loadTasks;
import static com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksListEffect.navigateToTaskDetails;
import static com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksListEffect.refreshTasks;
import static com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksListEffect.saveTask;
import static com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksListEffect.showFeedback;
import static com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksListEffect.startTaskCreationFlow;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.spotify.mobius.Effects.effects;
import static com.spotify.mobius.First.first;
import static com.spotify.mobius.Next.dispatch;
import static com.spotify.mobius.Next.next;
import static com.spotify.mobius.Next.noChange;

import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksListEvent.FilterSelected;
import com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksListEvent.NavigateToTaskDetailsRequested;
import com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksListEvent.TaskMarkedActive;
import com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksListEvent.TaskMarkedComplete;
import com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksListEvent.TasksLoaded;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.spotify.mobius.First;
import com.spotify.mobius.Next;
import io.reactivex.Observable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

public final class TasksListLogic {

  private TasksListLogic() {}

  @Nonnull
  public static First<TasksListModel, TasksListEffect> init(TasksListModel model) {
    if (model.tasks() == null) {
      return first(model.withLoading(true), effects(refreshTasks(), loadTasks()));
    } else {
      return first(model, effects(loadTasks()));
    }
  }

  @Nonnull
  public static Next<TasksListModel, TasksListEffect> update(
      TasksListModel model, TasksListEvent event) {
    return event.map(
        refreshRequest -> onRefreshRequested(model),
        newTaskClicked -> onNewTaskClicked(),
        navigateToTaskDetailsRequested ->
            onNavigateToTaskDetailsRequested(model, navigateToTaskDetailsRequested),
        taskCompleted -> onTaskCompleted(model, taskCompleted),
        taskActivated -> onTaskActivated(model, taskActivated),
        completedTasksCleared -> onCompletedTasksCleared(model),
        filterSelected -> onFilterSelected(model, filterSelected),
        tasksLoaded -> onTasksLoaded(model, tasksLoaded),
        taskCreated -> onTaskCreated(),
        tasksRefreshed -> onTasksRefreshed(model),
        tasksRefreshFailed -> onTasksRefreshFailed(model),
        tasksLoadingFailed -> onTasksLoadingFailed(model));
  }

  private static Next<TasksListModel, TasksListEffect> onRefreshRequested(TasksListModel model) {
    return next(model.withLoading(true), effects(refreshTasks()));
  }

  private static Next<TasksListModel, TasksListEffect> onNewTaskClicked() {
    return dispatch(effects(startTaskCreationFlow()));
  }

  private static Next<TasksListModel, TasksListEffect> onNavigateToTaskDetailsRequested(
      TasksListModel model, NavigateToTaskDetailsRequested event) {
    Optional<Task> task = model.findTaskById(event.taskId());
    if (!task.isPresent()) throw new IllegalStateException("Task does not exist");

    return dispatch(effects(navigateToTaskDetails(task.get())));
  }

  private static Next<TasksListModel, TasksListEffect> onTaskCompleted(
      TasksListModel model, TaskMarkedComplete event) {

    int taskIndex = model.findTaskIndexById(event.taskId());
    if (taskIndex < 0) throw new IllegalStateException("Task does not exist");
    Task updatedTask = checkNotNull(model.tasks()).get(taskIndex).complete();
    return updateTask(updatedTask, model, taskIndex, FeedbackType.MARKED_COMPLETE);
  }

  private static Next<TasksListModel, TasksListEffect> onTaskActivated(
      TasksListModel model, TaskMarkedActive event) {

    int taskIndex = model.findTaskIndexById(event.taskId());
    if (taskIndex < 0) throw new IllegalStateException("Task does not exist");
    Task updatedTask = checkNotNull(model.tasks()).get(taskIndex).activate();
    return updateTask(updatedTask, model, taskIndex, FeedbackType.MARKED_ACTIVE);
  }

  private static Next<TasksListModel, TasksListEffect> updateTask(
      Task updatedTask, TasksListModel model, int index, FeedbackType feedbackType) {
    return next(
        model.withTaskAtIndex(updatedTask, index),
        effects(saveTask(updatedTask), showFeedback(feedbackType)));
  }

  private static Next<TasksListModel, TasksListEffect> onCompletedTasksCleared(
      TasksListModel model) {
    ImmutableList<Task> allTasks = checkNotNull(model.tasks());
    List<Task> completedTasks =
        Observable.fromIterable(allTasks)
            .filter(t -> t.details().completed())
            .toList()
            .blockingGet();

    if (completedTasks.isEmpty()) return noChange();

    ArrayList<Task> newTasks = new ArrayList<>(allTasks);
    newTasks.removeAll(completedTasks);
    return next(
        model.withTasks(ImmutableList.copyOf(newTasks)),
        effects(
            deleteTasks(ImmutableList.copyOf(completedTasks)),
            showFeedback(FeedbackType.CLEARED_COMPLETED)));
  }

  private static Next<TasksListModel, TasksListEffect> onFilterSelected(
      TasksListModel model, FilterSelected event) {
    return next(model.withTasksFilter(event.filterType()));
  }

  private static Next<TasksListModel, TasksListEffect> onTasksLoaded(
      TasksListModel model, TasksLoaded event) {
    if (model.loading() && event.tasks().isEmpty()) {
      return noChange();
    }

    return event.tasks().equals(model.tasks()) ? noChange() : next(model.withTasks(event.tasks()));
  }

  private static Next<TasksListModel, TasksListEffect> onTaskCreated() {
    return dispatch(effects(showFeedback(FeedbackType.SAVED_SUCCESSFULLY)));
  }

  private static Next<TasksListModel, TasksListEffect> onTasksRefreshed(TasksListModel model) {
    return next(model.withLoading(false), effects(loadTasks()));
  }

  private static Next<TasksListModel, TasksListEffect> onTasksRefreshFailed(TasksListModel model) {
    return next(model.withLoading(false), effects(showFeedback(FeedbackType.LOADING_ERROR)));
  }

  private static Next<TasksListModel, TasksListEffect> onTasksLoadingFailed(TasksListModel model) {
    return next(model.withLoading(false), effects(showFeedback(FeedbackType.LOADING_ERROR)));
  }
}
