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
package com.example.android.architecture.blueprints.todoapp.taskdetail.domain

import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.TaskDetails
import com.spotify.mobius.test.NextMatchers.*
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test

class LogicTest {
    private val spec: UpdateSpec<Task, TaskDetailEvent, TaskDetailEffect> = UpdateSpec(::update)
    private val someTask = Task("123", TaskDetails("T1", "description"))

    @Test
    fun completingATaskCompletesAndSavesIt() {
        spec.given(someTask)
                .whenEvent(CompleteTaskRequested)
                .then(
                        assertThatNext(
                                hasModel(someTask.complete()),
                                hasEffects(saveTask(someTask.complete())))
                )
    }

    @Test
    fun activatingATaskUncompletesAndSavesIt() {
        val details = TaskDetails("T1", "description", true)
        val completedTask = Task("123", details)
        spec.given(completedTask)
                .whenEvent(ActivateTaskRequested)
                .then(
                        assertThatNext(
                                hasModel(completedTask.activate()),
                                hasEffects(saveTask(completedTask.activate()))))
    }

    @Test
    fun completingACompletedTaskDoesNothing() {
        val details = TaskDetails("T1", "description", true)
        val completedTask = Task("123", details)
        spec.given(completedTask).whenEvent(CompleteTaskRequested).then(assertThatNext(hasNothing()))
    }

    @Test
    fun activatingAnActiveTaskDoesNothing() {
        spec.given(someTask).whenEvent(ActivateTaskRequested).then(assertThatNext(hasNothing()))
    }

    @Test
    fun editingATaskWillOpenTaskEditor() {
        spec.given(someTask)
                .whenEvent(EditTaskRequested)
                .then(assertThatNext(hasEffects(openTaskEditor(someTask))))
    }

    @Test
    fun requestingTaskDeletionDeletesTheTask() {
        spec.given(someTask)
                .whenEvent(DeleteTaskRequested)
                .then(assertThatNext(hasEffects(deleteTask(someTask))))
    }

    @Test
    fun taskDeletionExitsDetailsFeature() {
        spec.given(someTask).whenEvent(TaskDeleted).then(assertThatNext(hasEffects(exit())))
    }

    @Test
    fun taskCompletionShowsFeedback() {
        spec.given(someTask)
                .whenEvent(TaskMarkedComplete)
                .then(assertThatNext(hasEffects(notifyTaskMarkedComplete())))
    }

    @Test
    fun taskUpdateShowsFeedback() {
        spec.given(someTask)
                .whenEvent(TaskMarkedActive)
                .then(assertThatNext(hasEffects(notifyTaskMarkedActive())))
    }

    private fun saveTask(task: Task) = SaveTask(task) as TaskDetailEffect
    private fun openTaskEditor(task: Task) = OpenTaskEditor(task) as TaskDetailEffect
    private fun deleteTask(task: Task) = DeleteTask(task) as TaskDetailEffect
    private fun exit() = Exit as TaskDetailEffect
    private fun notifyTaskMarkedComplete() = NotifyTaskMarkedComplete as TaskDetailEffect
    private fun notifyTaskMarkedActive() = NotifyTaskMarkedActive as TaskDetailEffect
}