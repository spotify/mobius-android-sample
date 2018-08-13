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

import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.TaskDetails
import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableList.of
import com.spotify.mobius.test.FirstMatchers
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import com.spotify.mobius.test.NextMatchers.*
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test

class TasksListLogicTest {

    private val initSpec = InitSpec(::init)

    private val updateSpec = UpdateSpec(::update)

    @Test
    fun initWithoutDataLoadsRemoteAndLocalData() {
        initSpec
                .whenInit(TasksListModel())
                .then(assertThatFirst(
                                FirstMatchers.hasModel(TasksListModel(loading = true)),
                                FirstMatchers.hasEffects(refreshTasks(), loadTasks())))
    }

    @Test
    fun initWithDataDispatchesNoEffects() {
        initSpec
                .whenInit(modelWithTasks(task("t1"), task("t2"), task("t3")))
                .then(assertThatFirst(
                                FirstMatchers.hasModel(modelWithTasks(task("t1"), task("t2"), task("t3"))),
                                FirstMatchers.hasEffects(loadTasks())))
    }

    @Test
    fun requestingRefreshesDispatchesLoadRemoteTasksEffect() {
        val stateWithSomeTasks = modelWithTasks(task("t1"), task("t2"), task("t3"))
        updateSpec
                .given(stateWithSomeTasks)
                .whenEvent(RefreshRequested)
                .then(assertThatNext(
                        hasModel(stateWithSomeTasks.copy(loading = true)),
                        hasEffects(refreshTasks())))
    }

    @Test
    fun navigatingToATaskWithAValidIndexDispatchesNavigationEffect() {
        val stateWithSomeTasks = modelWithTasks(task("t1"), task("t2"), task("t3"))
        updateSpec
                .given(stateWithSomeTasks)
                .whenEvent(NavigateToTaskDetailsRequested("t3"))
                .then(assertThatNext(
                        hasNoModel(),
                        hasEffects(navigateToTaskDetails(task("t3")))))
    }

    @Test
    fun completingTasksUpdatesModelAndDispatchesSaveTaskEffect() {
        val stateWithSomeTasks = modelWithTasks(task("t1"), task("t2"), task("t3"))
        updateSpec
                .given(stateWithSomeTasks)
                .whenEvent(TaskMarkedComplete("t2"))
                .then(assertThatNext(
                            hasModel(stateWithSomeTasks.withEntry(1 to task("t2", true))),
                            hasEffects(
                                    saveTask(task("t2", true)),
                                    showFeedback(FeedbackType.MARKED_COMPLETE)
                            )))
    }

    @Test
    fun completingTasksWhileHavingAFilterUpdatesTheCorrectIndex() {
        val stateWithSomeTasks = modelWithTasks(task("t1", true), task("t2"), task("t3"))
                .copy(filter = TasksFilterType.ACTIVE_TASKS)

        updateSpec
                .given(stateWithSomeTasks)
                .whenEvent(TaskMarkedComplete("t2"))
                .then(assertThatNext(
                                hasModel(stateWithSomeTasks.withEntry(1 to task("t2", true))),
                                hasEffects(saveTask(task("t2", true)))))
    }

    @Test
    fun activatingATaskUncompletesItAndSavesIt() {
        val stateWithSomeTasks = modelWithTasks(task("t1", true), task("t2"), task("t3"))

        updateSpec
                .given(stateWithSomeTasks)
                .whenEvent(TaskMarkedActive("t1"))
                .then(assertThatNext(
                            hasModel(stateWithSomeTasks.withEntry(0 to task("t1"))),
                            hasEffects(saveTask(task("t1")), showFeedback(FeedbackType.MARKED_ACTIVE))))
    }

    @Test
    fun addingAFilterUpdatesTheCurrentFilter() {
        val stateWithSomeTasks = modelWithTasks(task("t1", true), task("t2"), task("t3"))

        updateSpec
                .given(stateWithSomeTasks)
                .whenEvent(FilterSelected(TasksFilterType.COMPLETED_TASKS))
                .then(assertThatNext(
                            hasModel(stateWithSomeTasks.copy(filter = TasksFilterType.COMPLETED_TASKS)),
                            hasNoEffects()))
    }

