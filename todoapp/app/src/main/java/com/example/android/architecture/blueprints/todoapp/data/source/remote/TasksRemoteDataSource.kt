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
package com.example.android.architecture.blueprints.todoapp.data.source.remote

import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.TaskDetails
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource
import com.google.common.base.Optional
import io.reactivex.Flowable
import java.util.LinkedHashMap
import java.util.concurrent.TimeUnit

/** Implementation of the data source that adds a latency simulating network.  */
object TasksRemoteDataSource : TasksDataSource {

  private const val SERVICE_LATENCY_IN_MILLIS = 3000L
  private const val INITIAL_CAPACITY = 2
  private val tasksServiceData = LinkedHashMap<String, Task>(INITIAL_CAPACITY)

  init {
    addTask("1234", "Build tower in Pisa", "Ground looks good, no foundation work required.")
    addTask("4321", "Finish bridge in Tacoma", "Found awesome girders at half the cost!")
  }

  override fun getTasks(): Flowable<List<Task>> = Flowable.fromIterable(tasksServiceData.values)
      .delay(SERVICE_LATENCY_IN_MILLIS, TimeUnit.MILLISECONDS)
      .toList()
      .toFlowable()

  override fun getTask(taskId: String): Flowable<Optional<Task>> {
    val task = tasksServiceData[taskId]
    if (task != null) {
      return Flowable.just(Optional.of(task))
          .delay(SERVICE_LATENCY_IN_MILLIS, TimeUnit.MILLISECONDS)
    }
    return Flowable.empty()
  }

  override fun saveTask(task: Task) {
    tasksServiceData[task.id] = task
  }

  override fun deleteAllTasks() = tasksServiceData.clear()

  override fun deleteTask(taskId: String) {
    tasksServiceData.remove(taskId)
  }

  private fun addTask(id: String, title: String, description: String) {
    val newTask = Task.create(id, TaskDetails.create(title, description))
    tasksServiceData[newTask.id] = newTask
  }
}
