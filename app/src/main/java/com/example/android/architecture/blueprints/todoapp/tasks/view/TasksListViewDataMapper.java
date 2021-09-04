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
package com.example.android.architecture.blueprints.todoapp.tasks.view;

import static com.example.android.architecture.blueprints.todoapp.tasks.view.EmptyTasksViewDataMapper.createEmptyTaskViewData;
import static com.example.android.architecture.blueprints.todoapp.tasks.view.ViewState.awaitingTasks;
import static com.example.android.architecture.blueprints.todoapp.tasks.view.ViewState.emptyTasks;
import static com.example.android.architecture.blueprints.todoapp.tasks.view.ViewState.hasTasks;
import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Lists.transform;

import androidx.annotation.Nullable;
import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.tasks.TasksFragment;
import com.example.android.architecture.blueprints.todoapp.tasks.domain.TaskFilters;
import com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksFilterType;
import com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksListModel;
import com.google.common.collect.ImmutableList;

/**
 * Listens to user actions from the UI ({@link TasksFragment}), retrieves the data and updates the
 * UI as required.
 */
public class TasksListViewDataMapper {

  public static TasksListViewData tasksListModelToViewData(TasksListModel model) {
    return TasksListViewData.builder()
        .loading(model.loading())
        .filterLabel(getFilterLabel(model.filter()))
        .viewState(getViewState(model.tasks(), model.filter()))
        .build();
  }

  private static ViewState getViewState(
      @Nullable ImmutableList<Task> tasks, TasksFilterType filter) {
    if (tasks == null) return awaitingTasks();

    ImmutableList<Task> filteredTasks = TaskFilters.filterTasks(tasks, filter);
    if (filteredTasks.isEmpty()) {
      return emptyTasks(createEmptyTaskViewData(filter));
    } else {
      return hasTasks(copyOf(transform(filteredTasks, TaskViewDataMapper::createTaskViewData)));
    }
  }

  private static int getFilterLabel(TasksFilterType filterType) {
    switch (filterType) {
      case ACTIVE_TASKS:
        return R.string.label_active;
      case COMPLETED_TASKS:
        return R.string.label_completed;
      default:
        return R.string.label_all;
    }
  }
}
