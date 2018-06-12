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
package com.example.android.architecture.blueprints.todoapp.tasks.domain;

import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.google.common.collect.ImmutableList;
import com.spotify.dataenum.DataEnum;
import com.spotify.dataenum.dataenum_case;

@DataEnum
interface TasksListEvent_dataenum {

  dataenum_case RefreshRequested();

  dataenum_case NewTaskClicked();

  dataenum_case NavigateToTaskDetailsRequested(String taskId);

  dataenum_case TaskMarkedComplete(String taskId);

  dataenum_case TaskMarkedActive(String taskId);

  dataenum_case ClearCompletedTasksRequested();

  dataenum_case FilterSelected(TasksFilterType filterType);

  dataenum_case TasksLoaded(ImmutableList<Task> tasks);

  dataenum_case TaskCreated();

  dataenum_case TasksRefreshed();

  dataenum_case TasksRefreshFailed();

  dataenum_case TasksLoadingFailed();
}
