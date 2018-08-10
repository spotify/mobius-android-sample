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
@file:JvmName("TaskDetailEffectHandlers")

package com.example.android.architecture.blueprints.todoapp.taskdetail.effecthandlers

import android.content.Context
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.local.TasksLocalDataSource
import com.example.android.architecture.blueprints.todoapp.data.source.remote.TasksRemoteDataSource
import com.example.android.architecture.blueprints.todoapp.taskdetail.domain.*
import com.example.android.architecture.blueprints.todoapp.taskdetail.view.TaskDetailViews
import com.example.android.architecture.blueprints.todoapp.util.SubtypeEffectHandlerBuilder
import com.example.android.architecture.blueprints.todoapp.util.schedulers.SchedulerProvider
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer

fun createEffectHandlers(view: TaskDetailViews, context: Context, dismiss: Action, launchEditor: Consumer<Task>): ObservableTransformer<TaskDetailEffect, TaskDetailEvent> {
    val remoteSource = TasksRemoteDataSource.getInstance()
    val localSource = TasksLocalDataSource.getInstance(context, SchedulerProvider.getInstance())

    return SubtypeEffectHandlerBuilder<TaskDetailEffect, TaskDetailEvent>()
            .addFunction<DeleteTask>(deleteTaskHandler(remoteSource, localSource))
            .addFunction<SaveTask>(saveTaskHandler(remoteSource, localSource))
            .addAction<NotifyTaskMarkedComplete>(view::showTaskMarkedComplete, mainThread())
            .addAction<NotifyTaskMarkedActive>(view::showTaskMarkedActive, mainThread())
            .addAction<NotifyTaskDeletionFailed>(view::showTaskDeletionFailed, mainThread())
            .addAction<NotifyTaskSaveFailed>(view::showTaskSavingFailed, mainThread())
            .addConsumer<OpenTaskEditor>(openTaskEditorHandler(launchEditor), mainThread())
            .addAction<Exit>({dismiss.run()}, mainThread())
            .build()
}

private fun openTaskEditorHandler(launchEditorCommand: Consumer<Task>): (OpenTaskEditor) -> Unit =
        {
            launchEditorCommand.accept(it.task)
        }


private fun saveTaskHandler(remoteSource: TasksRemoteDataSource, localSource: TasksLocalDataSource): (SaveTask) -> TaskDetailEvent =
        {
            try {
                remoteSource.saveTask(it.task)
                localSource.saveTask(it.task)
                if (it.task.details.completed) TaskMarkedComplete else TaskMarkedActive
            } catch (e: Exception) {
                TaskSaveFailed
            }
        }

private fun deleteTaskHandler(remoteSource: TasksRemoteDataSource, localSource: TasksLocalDataSource): (DeleteTask) -> TaskDetailEvent =
        {
            try {
                remoteSource.deleteTask(it.task.id)
                localSource.deleteTask(it.task.id)
                TaskDeleted
            } catch (e: Exception) {
                TaskDeletionFailed
            }
        }
