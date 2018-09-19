/*
 * Copyright 2016, The Android Open Source Project
 * Copyright (c) 2017-2018 Spotify AB
 *
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
 */
@file:JvmName("TaskDetailViewDataMapper")

package com.example.android.architecture.blueprints.todoapp.taskdetail.view

import android.view.View
import com.example.android.architecture.blueprints.todoapp.data.Task

fun taskToTaskViewData(task: Task): TaskDetailViewData {
    val details = task.details
    val title = details.title
    val description = details.description

    return TaskDetailViewData(
            title = TextViewData(if (title.isEmpty()) View.GONE else View.VISIBLE, title),
            description = TextViewData(if (description.isEmpty()) View.GONE else View.VISIBLE, description),
            completedChecked = details.completed)
}