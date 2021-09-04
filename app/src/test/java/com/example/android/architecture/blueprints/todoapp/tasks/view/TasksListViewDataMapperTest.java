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
package com.example.android.architecture.blueprints.todoapp.tasks.view;

import static com.example.android.architecture.blueprints.todoapp.tasks.view.EmptyTasksViewDataMapper.createEmptyTaskViewData;
import static com.example.android.architecture.blueprints.todoapp.tasks.view.TaskViewDataMapper.createTaskViewData;
import static com.example.android.architecture.blueprints.todoapp.tasks.view.TasksListViewDataMapper.tasksListModelToViewData;
import static com.example.android.architecture.blueprints.todoapp.tasks.view.ViewState.awaitingTasks;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import android.view.View;
import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.TaskDetails;
import com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksFilterType;
import com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksListModel;
import com.example.android.architecture.blueprints.todoapp.tasks.view.TasksListViewData.EmptyTasksViewData;
import com.example.android.architecture.blueprints.todoapp.tasks.view.TasksListViewData.TaskViewData;
import com.google.common.collect.ImmutableList;
import org.junit.Test;

/** Unit tests for the implementation of {@link TasksListViewDataMapper} */
public class TasksListViewDataMapperTest {

  public static class TaskViewDataTests {
    @Test
    public void activeTasks() {
      Task task = Task.create("123", TaskDetails.create("title", "description"));
      TaskViewData taskViewData = createTaskViewData(task);
      assertThat(taskViewData.backgroundDrawableId(), is(R.drawable.touch_feedback));
      assertThat(taskViewData.completed(), is(false));
    }

    @Test
    public void completedTasks() {
      Task task = Task.create("123", TaskDetails.create("title", "description", true));
      TaskViewData taskViewData = createTaskViewData(task);
      assertThat(taskViewData.backgroundDrawableId(), is(R.drawable.list_completed_touch_feedback));
      assertThat(taskViewData.completed(), is(true));
    }

    @Test
    public void tasksWithTitleAndEmptyDescription() {
      Task task = Task.create("123", TaskDetails.create("title", "", true));
      TaskViewData taskViewData = createTaskViewData(task);
      assertThat(taskViewData.title(), is("title"));
    }

    @Test
    public void tasksWithDescriptionAndEmptyTitle() {
      Task task = Task.create("123", TaskDetails.create("", "description", true));
      TaskViewData taskViewData = createTaskViewData(task);
      assertThat(taskViewData.title(), is("description"));
    }

    @Test
    public void tasksWithBothTitleAndDescription() {
      Task task = Task.create("123", TaskDetails.create("title", "description"));
      TaskViewData taskViewData = createTaskViewData(task);
      assertThat(taskViewData.title(), is("title"));
    }
  }

  public static class EmptyTasksViewDataTests {
    @Test
    public void activeTasksFilter() {
      EmptyTasksViewData viewData = createEmptyTaskViewData(TasksFilterType.ACTIVE_TASKS);
      assertThat(viewData.addViewVisibility(), is(View.GONE));
      assertThat(viewData.title(), is(R.string.no_tasks_active));
      assertThat(viewData.icon(), is(R.drawable.ic_check_circle_24dp));
    }

    @Test
    public void completedTasksFilter() {
      EmptyTasksViewData viewData = createEmptyTaskViewData(TasksFilterType.COMPLETED_TASKS);
      assertThat(viewData.addViewVisibility(), is(View.GONE));
      assertThat(viewData.title(), is(R.string.no_tasks_completed));
      assertThat(viewData.icon(), is(R.drawable.ic_verified_user_24dp));
    }

    @Test
    public void allFilter() {
      EmptyTasksViewData viewData = createEmptyTaskViewData(TasksFilterType.ALL_TASKS);
      assertThat(viewData.addViewVisibility(), is(View.VISIBLE));
      assertThat(viewData.title(), is(R.string.no_tasks_all));
      assertThat(viewData.icon(), is(R.drawable.ic_assignment_turned_in_24dp));
    }
  }

  public static class TaskListViewDataMapperTests {
    @Test
    public void defaultModel() {
      TasksListViewData viewData = tasksListModelToViewData(TasksListModel.DEFAULT);
      assertThat(viewData.loading(), is(false));
      assertThat(viewData.viewState(), is(awaitingTasks()));
      assertThat(viewData.filterLabel(), is(R.string.label_all));
    }

    @Test
    public void loadingIndicator() {
      TasksListModel loadingWithNoData = TasksListModel.DEFAULT.withLoading(true);

      TasksListViewData viewData = tasksListModelToViewData(loadingWithNoData);
      assertThat(viewData.loading(), is(true));
    }

    @Test
    public void tasksAreFilteredThroughTheSelectedFilter() {
      ImmutableList<Task> tasks = ImmutableList.of(task("t1", true), task("t2", true), task("t3"));
      TasksListModel withTasksAndFilter =
          TasksListModel.builder()
              .loading(false)
              .filter(TasksFilterType.ACTIVE_TASKS)
              .tasks(tasks)
              .build();
      TasksListViewData viewData = tasksListModelToViewData(withTasksAndFilter);
      assertThat(viewData.viewState().isHasTasks(), is(true));
      ImmutableList<TaskViewData> tasksViewData = viewData.viewState().asHasTasks().taskViewData();
      assertThat(tasksViewData.size(), is(1));
      assertThat(
          tasksViewData.get(0),
          is(TaskViewData.create("t3", false, R.drawable.touch_feedback, "t3")));
    }

    @Test
    public void emptyViewStateWhenFilteredTasksAreEmpty() {
      ImmutableList<Task> tasks = ImmutableList.of(task("t1", true), task("t2", true));
      TasksListModel withTasksAndFilter =
          TasksListModel.builder()
              .loading(false)
              .filter(TasksFilterType.ACTIVE_TASKS)
              .tasks(tasks)
              .build();
      TasksListViewData viewData = tasksListModelToViewData(withTasksAndFilter);
      assertThat(viewData.filterLabel(), is(R.string.label_active));
      assertThat(viewData.viewState().isEmptyTasks(), is(true));
    }

    private Task task(String title) {
      return task(title, false);
    }

    private Task task(String title, boolean completed) {
      return Task.create(
          title.toLowerCase(),
          TaskDetails.create(title, title.concat(title).concat(title), completed));
    }
  }
}
