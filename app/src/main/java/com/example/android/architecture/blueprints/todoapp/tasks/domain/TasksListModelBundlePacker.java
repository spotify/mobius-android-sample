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

import static com.example.android.architecture.blueprints.todoapp.data.TaskBundlePacker.taskFromBundle;
import static com.example.android.architecture.blueprints.todoapp.data.TaskBundlePacker.taskToBundle;

import android.os.Bundle;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;

public class TasksListModelBundlePacker {
  private static class TasksListModelBundleIdentifiers {
    static final String FILTER = "model_filter";
    static final String LOADING = "model_loading";
    static final String TASKS = "model_tasks";
  }

  public static Bundle tasksListModelToBundle(TasksListModel tasksListModel) {
    Bundle b = new Bundle();
    b.putSerializable(TasksListModelBundleIdentifiers.FILTER, tasksListModel.filter());
    b.putBoolean(TasksListModelBundleIdentifiers.LOADING, tasksListModel.loading());
    ImmutableList<Task> tasks = tasksListModel.tasks();
    if (tasks != null) {
      ArrayList<Bundle> taskBundles = new ArrayList<>();
      for (Task task : tasks) {
        taskBundles.add(taskToBundle(task));
      }
      b.putParcelableArrayList(TasksListModelBundleIdentifiers.TASKS, taskBundles);
    }
    return b;
  }

  public static TasksListModel tasksListModelFromBundle(Bundle b) {
    TasksListModel.Builder builder =
        TasksListModel.builder()
            .filter((TasksFilterType) b.getSerializable(TasksListModelBundleIdentifiers.FILTER))
            .loading(b.getBoolean(TasksListModelBundleIdentifiers.LOADING));

    ArrayList<Bundle> bundles = b.getParcelableArrayList(TasksListModelBundleIdentifiers.TASKS);
    if (bundles == null) return builder.build();

    ImmutableList.Builder<Task> tasks = ImmutableList.builder();
    for (Bundle bundle : bundles) {
      tasks.add(taskFromBundle(bundle));
    }
    return builder.tasks(tasks.build()).build();
  }
}
