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
package com.example.android.architecture.blueprints.todoapp.addedittask.effecthandlers;

import static com.example.android.architecture.blueprints.todoapp.addedittask.domain.AddEditTaskEvent.taskCreatedSuccessfully;
import static com.example.android.architecture.blueprints.todoapp.addedittask.domain.AddEditTaskEvent.taskCreationFailed;
import static com.example.android.architecture.blueprints.todoapp.addedittask.domain.AddEditTaskEvent.taskUpdatedSuccessfully;
import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;

import android.content.Context;
import com.example.android.architecture.blueprints.todoapp.addedittask.domain.AddEditTaskEffect;
import com.example.android.architecture.blueprints.todoapp.addedittask.domain.AddEditTaskEffect.CreateTask;
import com.example.android.architecture.blueprints.todoapp.addedittask.domain.AddEditTaskEffect.Exit;
import com.example.android.architecture.blueprints.todoapp.addedittask.domain.AddEditTaskEffect.NotifyEmptyTaskNotAllowed;
import com.example.android.architecture.blueprints.todoapp.addedittask.domain.AddEditTaskEffect.SaveTask;
import com.example.android.architecture.blueprints.todoapp.addedittask.domain.AddEditTaskEvent;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.local.TasksLocalDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.remote.TasksRemoteDataSource;
import com.example.android.architecture.blueprints.todoapp.util.schedulers.SchedulerProvider;
import com.spotify.mobius.rx2.RxMobius;
import io.reactivex.ObservableTransformer;
import io.reactivex.functions.Action;
import io.reactivex.functions.Function;
import java.util.UUID;

public class AddEditTaskEffectHandlers {

  public static ObservableTransformer<AddEditTaskEffect, AddEditTaskEvent> createEffectHandlers(
      Context context, Action showTasksList, Action showEmptyTaskError) {
    TasksRemoteDataSource remoteSource = TasksRemoteDataSource.getInstance();
    TasksLocalDataSource localSource =
        TasksLocalDataSource.getInstance(context, SchedulerProvider.getInstance());

    return RxMobius.<AddEditTaskEffect, AddEditTaskEvent>subtypeEffectHandler()
        .addAction(NotifyEmptyTaskNotAllowed.class, showEmptyTaskError, mainThread())
        .addAction(Exit.class, showTasksList, mainThread())
        .addFunction(CreateTask.class, createTaskHandler(remoteSource, localSource))
        .addFunction(SaveTask.class, saveTaskHandler(remoteSource, localSource))
        .build();
  }

  static Function<CreateTask, AddEditTaskEvent> createTaskHandler(
      TasksDataSource remoteSource, TasksDataSource localSource) {

    return createTaskEffect -> {
      Task task = Task.create(UUID.randomUUID().toString(), createTaskEffect.taskDetails());
      try {
        remoteSource.saveTask(task);
        localSource.saveTask(task);
        return taskCreatedSuccessfully();
      } catch (Exception e) {
        return taskCreationFailed("Failed to create task");
      }
    };
  }

  static Function<SaveTask, AddEditTaskEvent> saveTaskHandler(
      TasksDataSource remoteSource, TasksDataSource localSource) {
    return saveTasks -> {
      try {
        remoteSource.saveTask(saveTasks.task());
        localSource.saveTask(saveTasks.task());
        return taskUpdatedSuccessfully();
      } catch (Exception e) {
        return taskCreationFailed("Failed to update task");
      }
    };
  }
}
