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
@file:JvmName("TaskDetailLogic")

package com.example.android.architecture.blueprints.todoapp.taskdetail.domain

import com.example.android.architecture.blueprints.todoapp.data.Task
import com.spotify.mobius.Effects.effects
import com.spotify.mobius.Next
import com.spotify.mobius.Next.*

fun update(task: Task, event: TaskDetailEvent): Next<Task, TaskDetailEffect> =
        when (event) {
            DeleteTaskRequested -> dispatch(effects(DeleteTask(task)))
            CompleteTaskRequested -> onCompleteTaskRequested(task)
            ActivateTaskRequested -> onActivateTaskRequested(task)
            EditTaskRequested -> dispatch(effects(OpenTaskEditor(task)))
            TaskDeleted -> dispatch(effects(Exit))
            TaskMarkedComplete -> dispatch(effects(NotifyTaskMarkedComplete))
            TaskMarkedActive -> dispatch(effects(NotifyTaskMarkedActive))
            TaskSaveFailed -> noChange()
            TaskDeletionFailed -> noChange()
        }

private fun onActivateTaskRequested(task: Task): Next<Task, TaskDetailEffect> =
        if (!task.details.completed) {
            noChange()
        } else {
            val activatedTask = task.activate()
            next(activatedTask, effects(SaveTask(activatedTask)))
        }

private fun onCompleteTaskRequested(task: Task): Next<Task, TaskDetailEffect> =
        if (task.details.completed) {
            noChange()
        } else {
            val completedTask = task.complete()
            next(completedTask, effects(SaveTask(completedTask)))
        }
