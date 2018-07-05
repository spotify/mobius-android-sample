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
@file:JvmName("AddEditTaskLogic")

package com.example.android.architecture.blueprints.todoapp.addedittask.domain

import com.example.android.architecture.blueprints.todoapp.addedittask.domain.AddEditTaskModel.Mode.Add
import com.example.android.architecture.blueprints.todoapp.addedittask.domain.AddEditTaskModel.Mode.Edit
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.spotify.mobius.Effects.effects
import com.spotify.mobius.Next
import com.spotify.mobius.Next.dispatch
import com.spotify.mobius.Next.next

fun update(model: AddEditTaskModel, event: AddEditTaskEvent) =
        when (event) {
            is TaskDefinitionCompleted -> onTaskDefinitionCompleted(model, event)
            is TaskCreatedSuccessfully -> exitWithSuccess()
            is TaskCreationFailed -> exitWithFailure()
            is TaskUpdatedSuccessfully -> exitWithSuccess()
            is TaskUpdateFailed -> exitWithFailure()
        }

private fun onTaskDefinitionCompleted(
        model: AddEditTaskModel,
        definitionCompleted: TaskDefinitionCompleted): Next<AddEditTaskModel, AddEditTaskEffect> {
    val (title, description) = definitionCompleted.sanitize()

    if (title.isEmpty() && description.isEmpty()) {
        return dispatch(effects(NotifyEmptyTaskNotAllowed))
    }

    val newModel = model.copy(details = model.details.copy(title = title, description = description))
    return when (newModel.mode) {
        Add -> next(newModel, effects(CreateTask(newModel.details)))
        is Edit -> next(newModel, effects(SaveTask(Task(newModel.mode.id, newModel.details))))
    }
}

private fun exitWithSuccess(): Next<AddEditTaskModel, AddEditTaskEffect> =
        dispatch(effects(Exit(true)))

private fun exitWithFailure(): Next<AddEditTaskModel, AddEditTaskEffect> =
        dispatch(effects(Exit(false)))

fun TaskDefinitionCompleted.sanitize() = this.title.trim() to this.description.trim()