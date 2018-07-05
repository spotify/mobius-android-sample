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
@file:JvmName("BundlePacker")
package com.example.android.architecture.blueprints.todoapp.addedittask.view

import android.os.Bundle
import com.example.android.architecture.blueprints.todoapp.addedittask.domain.AddEditTaskModel
import com.example.android.architecture.blueprints.todoapp.addedittask.domain.AddEditTaskModel.Mode
import com.example.android.architecture.blueprints.todoapp.addedittask.domain.AddEditTaskModel.Mode.Add
import com.example.android.architecture.blueprints.todoapp.addedittask.domain.AddEditTaskModel.Mode.Edit
import com.example.android.architecture.blueprints.todoapp.data.TaskBundlePacker
import com.example.android.architecture.blueprints.todoapp.data.TaskBundlePacker.taskDetailsFromBundle
import com.google.common.base.Optional

internal fun AddEditTaskModel.toBundle() : Bundle = Bundle().apply {
    putBundle("task_details", TaskBundlePacker.taskDetailsToBundle(details))
    mode.toBundle()?.let { putBundle("add_edit_mode", it) }
}

private fun Mode.toBundle() = when(this) {
    Add -> null
    is Edit -> Bundle().apply { putString("task_id", id) }
}

internal fun Bundle.toAddEditTaskModel(): AddEditTaskModel {
    val details = taskDetailsFromBundle(getBundle("task_details"))
    val mode = getBundle("add_edit_mode").toCreateOrUpdateModes()
    return AddEditTaskModel(mode = mode, details = details)
}

internal fun Bundle?.toCreateOrUpdateModes() = if (this == null) Add else Edit(getString("task_id"))



