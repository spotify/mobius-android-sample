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

import android.view.View;
import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksFilterType;
import com.example.android.architecture.blueprints.todoapp.tasks.view.TasksListViewData.EmptyTasksViewData;

public class EmptyTasksViewDataMapper {
  public static EmptyTasksViewData createEmptyTaskViewData(TasksFilterType filter) {
    EmptyTasksViewData.Builder builder = EmptyTasksViewData.builder();
    switch (filter) {
      case ACTIVE_TASKS:
        return builder
            .addViewVisibility(View.GONE)
            .title(R.string.no_tasks_active)
            .icon(R.drawable.ic_check_circle_24dp)
            .build();
      case COMPLETED_TASKS:
        return builder
            .addViewVisibility(View.GONE)
            .title(R.string.no_tasks_completed)
            .icon(R.drawable.ic_verified_user_24dp)
            .build();
      default:
        return builder
            .addViewVisibility(View.VISIBLE)
            .title(R.string.no_tasks_all)
            .icon(R.drawable.ic_assignment_turned_in_24dp)
            .build();
    }
  }
}
