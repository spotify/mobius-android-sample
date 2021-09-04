/*
 * Copyright 2016, The Android Open Source Project
 * Copyright (c) 2017-2018 Spotify AB
 *
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
 */
package com.example.android.architecture.blueprints.todoapp.data.source.remote;

import androidx.annotation.NonNull;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.TaskDetails;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.google.common.base.Optional;
import io.reactivex.Flowable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/** Implementation of the data source that adds a latency simulating network. */
public class TasksRemoteDataSource implements TasksDataSource {

  private static TasksRemoteDataSource INSTANCE;

  private static final int SERVICE_LATENCY_IN_MILLIS = 3000;

  private static final Map<String, Task> TASKS_SERVICE_DATA;

  static {
    TASKS_SERVICE_DATA = new LinkedHashMap<>(2);
    addTask("1234", "Build tower in Pisa", "Ground looks good, no foundation work required.");
    addTask("4321", "Finish bridge in Tacoma", "Found awesome girders at half the cost!");
  }

  public static TasksRemoteDataSource getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new TasksRemoteDataSource();
    }
    return INSTANCE;
  }

  // Prevent direct instantiation.
  private TasksRemoteDataSource() {}

  private static void addTask(String id, String title, String description) {
    Task newTask = Task.create(id, TaskDetails.create(title, description, false));
    TASKS_SERVICE_DATA.put(newTask.id(), newTask);
  }

  @Override
  public Flowable<List<Task>> getTasks() {
    return Flowable.fromIterable(TASKS_SERVICE_DATA.values())
        .delay(SERVICE_LATENCY_IN_MILLIS, TimeUnit.MILLISECONDS)
        .toList()
        .toFlowable();
  }

  @Override
  public Flowable<Optional<Task>> getTask(@NonNull String taskId) {
    final Task task = TASKS_SERVICE_DATA.get(taskId);
    if (task != null) {
      return Flowable.just(Optional.of(task))
          .delay(SERVICE_LATENCY_IN_MILLIS, TimeUnit.MILLISECONDS);
    } else {
      return Flowable.empty();
    }
  }

  @Override
  public void saveTask(@NonNull Task task) {
    TASKS_SERVICE_DATA.put(task.id(), task);
  }

  @Override
  public void deleteAllTasks() {
    TASKS_SERVICE_DATA.clear();
  }

  @Override
  public void deleteTask(@NonNull String taskId) {
    TASKS_SERVICE_DATA.remove(taskId);
  }
}
