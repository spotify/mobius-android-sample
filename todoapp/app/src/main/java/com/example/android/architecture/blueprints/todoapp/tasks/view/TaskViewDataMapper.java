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

import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.TaskDetails;
import com.google.common.base.Strings;

public class TaskViewDataMapper {
  public static TasksListViewData.TaskViewData createTaskViewData(Task task) {
    if (task == null) return null;
    return TasksListViewData.TaskViewData.create(
        getTitleForList(task.details()),
        task.details().completed(),
        task.details().completed()
            ? R.drawable.list_completed_touch_feedback
            : R.drawable.touch_feedback,
        task.id());
  }

  private static String getTitleForList(TaskDetails details) {
    if (!Strings.isNullOrEmpty(details.title())) {
      return details.title();
    } else {
      return details.description();
    }
  }
}
