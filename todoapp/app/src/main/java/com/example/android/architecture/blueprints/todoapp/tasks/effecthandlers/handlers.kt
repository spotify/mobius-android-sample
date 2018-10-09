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
package com.example.android.architecture.blueprints.todoapp.tasks.effecthandlers

import android.content.Context
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource
import com.example.android.architecture.blueprints.todoapp.data.source.local.TasksLocalDataSource
import com.example.android.architecture.blueprints.todoapp.data.source.remote.TasksRemoteDataSource
import com.example.android.architecture.blueprints.todoapp.tasks.domain.*
import com.example.android.architecture.blueprints.todoapp.tasks.domain.FeedbackType.*
import com.example.android.architecture.blueprints.todoapp.tasks.view.TasksListViewActions
import com.example.android.architecture.blueprints.todoapp.util.Either
import com.example.android.architecture.blueprints.todoapp.util.SubtypeEffectHandlerBuilder
import com.example.android.architecture.blueprints.todoapp.util.schedulers.SchedulerProvider
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread

fun createEffectHandler(
        context: Context,
        view: TasksListViewActions,
        showAddTask: () -> Unit,
        showTaskDetails: (Task) -> Unit): ObservableTransformer<TasksListEffect, TasksListEvent> {

    val remoteSource = TasksRemoteDataSource
    val localSource = TasksLocalDataSource.getInstance(context, SchedulerProvider.getInstance())

    return SubtypeEffectHandlerBuilder<TasksListEffect, TasksListEvent>()
            .addTransformer<RefreshTasks>(refreshTasksHandler(remoteSource, localSource))
            .addTransformer<LoadTasks>(loadTasksHandler(localSource))
            .addConsumer<SaveTask>(saveTaskHandler(remoteSource, localSource))
            .addConsumer<DeleteTasks>(deleteTasksHandler(remoteSource, localSource))
            .addConsumer<ShowFeedback>(showFeedbackHandler(view), mainThread())
            .addConsumer<NavigateToTaskDetails>(navigateToDetailsHandler(showTaskDetails), mainThread())
            .addAction<StartTaskCreationFlow>(showAddTask, mainThread())
            .build()
}

internal fun refreshTasksHandler(
        remoteSource: TasksDataSource, localSource: TasksDataSource)
        : ObservableTransformer<RefreshTasks, TasksListEvent> =
    ObservableTransformer {
        it.flatMapSingle {
            remoteSource
                    .getTasks()
                    .singleOrError()
                    .map { it.toList() }
                    .map { Either.right<Throwable, List<Task>>(it) }
                    .onErrorReturn { Either.Left(it) }
                    .flatMap {
                        when(it) {
                            is Either.Left -> Single.just(TasksLoadingFailed)
                            is Either.Right -> Observable.fromIterable(it.value)
                                    .concatMapCompletable { localSource.save(it) }
                                    .andThen(Single.just<TasksListEvent>(TasksRefreshed))
                                    .onErrorReturnItem(TasksLoadingFailed)
                        }
                    }
        }
    }

private fun TasksDataSource.save(task: Task) = Completable.fromAction { saveTask(task) }

internal fun loadTasksHandler(dataSource: TasksDataSource)
        : ObservableTransformer<LoadTasks, TasksListEvent> =
    ObservableTransformer {
        it.flatMap {
            dataSource
                    .getTasks()
                    .toObservable()
                    .take(1)
                    .map<TasksListEvent> { TasksLoaded(it) }
                    .onErrorReturnItem(TasksLoadingFailed)
        }
    }

internal fun saveTaskHandler(
        remoteSource: TasksDataSource, localSource: TasksDataSource): (SaveTask) -> Unit =
    {
        remoteSource.saveTask(it.task)
        localSource.saveTask(it.task)
    }


internal fun deleteTasksHandler(
        remoteSource: TasksDataSource, localSource: TasksDataSource): (DeleteTasks) -> Unit =
    {
        it.tasks.forEach {
            remoteSource.deleteTask(it.id)
            localSource.deleteTask(it.id)
        }
    }


internal fun showFeedbackHandler(view: TasksListViewActions): (ShowFeedback) -> Unit =
    {
        when (it.type) {
            SAVED_SUCCESSFULLY -> view.showSuccessfullySavedMessage()
            MARKED_ACTIVE -> view.showTaskMarkedActive()
            MARKED_COMPLETE -> view.showTaskMarkedComplete()
            CLEARED_COMPLETED -> view.showCompletedTasksCleared()
            LOADING_ERROR -> view.showLoadingTasksError()
        }
    }


internal fun navigateToDetailsHandler(block: (Task) -> Unit): (NavigateToTaskDetails) -> Unit =
    { block(it.task) }
