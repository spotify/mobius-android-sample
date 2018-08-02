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

import android.view.View
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.TaskDetails
import com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksFilterType
import com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksListModel
import junit.framework.Assert.assertEquals
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/** Unit tests for the implementation of [TasksListViewDataMapper]  */
class TasksListViewDataMapperTest {

    class TaskViewDataTests {
        @Test
        fun activeTasks() {
            val task = Task("123", TaskDetails("title", "description"))
            val taskViewData = toViewData(task)
            assertThat(taskViewData.backgroundDrawableId, `is`(R.drawable.touch_feedback))
            assertThat(taskViewData.completed, `is`(false))
        }

        @Test
        fun completedTasks() {
            val task = Task("123", TaskDetails("title", "description", true))
            val taskViewData = toViewData(task)
            assertThat(taskViewData.backgroundDrawableId, `is`(R.drawable.list_completed_touch_feedback))
            assertThat(taskViewData.completed, `is`(true))
        }

        @Test
        fun tasksWithTitleAndEmptyDescription() {
            val task = Task("123", TaskDetails("title", "", true))
            val taskViewData = toViewData(task)
            assertThat(taskViewData.title, `is`("title"))
        }

        @Test
        fun tasksWithDescriptionAndEmptyTitle() {
            val task = Task("123", TaskDetails("", "description", true))
            val taskViewData = toViewData(task)
            assertThat(taskViewData.title, `is`("description"))
        }

        @Test
        fun tasksWithBothTitleAndDescription() {
            val task = Task("123", TaskDetails("title", "description"))
            val taskViewData = toViewData(task)
            assertThat(taskViewData.title, `is`("title"))
        }
    }

    class EmptyTasksViewDataTests {
        @Test
        fun activeTasksFilter() {
            val viewData = TasksFilterType.ACTIVE_TASKS.emptyTasksView()
            assertThat(viewData.addButtonVisibility, `is`(View.GONE))
            assertThat(viewData.title, `is`(R.string.no_tasks_active))
            assertThat(viewData.icon, `is`(R.drawable.ic_check_circle_24dp))
        }

        @Test
        fun completedTasksFilter() {
            val viewData = TasksFilterType.COMPLETED_TASKS.emptyTasksView()
            assertThat(viewData.addButtonVisibility, `is`(View.GONE))
            assertThat(viewData.title, `is`(R.string.no_tasks_completed))
            assertThat(viewData.icon, `is`(R.drawable.ic_verified_user_24dp))
        }

        @Test
        fun allFilter() {
            val viewData = TasksFilterType.ALL_TASKS.emptyTasksView()
            assertThat(viewData.addButtonVisibility, `is`(View.VISIBLE))
            assertThat(viewData.title, `is`(R.string.no_tasks_all))
            assertThat(viewData.icon, `is`(R.drawable.ic_assignment_turned_in_24dp))
        }
    }

    class TaskListViewDataMapperTests {
        @Test
        fun defaultModel() {
            val viewData = tasksListModelToViewData(TasksListModel())
            assertThat(viewData.loading, `is`(false))
            assertThat(viewData.viewState is AwaitingTasks, `is`(true))
            assertThat(viewData.filterLabel, `is`(R.string.label_all))
        }

        @Test
        fun loadingIndicator() {
            val loadingWithNoData = TasksListModel(loading = true)

            val viewData = tasksListModelToViewData(loadingWithNoData)
            assertThat(viewData.loading, `is`(true))
        }

        @Test
        fun tasksAreFilteredThroughTheSelectedFilter() {
            val tasks = listOf(task("t1", true), task("t2", true), task("t3"))
            val withTasksAndFilter = TasksListModel(
                    loading = false,
                    filter = TasksFilterType.ACTIVE_TASKS,
                    tasks = tasks)
            val viewData = tasksListModelToViewData(withTasksAndFilter)
            assertThat(viewData.viewState is HasTasks, `is`(true))
            val tasksViewData = (viewData.viewState as HasTasks).tasks
            assertEquals(
                    listOf(TaskViewData("t3", false, R.drawable.touch_feedback, "t3")),
                    tasksViewData)
            assertThat(tasksViewData.size, `is`(1))
            assertThat(tasksViewData[0],
                    `is`(TaskViewData("t3", false, R.drawable.touch_feedback, "t3")))
        }

        @Test
        fun emptyViewStateWhenFilteredTasksAreEmpty() {
            val tasks = listOf(task("t1", true), task("t2", true))
            val withTasksAndFilter = TasksListModel(
                    loading = false,
                    filter = TasksFilterType.ACTIVE_TASKS,
                    tasks = tasks)
            val viewData = tasksListModelToViewData(withTasksAndFilter)
            assertThat(viewData.filterLabel, `is`(R.string.label_active))
            assertThat(viewData.viewState is EmptyTasks, `is`(true))
        }

        private fun task(title: String, completed: Boolean = false): Task {
            return Task(title.toLowerCase(),
                    TaskDetails(title, title + title + title, completed))
        }
    }
}
