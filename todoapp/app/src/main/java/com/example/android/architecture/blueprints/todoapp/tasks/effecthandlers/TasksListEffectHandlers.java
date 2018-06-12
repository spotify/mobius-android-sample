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
package com.example.android.architecture.blueprints.todoapp.tasks.effecthandlers;

import static com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksListEvent.tasksLoaded;
import static com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksListEvent.tasksLoadingFailed;
import static com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksListEvent.tasksRefreshed;
import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;

import android.content.Context;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.local.TasksLocalDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.remote.TasksRemoteDataSource;
import com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksListEffect;
import com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksListEffect.DeleteTasks;
import com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksListEffect.LoadTasks;
import com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksListEffect.NavigateToTaskDetails;
import com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksListEffect.RefreshTasks;
import com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksListEffect.SaveTask;
import com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksListEffect.ShowFeedback;
import com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksListEffect.StartTaskCreationFlow;
import com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksListEvent;
import com.example.android.architecture.blueprints.todoapp.tasks.view.TasksListViewActions;
import com.example.android.architecture.blueprints.todoapp.util.Either;
import com.example.android.architecture.blueprints.todoapp.util.schedulers.SchedulerProvider;
import com.google.common.collect.ImmutableList;
import com.spotify.mobius.rx2.RxMobius;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.Single;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import java.util.List;

public class TasksListEffectHandlers {

  public static ObservableTransformer<TasksListEffect, TasksListEvent> createEffectHandler(
      Context context,
      TasksListViewActions view,
      Action showAddTask,
      Consumer<Task> showTaskDetails) {

    TasksRemoteDataSource remoteSource = TasksRemoteDataSource.getInstance();
    TasksLocalDataSource localSource =
        TasksLocalDataSource.getInstance(context, SchedulerProvider.getInstance());

    return RxMobius.<TasksListEffect, TasksListEvent>subtypeEffectHandler()
        .addTransformer(RefreshTasks.class, refreshTasksHandler(remoteSource, localSource))
        .addTransformer(LoadTasks.class, loadTasksHandler(localSource))
        .addConsumer(SaveTask.class, saveTaskHandler(remoteSource, localSource))
        .addConsumer(DeleteTasks.class, deleteTasksHandler(remoteSource, localSource))
        .addConsumer(ShowFeedback.class, showFeedbackHandler(view), mainThread())
        .addConsumer(
            NavigateToTaskDetails.class, navigateToDetailsHandler(showTaskDetails), mainThread())
        .addAction(StartTaskCreationFlow.class, showAddTask, mainThread())
        .build();
  }

  static ObservableTransformer<RefreshTasks, TasksListEvent> refreshTasksHandler(
      TasksDataSource remoteSource, TasksDataSource localSource) {
    Single<TasksListEvent> refreshTasksOperation =
        remoteSource
            .getTasks()
            .singleOrError()
            .map(Either::<Throwable, List<Task>>right)
            .onErrorReturn(Either::left)
            .flatMap(
                either ->
                    either.map(
                        left -> Single.just(tasksLoadingFailed()),
                        right ->
                            Observable.fromIterable(right.value())
                                .concatMapCompletable(
                                    t -> Completable.fromAction(() -> localSource.saveTask(t)))
                                .andThen(Single.just(tasksRefreshed()))
                                .onErrorReturnItem(tasksLoadingFailed())));

    return refreshTasks -> refreshTasks.flatMapSingle(__ -> refreshTasksOperation);
  }

  static ObservableTransformer<LoadTasks, TasksListEvent> loadTasksHandler(
      TasksDataSource dataSource) {
    return loadTasks ->
        loadTasks.flatMap(
            effect ->
                dataSource
                    .getTasks()
                    .toObservable()
                    .take(1)
                    .map(tasks -> tasksLoaded(ImmutableList.copyOf(tasks)))
                    .onErrorReturnItem(tasksLoadingFailed()));
  }

  static Consumer<SaveTask> saveTaskHandler(
      TasksDataSource remoteSource, TasksDataSource localSource) {
    return saveTaskEffect -> {
      remoteSource.saveTask(saveTaskEffect.task());
      localSource.saveTask(saveTaskEffect.task());
    };
  }

  static Consumer<DeleteTasks> deleteTasksHandler(
      TasksDataSource remoteSource, TasksDataSource localSource) {
    return deleteTasks -> {
      for (Task task : deleteTasks.tasks()) {
        remoteSource.deleteTask(task.id());
        localSource.deleteTask(task.id());
      }
    };
  }

  static Consumer<ShowFeedback> showFeedbackHandler(TasksListViewActions view) {
    return showFeedback -> {
      switch (showFeedback.feedbackType()) {
        case SAVED_SUCCESSFULLY:
          view.showSuccessfullySavedMessage();
          break;
        case MARKED_ACTIVE:
          view.showTaskMarkedActive();
          break;
        case MARKED_COMPLETE:
          view.showTaskMarkedComplete();
          break;
        case CLEARED_COMPLETED:
          view.showCompletedTasksCleared();
          break;
        case LOADING_ERROR:
          view.showLoadingTasksError();
          break;
      }
    };
  }

  static Consumer<NavigateToTaskDetails> navigateToDetailsHandler(Consumer<Task> command) {
    return navigationEffect -> command.accept(navigationEffect.task());
  }
}
