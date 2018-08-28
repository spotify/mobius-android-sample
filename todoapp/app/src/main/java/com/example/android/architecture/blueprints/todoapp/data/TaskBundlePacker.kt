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
package com.example.android.architecture.blueprints.todoapp.data

import com.google.common.base.Preconditions.checkNotNull

import android.os.Bundle

object TaskBundlePacker {

  private object TaskDetailsBundleIdentifiers {
    const val TITLE = "task_title"
    const val DESCRIPTION = "task_description"
    const val STATUS = "task_status"
  }

  private object TaskBundleIdentifiers {
    const val ID = "task_id"
    const val DETAILS = "task_details"
  }

  @JvmStatic
  fun taskToBundle(task: Task) = Bundle().apply {
    putString(TaskBundleIdentifiers.ID, task.id)
    putBundle(TaskBundleIdentifiers.DETAILS, taskDetailsToBundle(task.details))
  }

  @JvmStatic
  fun taskFromBundle(b: Bundle) = Task.create(
      checkNotNull(b.getString(TaskBundleIdentifiers.ID)),
      taskDetailsFromBundle(checkNotNull(b.getBundle(TaskBundleIdentifiers.DETAILS)))
  )

  fun taskDetailsToBundle(details: TaskDetails) = Bundle().apply {
    putString(TaskDetailsBundleIdentifiers.TITLE, details.title)
    putString(TaskDetailsBundleIdentifiers.DESCRIPTION, details.description)
    putBoolean(TaskDetailsBundleIdentifiers.STATUS, details.completed)
  }

  fun taskDetailsFromBundle(b: Bundle) = TaskDetails.builder()
      .title(b.getString(TaskDetailsBundleIdentifiers.TITLE))
      .description(b.getString(TaskDetailsBundleIdentifiers.DESCRIPTION))
      .completed(b.getBoolean(TaskDetailsBundleIdentifiers.STATUS))
      .build()
}
