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
package com.example.android.architecture.blueprints.todoapp.addedittask.effecthandlers

import android.content.Context
import com.example.android.architecture.blueprints.todoapp.addedittask.domain.*
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.local.TasksLocalDataSource
import com.example.android.architecture.blueprints.todoapp.data.source.remote.TasksRemoteDataSource
import com.example.android.architecture.blueprints.todoapp.util.SubtypeEffectHandlerBuilder
import com.example.android.architecture.blueprints.todoapp.util.schedulers.SchedulerProvider
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import java.util.*

fun createEffectHandlers(context: Context,
                         showTasksList: () -> Unit,
                         showEmptyTaskError: () -> Unit):
        ObservableTransformer<AddEditTaskEffect, AddEditTaskEvent> {

    val taskSaver = createTaskSaver(context)

    return SubtypeEffectHandlerBuilder<AddEditTaskEffect, AddEditTaskEvent>()
            .addAction<NotifyEmptyTaskNotAllowed>(showEmptyTaskError, mainThread())
            .addAction<Exit>(showTasksList, mainThread())
            .addFunction<CreateTask>(createTaskHandler(taskSaver))
            .addFunction<SaveTask>(saveTaskHandler(taskSaver))
            .build()
}

internal fun createTaskHandler(taskSaver: (Task) -> AddEditTaskEvent): (CreateTask) -> AddEditTaskEvent = {
    val task = Task(UUID.randomUUID().toString(), it.taskDetails)
    val result = taskSaver(task)
    if (result is TaskUpdatedSuccessfully) TaskCreatedSuccessfully else result
}

internal fun saveTaskHandler(taskSaver: (Task) -> AddEditTaskEvent): (SaveTask) -> AddEditTaskEvent = {
    taskSaver(it.task)
}

internal fun createTaskSaver(context: Context): (Task) -> AddEditTaskEvent {
    val remoteSource = TasksRemoteDataSource
    val localSource = TasksLocalDataSource.getInstance(context, SchedulerProvider.getInstance())

    return {
        try {
            remoteSource.saveTask(it)
            localSource.saveTask(it)
            TaskUpdatedSuccessfully
        } catch (e: Exception) {
            TaskCreationFailed("Failed to update task")
        }
    }
}


