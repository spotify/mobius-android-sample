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
package com.example.android.architecture.blueprints.todoapp.taskdetail.view;

import static com.google.common.base.Strings.isNullOrEmpty;

import android.view.View;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.TaskDetails;
import com.example.android.architecture.blueprints.todoapp.taskdetail.view.TaskDetailViewData.TextViewData;

/** Maps instances of {@link Task} to {@link TaskDetailViewData} */
public class TaskDetailViewDataMapper {

  public static TaskDetailViewData taskToTaskViewData(Task task) {
    TaskDetails details = task.details();
    String title = details.title();
    String description = details.description();

    return TaskDetailViewData.builder()
        .title(TextViewData.create(isNullOrEmpty(title) ? View.GONE : View.VISIBLE, title))
        .description(
            TextViewData.create(isNullOrEmpty(description) ? View.GONE : View.VISIBLE, description))
        .completedChecked(details.completed())
        .build();
  }
}