    @Test
    fun loadedTasksReplaceTheCurrentList() {
        val stateWithSomeTasks = modelWithTasks(task("t1", true), task("t2"), task("t3"))

        val receivedList = of(task("t4"), task("t5"))
        updateSpec
                .given(stateWithSomeTasks)
                .whenEvent(TasksLoaded(receivedList))
                .then(assertThatNext(
                        hasModel(stateWithSomeTasks.copy(tasks = receivedList)),
                        hasNoEffects()))
    }

    @Test
    fun loadedTasksReplaceTheCurrentNullList() {
        val receivedList = of(task("t4"), task("t5"))
        updateSpec
                .given(TasksListModel())
                .whenEvent(TasksLoaded(receivedList))
                .then(assertThatNext(
                        hasModel(TasksListModel(tasks = receivedList)),
                        hasNoEffects()))
    }

    @Test
    fun loadingEmptyTaskListLocallyWhileRefreshingShouldNotUpdateModel() {
        updateSpec
                .given(TasksListModel(loading = true))
                .whenEvent(TasksLoaded(ImmutableList.of<Task>()))
                .then(assertThatNext(hasNothing()))
    }

    @Test
    fun loadingEmptyTaskListLocallyWhenNotRefreshingUpdatesModel() {
        updateSpec
                .given(TasksListModel())
                .whenEvent(TasksLoaded(ImmutableList.of<Task>()))
                .then(assertThatNext(
                        hasModel(TasksListModel(tasks = listOf())),
                        hasNoEffects()))
    }

    @Test
    fun createdTasksEndAtTailOfTheList() {
        val stateWithSomeTasks = modelWithTasks(task("t1", true), task("t2"), task("t3"))

        updateSpec
                .given(stateWithSomeTasks)
                .whenEvent(TaskCreated)
                .then(assertThatNext(
                        hasNoModel(),
                        hasEffects(showFeedback(FeedbackType.SAVED_SUCCESSFULLY))))
    }

    @Test
    fun clearingCompletedTasksShouldRemoveThemFromTheListAndDeleteThem() {
        val stateWithSomeTasks = modelWithTasks(task("t1", true), task("t2", true), task("t3"))

        updateSpec
                .given(stateWithSomeTasks)
                .whenEvent(ClearCompletedTasksRequested)
                .then(assertThatNext(
                        hasModel(modelWithTasks(task("t3"))),
                        hasEffects(
                                deleteTasks(task("t1", true), task("t2", true)),
                                showFeedback(FeedbackType.CLEARED_COMPLETED))))
    }

    @Test
    fun taskRefreshesReloadsTasksFromDatabase() {
        val stateWithSomeTasks = modelWithTasks(task("t1", true), task("t2", true), task("t3"))
        updateSpec
                .given(stateWithSomeTasks)
                .whenEvent(TasksRefreshed)
                .then(assertThatNext(
                        hasModel(stateWithSomeTasks.copy(loading = false)),
                        hasEffects(loadTasks())))
    }

    @Test
    fun taskCreationRequested() {
        updateSpec.given(TasksListModel())
                .whenEvent(NewTaskClicked)
                .then(assertThatNext(hasNoModel(), hasEffects(startTaskCreationFlow())))
    }

    private fun deleteTasks(vararg tasks: Task): TasksListEffect {
        return deleteTasks(tasks.toList())
    }

    private fun modelWithTasks(vararg  tasks: Task) = TasksListModel(tasks = tasks.toList())
    
    private fun task(title: String, completed: Boolean = false): Task {
        return Task.create(
                title.toLowerCase(),
                TaskDetails.create(title, title + title + title, completed))
    }

    fun refreshTasks() = RefreshTasks as TasksListEffect
    fun loadTasks() = LoadTasks as TasksListEffect
    fun saveTask(task: Task) = SaveTask(task) as TasksListEffect
    fun deleteTasks(tasks: List<Task>) = DeleteTasks(tasks) as TasksListEffect
    fun showFeedback(type: FeedbackType) = ShowFeedback(type) as TasksListEffect
    fun navigateToTaskDetails(task: Task) = NavigateToTaskDetails(task) as TasksListEffect
    fun startTaskCreationFlow() = StartTaskCreationFlow as TasksListEffect
}
