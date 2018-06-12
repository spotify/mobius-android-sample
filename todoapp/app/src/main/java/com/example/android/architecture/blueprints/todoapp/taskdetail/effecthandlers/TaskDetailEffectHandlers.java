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
package com.example.android.architecture.blueprints.todoapp.taskdetail.effecthandlers;

import static com.example.android.architecture.blueprints.todoapp.taskdetail.domain.TaskDetailEvent.taskDeleted;
import static com.example.android.architecture.blueprints.todoapp.taskdetail.domain.TaskDetailEvent.taskDeletionFailed;
import static com.example.android.architecture.blueprints.todoapp.taskdetail.domain.TaskDetailEvent.taskMarkedActive;
import static com.example.android.architecture.blueprints.todoapp.taskdetail.domain.TaskDetailEvent.taskMarkedComplete;
import static com.example.android.architecture.blueprints.todoapp.taskdetail.domain.TaskDetailEvent.taskSaveFailed;
import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;

import android.content.Context;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.local.TasksLocalDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.remote.TasksRemoteDataSource;
import com.example.android.architecture.blueprints.todoapp.taskdetail.domain.TaskDetailEffect;
import com.example.android.architecture.blueprints.todoapp.taskdetail.domain.TaskDetailEffect.DeleteTask;
import com.example.android.architecture.blueprints.todoapp.taskdetail.domain.TaskDetailEffect.Exit;
import com.example.android.architecture.blueprints.todoapp.taskdetail.domain.TaskDetailEffect.NotifyTaskDeletionFailed;
import com.example.android.architecture.blueprints.todoapp.taskdetail.domain.TaskDetailEffect.NotifyTaskMarkedActive;
import com.example.android.architecture.blueprints.todoapp.taskdetail.domain.TaskDetailEffect.NotifyTaskMarkedComplete;
import com.example.android.architecture.blueprints.todoapp.taskdetail.domain.TaskDetailEffect.NotifyTaskSaveFailed;
import com.example.android.architecture.blueprints.todoapp.taskdetail.domain.TaskDetailEffect.OpenTaskEditor;
import com.example.android.architecture.blueprints.todoapp.taskdetail.domain.TaskDetailEffect.SaveTask;
import com.example.android.architecture.blueprints.todoapp.taskdetail.domain.TaskDetailEvent;
import com.example.android.architecture.blueprints.todoapp.taskdetail.view.TaskDetailViewActions;
import com.example.android.architecture.blueprints.todoapp.util.schedulers.SchedulerProvider;
import com.spotify.mobius.rx2.RxMobius;
import io.reactivex.ObservableTransformer;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

public class TaskDetailEffectHandlers {

  public static ObservableTransformer<TaskDetailEffect, TaskDetailEvent> createEffectHandlers(
      TaskDetailViewActions view, Context context, Action dismiss, Consumer<Task> launchEditor) {

    TasksRemoteDataSource remoteSource = TasksRemoteDataSource.getInstance();
    TasksLocalDataSource localSource =
        TasksLocalDataSource.getInstance(context, SchedulerProvider.getInstance());
    return RxMobius.<TaskDetailEffect, TaskDetailEvent>subtypeEffectHandler()
        .addFunction(DeleteTask.class, deleteTaskHandler(remoteSource, localSource))
        .addFunction(SaveTask.class, saveTaskHandler(remoteSource, localSource))
        .addAction(NotifyTaskMarkedComplete.class, view::showTaskMarkedComplete, mainThread())
        .addAction(NotifyTaskMarkedActive.class, view::showTaskMarkedActive, mainThread())
        .addAction(NotifyTaskDeletionFailed.class, view::showTaskDeletionFailed, mainThread())
        .addAction(NotifyTaskSaveFailed.class, view::showTaskSavingFailed, mainThread())
        .addConsumer(OpenTaskEditor.class, openTaskEditorHandler(launchEditor), mainThread())
        .addAction(Exit.class, dismiss, mainThread())
        .build();
  }

  private static Consumer<OpenTaskEditor> openTaskEditorHandler(
      Consumer<Task> launchEditorCommand) {
    return openEditorEffect -> launchEditorCommand.accept(openEditorEffect.task());
  }

  private static Function<SaveTask, TaskDetailEvent> saveTaskHandler(
      TasksDataSource remoteSource, TasksDataSource localSource) {
    return saveTask -> {
      try {
        remoteSource.saveTask(saveTask.task());
        localSource.saveTask(saveTask.task());
        return saveTask.task().details().completed() ? taskMarkedComplete() : taskMarkedActive();
      } catch (Exception e) {
        return taskSaveFailed();
      }
    };
  }

  private static Function<DeleteTask, TaskDetailEvent> deleteTaskHandler(
      TasksDataSource remoteSource, TasksDataSource localSource) {
    return deleteTask -> {
      try {
        remoteSource.deleteTask(deleteTask.task().id());
        localSource.deleteTask(deleteTask.task().id());
        return taskDeleted();
      } catch (Exception e) {
        return taskDeletionFailed();
      }
    };
  }
}
