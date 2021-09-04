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
package com.example.android.architecture.blueprints.todoapp.addedittask.domain;

import com.example.android.architecture.blueprints.todoapp.data.TaskDetails;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class AddEditTaskModel {

  public abstract AddEditTaskMode mode();

  public abstract TaskDetails details();

  public AddEditTaskModel withDetails(TaskDetails details) {
    return toBuilder().details(details).build();
  }

  public abstract Builder toBuilder();

  public static Builder builder() {
    return new AutoValue_AddEditTaskModel.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder mode(AddEditTaskMode mode);

    public abstract Builder details(TaskDetails details);

    public abstract AddEditTaskModel build();
  }
}
