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
package com.example.android.architecture.blueprints.todoapp.tasks.domain

import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksFilterType.ALL_TASKS

/** Our State **/
data class TasksListModel(@get:JvmName("tasks") val tasks: List<Task>? = null,
                          @get:JvmName("loading") val loading: Boolean = false,
                          @get:JvmName("filter") val filter: TasksFilterType = ALL_TASKS)

enum class TasksFilterType {
    ALL_TASKS,
    ACTIVE_TASKS,
    COMPLETED_TASKS
}

typealias TaskEntry = Pair<Int, Task>

/** Things we react to **/
sealed class TasksListEvent
object RefreshRequested : TasksListEvent()
object NewTaskClicked : TasksListEvent()
data class NavigateToTaskDetailsRequested(val taskId: String) : TasksListEvent()
data class TaskMarkedComplete(val taskId: String) : TasksListEvent()
data class TaskMarkedActive(val taskId: String) : TasksListEvent()
object ClearCompletedTasksRequested : TasksListEvent()
data class FilterSelected(val filterType: TasksFilterType) : TasksListEvent()
data class TasksLoaded(val tasks: List<Task>) : TasksListEvent()
object TaskCreated : TasksListEvent()
object TasksRefreshed : TasksListEvent()
object TasksFreshFailed : TasksListEvent()
object TasksLoadingFailed : TasksListEvent()

/** Things we request **/
sealed class TasksListEffect
object RefreshTasks : TasksListEffect()
object LoadTasks : TasksListEffect()
data class SaveTask(val task: Task) : TasksListEffect()
data class DeleteTasks(val tasks: List<Task>) : TasksListEffect()
data class ShowFeedback(val type: FeedbackType) : TasksListEffect()
data class NavigateToTaskDetails(val task: Task) : TasksListEffect()
object StartTaskCreationFlow : TasksListEffect()

enum class FeedbackType {
    SAVED_SUCCESSFULLY,
    MARKED_ACTIVE,
    MARKED_COMPLETE,
    CLEARED_COMPLETED,
    LOADING_ERROR
}
