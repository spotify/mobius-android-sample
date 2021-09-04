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

import static com.google.common.base.Preconditions.checkNotNull;

import androidx.annotation.Nullable;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.google.auto.value.AutoValue;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;

@AutoValue
public abstract class TasksListModel {
  public static final TasksListModel DEFAULT = TasksListModel.builder().build();

  @Nullable
  public abstract ImmutableList<Task> tasks();

  public abstract TasksFilterType filter();

  public abstract boolean loading();

  public int findTaskIndexById(String id) {
    ImmutableList<Task> tasks = checkNotNull(tasks());
    int taskIndex = -1;
    for (int i = 0; i < tasks.size(); i++) {
      if (tasks.get(i).id().equals(id)) {
        taskIndex = i;
        break;
      }
    }
    return taskIndex;
  }

  public Optional<Task> findTaskById(String id) {
    int taskIndex = findTaskIndexById(id);
    if (taskIndex < 0) return Optional.absent();
    return Optional.of((checkNotNull(tasks()).get(taskIndex)));
  }

  public TasksListModel withTasks(ImmutableList<Task> tasks) {
    return toBuilder().tasks(tasks).build();
  }

  public TasksListModel withLoading(boolean loading) {
    return toBuilder().loading(loading).build();
  }

  public TasksListModel withTasksFilter(TasksFilterType tasksFilter) {
    return toBuilder().filter(tasksFilter).build();
  }

  public TasksListModel withTaskAtIndex(Task task, int index) {
    ImmutableList<Task> tasks = checkNotNull(tasks());
    assertIndexWithinBounds(index, tasks);

    ArrayList<Task> copy = new ArrayList<>(tasks);
    copy.set(index, task);
    return withTasks(ImmutableList.copyOf(copy));
  }

  public abstract Builder toBuilder();

  public static Builder builder() {
    return new AutoValue_TasksListModel.Builder().loading(false).filter(TasksFilterType.ALL_TASKS);
  }

  static void assertIndexWithinBounds(int index, List<?> items) {
    if (index < 0 || index >= items.size())
      throw new IllegalArgumentException("Index out of bounds");
  }

  @AutoValue
  public abstract static class TaskEntry {
    public abstract int index();

    public abstract Task task();

    public static TaskEntry create(int index, Task task) {
      return new AutoValue_TasksListModel_TaskEntry(index, task);
    }
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder tasks(ImmutableList<Task> tasks);

    public abstract Builder filter(TasksFilterType filter);

    public abstract Builder loading(boolean loading);

    public abstract TasksListModel build();
  }
}
