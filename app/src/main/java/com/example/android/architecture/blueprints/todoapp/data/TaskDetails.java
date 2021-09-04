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

@AutoValue
public abstract class TaskDetails {

  public static final TaskDetails DEFAULT = TaskDetails.builder().build();

  public abstract String title();

  public abstract String description();

  public abstract boolean completed();

  public TaskDetails withTitle(String title) {
    return toBuilder().title(title).build();
  }

  public TaskDetails withDescription(String description) {
    return toBuilder().description(description).build();
  }

  public TaskDetails withCompleted(boolean completed) {
    return toBuilder().completed(completed).build();
  }

  public abstract Builder toBuilder();

  public static TaskDetails create(String title, String description, boolean completed) {
    return builder().title(title).description(description).completed(completed).build();
  }

  public static TaskDetails create(String title, String description) {
    return builder().title(title).description(description).completed(false).build();
  }

  public static Builder builder() {
    return new AutoValue_TaskDetails.Builder().completed(false).title("").description("");
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder title(String title);

    public abstract Builder description(String description);

    public abstract Builder completed(boolean completed);

    public abstract TaskDetails build();
  }
}
