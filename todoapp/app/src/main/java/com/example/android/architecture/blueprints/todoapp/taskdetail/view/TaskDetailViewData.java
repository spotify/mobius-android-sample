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

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class TaskDetailViewData {
  public abstract TextViewData title();

  public abstract TextViewData description();

  public abstract boolean completedChecked();

  public static Builder builder() {
    return new AutoValue_TaskDetailViewData.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder title(TextViewData title);

    public abstract Builder description(TextViewData description);

    public abstract Builder completedChecked(boolean completedChecked);

    public abstract TaskDetailViewData build();
  }

  @AutoValue
  public abstract static class TextViewData {
    public abstract int visibility();

    public abstract String text();

    public static TextViewData create(int visibility, String text) {
      return new AutoValue_TaskDetailViewData_TextViewData(visibility, text);
    }
  }
}
