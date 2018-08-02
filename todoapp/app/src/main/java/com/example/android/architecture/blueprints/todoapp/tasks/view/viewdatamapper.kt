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
package com.example.android.architecture.blueprints.todoapp.tasks.view

import android.view.View
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksFilterType
import com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksFilterType.*
import com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksListModel
import com.example.android.architecture.blueprints.todoapp.tasks.domain.filter

fun tasksListModelToViewData(model: TasksListModel) =
        TasksListViewData(
            loading = model.loading,
            filterLabel = model.filter.label(),
            viewState = model.content().toViewState()
        )


private fun Pair<TasksFilterType, List<Task>?>.toViewState() =
        second?.filter(first)
                ?.run {
                    if (isEmpty()) EmptyTasks(first.emptyTasksView())
                    else HasTasks(map(::toViewData))
                }
                ?: AwaitingTasks

private fun TasksListModel.content() = filter to tasks

fun toViewData(task: Task) =
        TaskViewData(
            title = if (!task.details.title.isEmpty()) task.details.title
                    else task.details.description,
            completed = task.details.completed,
            backgroundDrawableId =
                if (task.details.completed) R.drawable.list_completed_touch_feedback
                else R.drawable.touch_feedback,
            id = task.id
        )

fun TasksFilterType.label() =
        when(this) {
            ALL_TASKS -> R.string.label_all
            ACTIVE_TASKS -> R.string.label_active
            COMPLETED_TASKS -> R.string.label_completed
        }

fun TasksFilterType.emptyTasksView() =
    when(this) {
        ALL_TASKS -> EmptyTasksViewData(
                R.string.no_tasks_all,
                R.drawable.ic_assignment_turned_in_24dp,
                View.VISIBLE)
        ACTIVE_TASKS -> EmptyTasksViewData(
                R.string.no_tasks_active,
                R.drawable.ic_check_circle_24dp,
                View.GONE)
        COMPLETED_TASKS -> EmptyTasksViewData(
                R.string.no_tasks_completed,
                R.drawable.ic_verified_user_24dp,
                View.GONE)
    }
