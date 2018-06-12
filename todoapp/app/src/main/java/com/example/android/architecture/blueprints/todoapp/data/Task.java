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

import com.google.auto.value.AutoValue;

/** Immutable model class for a Task. */
@AutoValue
public abstract class Task {

  public abstract String id();

  public abstract TaskDetails details();

  public Task withDetails(TaskDetails details) {
    return create(id(), details);
  }

  public static Task create(String id, TaskDetails details) {
    return new AutoValue_Task(id, details);
  }

  public Task complete() {
    return withDetails(details().withCompleted(true));
  }

  public Task activate() {
    return withDetails(details().withCompleted(false));
  }
}
