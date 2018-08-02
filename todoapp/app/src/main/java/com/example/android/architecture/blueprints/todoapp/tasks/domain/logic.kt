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

import com.example.android.architecture.blueprints.todoapp.tasks.domain.FeedbackType.*
import com.example.android.architecture.blueprints.todoapp.util.change
import com.example.android.architecture.blueprints.todoapp.util.without
import com.spotify.mobius.Effects.effects
import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Next
import com.spotify.mobius.Next.*


fun init(model: TasksListModel) : First<TasksListModel, TasksListEffect> =
        model.tasks?.let { first(model, effects(LoadTasks as TasksListEffect)) }
                ?: first(model.copy(loading = true), effects(RefreshTasks, LoadTasks))

fun update(model: TasksListModel, event: TasksListEvent): Next<TasksListModel, TasksListEffect> =
        when (event) {
            RefreshRequested -> onRefreshRequested(model)
            NewTaskClicked -> onNewTaskClicked()
            is NavigateToTaskDetailsRequested -> onNavigateToTaskDetailsRequested(model, event)
            is TaskMarkedComplete -> onTaskCompleted(model, event)
            is TaskMarkedActive -> onTaskActivated(model, event)
            ClearCompletedTasksRequested -> onCompletedTasksCleared(model)
            is FilterSelected -> onFilterSelected(model, event)
            is TasksLoaded -> onTasksLoaded(model, event)
            TaskCreated -> onTaskCreated()
            TasksRefreshed -> onTasksRefreshed(model)
            TasksFreshFailed -> onTasksRefreshFailed(model)
            TasksLoadingFailed -> onTasksLoadingFailed(model)
        }

private fun onRefreshRequested(model: TasksListModel): Next<TasksListModel, TasksListEffect> =
        next(model.copy(loading = true), effects(RefreshTasks))

private fun onNewTaskClicked(): Next<TasksListModel, TasksListEffect> =
        dispatch(effects(StartTaskCreationFlow))

private fun onNavigateToTaskDetailsRequested(model: TasksListModel,
                                             event: NavigateToTaskDetailsRequested)
        : Next<TasksListModel, TasksListEffect> =
        model.findTaskById(event.taskId)?.let {
            dispatch<TasksListModel, TasksListEffect>(effects(NavigateToTaskDetails(it)))
        }
        ?: throw IllegalStateException("Task does not exist")

private fun onTaskCompleted(model: TasksListModel,
                            event: TaskMarkedComplete): Next<TasksListModel, TasksListEffect> =
        model.findTaskIndexById(event.taskId)
                .run {
                    updateTask(copy(second = second.complete()), model, MARKED_COMPLETE)
                }

private fun onTaskActivated(model: TasksListModel,
                            event: TaskMarkedActive): Next<TasksListModel, TasksListEffect> =
        model.findTaskIndexById(event.taskId)
                .run {
                    updateTask(copy(second = second.activate()), model, MARKED_ACTIVE)
                }

private fun updateTask(entry: TaskEntry,
                       model: TasksListModel,
                       feedbackType: FeedbackType): Next<TasksListModel, TasksListEffect> =
        next(model.withEntry(entry),
                effects(SaveTask(entry.second), ShowFeedback(feedbackType)))

private fun onCompletedTasksCleared(model: TasksListModel): Next<TasksListModel, TasksListEffect> =
        model.tasks
                ?.filter { it.details.completed }
                ?.let {
                    if (it.isEmpty()) noChange()
                    else next(model.copy(tasks = model.tasks.without(it)),
                            effects(DeleteTasks(it), ShowFeedback(CLEARED_COMPLETED)))
                }
                ?: noChange()

private fun onFilterSelected(model: TasksListModel, event: FilterSelected)
        : Next<TasksListModel, TasksListEffect> = next(model.copy(filter = event.filterType))

private fun onTasksLoaded(model: TasksListModel, event: TasksLoaded)
        : Next<TasksListModel, TasksListEffect> = when {
    model.loading && event.tasks.isEmpty() -> noChange()
    event.tasks == model.tasks -> noChange()
    else -> next(model.copy(tasks = event.tasks))
}

private fun onTaskCreated(): Next<TasksListModel, TasksListEffect>
        = dispatch(effects(ShowFeedback(SAVED_SUCCESSFULLY)))

private fun onTasksRefreshed(model: TasksListModel): Next<TasksListModel, TasksListEffect>
        = next(model.copy(loading = false), effects(LoadTasks))

private fun onTasksRefreshFailed(model: TasksListModel): Next<TasksListModel, TasksListEffect>
        = next(model.copy(loading = false), effects(ShowFeedback(LOADING_ERROR)))

private fun onTasksLoadingFailed(model: TasksListModel): Next<TasksListModel, TasksListEffect>
        = next(model.copy(loading = false), effects(ShowFeedback(LOADING_ERROR)))


fun TasksListModel.findTaskIndexById(id: String) =
        tasks?.indexOfFirst { it.id == id }
            ?.let {
                if (it == -1) throw IllegalArgumentException("Task does not exist")
                else it to tasks[it]
            }
            ?: throw IllegalStateException("Tasks have not been loaded yet")


fun TasksListModel.findTaskById(id: String) =
        findTaskIndexById(id).let {
            if (it.first == -1) null
            else tasks?.get(it.first)
        }

fun TasksListModel.withEntry(entry: TaskEntry) =
        tasks?.let {
            if (entry.first < 0 || entry.first >= it.size) throw IllegalArgumentException("Index out of bounds")
            else copy(tasks = it.change(entry))
        }
        ?: throw IllegalStateException("Cannot update tasks without a tasks list")

