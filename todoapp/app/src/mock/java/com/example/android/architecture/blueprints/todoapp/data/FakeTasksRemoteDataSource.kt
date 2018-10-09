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
package com.example.android.architecture.blueprints.todoapp.data

import android.support.annotation.VisibleForTesting
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource
import com.google.common.base.Optional
import io.reactivex.Flowable
import java.util.LinkedHashMap

/** Implementation of a remote data source with static access to the data for easy testing.  */
object FakeTasksRemoteDataSource : TasksDataSource {

  private val tasksServiceData = LinkedHashMap<String, Task>()

  override fun getTasks(): Flowable<List<Task>> =
      Flowable.fromIterable(tasksServiceData.values)
          .toList()
          .toFlowable()

  override fun getTask(taskId: String): Flowable<Optional<Task>> =
      Flowable.just(Optional.of<Task>(tasksServiceData[taskId]))

  override fun saveTask(task: Task) {
    tasksServiceData[task.id] = task
  }

  override fun deleteTask(taskId: String) {
    tasksServiceData.remove(taskId)
  }

  override fun deleteAllTasks() = tasksServiceData.clear()

  @VisibleForTesting
  fun addTasks(vararg tasks: Task) = tasks.forEach { tasksServiceData[it.id] = it }
}
