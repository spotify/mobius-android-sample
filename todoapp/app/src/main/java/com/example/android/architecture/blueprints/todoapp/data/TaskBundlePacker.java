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
package com.example.android.architecture.blueprints.todoapp.data;

import static com.google.common.base.Preconditions.checkNotNull;

import android.os.Bundle;

public class TaskBundlePacker {

  private static class TaskDetailsBundleIdentifiers {
    static final String TITLE = "task_title";
    static final String DESCRIPTION = "task_description";
    static final String STATUS = "task_status";
  }

  private static class TaskBundleIdentifiers {
    static final String ID = "task_id";
    static final String DETAILS = "task_details";
  }

  public static Bundle taskToBundle(Task task) {
    Bundle b = new Bundle();
    b.putString(TaskBundleIdentifiers.ID, task.id());
    b.putBundle(TaskBundleIdentifiers.DETAILS, taskDetailsToBundle(task.details()));
    return b;
  }

  public static Task taskFromBundle(Bundle b) {
    return Task.create(
        checkNotNull(b.getString(TaskBundleIdentifiers.ID)),
        taskDetailsFromBundle(checkNotNull(b.getBundle(TaskBundleIdentifiers.DETAILS))));
  }

  public static Bundle taskDetailsToBundle(TaskDetails details) {
    Bundle b = new Bundle();
    b.putString(TaskDetailsBundleIdentifiers.TITLE, details.title());
    b.putString(TaskDetailsBundleIdentifiers.DESCRIPTION, details.description());
    b.putBoolean(TaskDetailsBundleIdentifiers.STATUS, details.completed());
    return b;
  }

  public static TaskDetails taskDetailsFromBundle(Bundle b) {
    String title = checkNotNull(b.getString(TaskDetailsBundleIdentifiers.TITLE));
    String description = checkNotNull(b.getString(TaskDetailsBundleIdentifiers.DESCRIPTION));
    return TaskDetails.builder()
        .title(title)
        .description(description)
        .completed(b.getBoolean(TaskDetailsBundleIdentifiers.STATUS))
        .build();
  }
}
