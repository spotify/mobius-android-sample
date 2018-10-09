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
package com.example.android.architecture.blueprints.todoapp.taskdetail.domain

import com.example.android.architecture.blueprints.todoapp.data.Task

/** Things we react to **/
sealed class TaskDetailEvent {
    companion object {
        @JvmStatic fun deleteTaskRequested() = DeleteTaskRequested
    }
}
object DeleteTaskRequested : TaskDetailEvent()
object CompleteTaskRequested : TaskDetailEvent()
object ActivateTaskRequested : TaskDetailEvent()
object EditTaskRequested : TaskDetailEvent()
object TaskDeleted : TaskDetailEvent()
object TaskMarkedComplete : TaskDetailEvent()
object TaskMarkedActive : TaskDetailEvent()
object TaskSaveFailed : TaskDetailEvent()
object TaskDeletionFailed : TaskDetailEvent()


/** Things we'll do **/
sealed class TaskDetailEffect
data class DeleteTask(val task: Task) : TaskDetailEffect()
data class SaveTask(val task: Task) : TaskDetailEffect()
object NotifyTaskMarkedComplete : TaskDetailEffect()
object NotifyTaskMarkedActive : TaskDetailEffect()
object NotifyTaskSaveFailed : TaskDetailEffect()
object NotifyTaskDeletionFailed : TaskDetailEffect()
data class OpenTaskEditor(val task: Task) : TaskDetailEffect()
object Exit : TaskDetailEffect()
