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
@file:JvmName("AddEditTaskEffectHandlers")

package com.example.android.architecture.blueprints.todoapp.addedittask.effecthandlers

import android.content.Context
import com.example.android.architecture.blueprints.todoapp.addedittask.domain.*
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.local.TasksLocalDataSource
import com.example.android.architecture.blueprints.todoapp.data.source.remote.TasksRemoteDataSource
import com.example.android.architecture.blueprints.todoapp.util.schedulers.SchedulerProvider
import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.functions.Action
import java.util.*

fun createEffectHandlers(context: Context,
                         showTasksList: Action,
                         showEmptyTaskError: Action):
        ObservableTransformer<Effect, Event> {

    val taskSaver = createTaskSaver(context)
    return RxMobius.subtypeEffectHandler<Effect, Event>()
            .addAction(NotifyEmptyTaskNotAllowed::class.java, showEmptyTaskError, mainThread())
            .addAction(Exit::class.java, showTasksList, mainThread())
            .addFunction(CreateTask::class.java, createTaskHandler(taskSaver))
            .addFunction(SaveTask::class.java, saveTaskHandler(taskSaver))
            .build()
}

internal fun createTaskHandler(taskSaver: (Task) -> AddEditTaskEvent): (CreateTask) -> Event = {
    val task = Task(UUID.randomUUID().toString(), it.taskDetails)
    val result = taskSaver(task)
    if (result is TaskUpdatedSuccessfully) TaskCreatedSuccessfully else result
}

internal fun saveTaskHandler(taskSaver: (Task) -> AddEditTaskEvent): (SaveTask) -> AddEditTaskEvent = {
    taskSaver(it.task)
}

internal fun createTaskSaver(context: Context): (Task) -> AddEditTaskEvent {
    val remoteSource = TasksRemoteDataSource.getInstance()
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


