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
package com.example.android.architecture.blueprints.todoapp.addedittask.domain

import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.TaskDetails

/** State definition **/
data class AddEditTaskModel(val mode: Mode,
                            val details: TaskDetails = TaskDetails.DEFAULT) {
    sealed class Mode {
        object Add : Mode()
        data class Edit(@get:JvmName("id") val id: String) : Mode()
    }
}
internal typealias Model = AddEditTaskModel

/** Things we react to **/
sealed class AddEditTaskEvent
internal typealias Event = AddEditTaskEvent
data class TaskDefinitionCompleted(val title: String, val description: String) : Event()
object TaskCreatedSuccessfully : Event()
data class TaskCreationFailed(val reason: String) : Event()
object TaskUpdatedSuccessfully : Event()
data class TaskUpdateFailed(val reason: String) : Event()

/** Things we'll do **/
sealed class AddEditTaskEffect
internal typealias Effect = AddEditTaskEffect
object NotifyEmptyTaskNotAllowed : Effect()
data class CreateTask(val taskDetails: TaskDetails) : Effect()
data class SaveTask(val task: Task) : Effect()
data class Exit(val successful: Boolean) : Effect()



