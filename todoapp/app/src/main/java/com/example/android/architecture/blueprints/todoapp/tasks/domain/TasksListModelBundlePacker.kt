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

import android.os.Bundle
import com.example.android.architecture.blueprints.todoapp.data.TaskBundlePacker.taskFromBundle
import com.example.android.architecture.blueprints.todoapp.data.TaskBundlePacker.taskToBundle

internal const val FILTER = "model_filter"
internal const val LOADING = "model_loading"
internal const val TASKS = "model_tasks"

fun tasksListModelToBundle(tasksListModel: TasksListModel) = Bundle().apply {
    putSerializable(FILTER, tasksListModel.filter)
    putBoolean(LOADING, tasksListModel.loading)
    tasksListModel.tasks?.map { taskToBundle(it) }?.toTypedArray()?.let {
        putParcelableArray(TASKS, it)
    }
}


fun tasksListModelFromBundle(b: Bundle): TasksListModel {
    return with(b) {
        TasksListModel(
                filter = getSerializable(FILTER) as TasksFilterType,
                loading = getBoolean(LOADING),
                tasks = getParcelableArray(TASKS)
                        ?.toList()
                        ?.map { taskFromBundle(it as Bundle) })
    }
}
