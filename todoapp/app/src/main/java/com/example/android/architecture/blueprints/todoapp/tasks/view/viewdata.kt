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

data class TasksListViewData(val filterLabel: Int,
                             val loading: Boolean,
                             val viewState: ViewState)

data class EmptyTasksViewData(val title: Int,
                              val icon: Int,
                              val addButtonVisibility: Int)

data class TaskViewData(val title: String,
                        val completed: Boolean,
                        val backgroundDrawableId: Int,
                        val id: String)

data class HasTasks(val tasks: List<TaskViewData>) : ViewState()
sealed class ViewState
object AwaitingTasks : ViewState()
data class EmptyTasks(val viewData: EmptyTasksViewData) : ViewState()
