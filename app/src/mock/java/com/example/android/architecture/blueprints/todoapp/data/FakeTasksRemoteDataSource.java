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
package com.example.android.architecture.blueprints.todoapp.data;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.google.common.base.Optional;
import io.reactivex.Flowable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Implementation of a remote data source with static access to the data for easy testing. */
public class FakeTasksRemoteDataSource implements TasksDataSource {

  private static FakeTasksRemoteDataSource INSTANCE;

  private static final Map<String, Task> TASKS_SERVICE_DATA = new LinkedHashMap<>();

  // Prevent direct instantiation.
  private FakeTasksRemoteDataSource() {}

  public static FakeTasksRemoteDataSource getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new FakeTasksRemoteDataSource();
    }
    return INSTANCE;
  }

  @Override
  public Flowable<List<Task>> getTasks() {
    Collection<Task> values = TASKS_SERVICE_DATA.values();
    return Flowable.fromIterable(values).toList().toFlowable();
  }

  @Override
  public Flowable<Optional<Task>> getTask(@NonNull String taskId) {
    Task task = TASKS_SERVICE_DATA.get(taskId);
    return Flowable.just(Optional.of(task));
  }

  @Override
  public void saveTask(@NonNull Task task) {
    TASKS_SERVICE_DATA.put(task.id(), task);
  }

  @Override
  public void deleteTask(@NonNull String taskId) {
    TASKS_SERVICE_DATA.remove(taskId);
  }

  @Override
  public void deleteAllTasks() {
    TASKS_SERVICE_DATA.clear();
  }

  @VisibleForTesting
  public void addTasks(Task... tasks) {
    for (Task task : tasks) {
      TASKS_SERVICE_DATA.put(task.id(), task);
    }
  }
}
