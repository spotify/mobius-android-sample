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

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.spotify.dataenum.DataEnum;
import com.spotify.dataenum.dataenum_case;

@AutoValue
public abstract class TasksListViewData {
  @StringRes
  public abstract int filterLabel();

  public abstract boolean loading();

  public abstract ViewState viewState();

  public static Builder builder() {
    return new AutoValue_TasksListViewData.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder filterLabel(int filterLabel);

    public abstract Builder loading(boolean loading);

    public abstract Builder viewState(ViewState viewState);

    public abstract TasksListViewData build();
  }

  @AutoValue
  public abstract static class EmptyTasksViewData {
    @StringRes
    public abstract int title();

    @DrawableRes
    public abstract int icon();

    public abstract int addViewVisibility();

    public static Builder builder() {
      return new AutoValue_TasksListViewData_EmptyTasksViewData.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
      public abstract Builder title(int title);

      public abstract Builder icon(int icon);

      public abstract Builder addViewVisibility(int addViewVisibility);

      public abstract EmptyTasksViewData build();
    }
  }

  @AutoValue
  public abstract static class TaskViewData {
    public abstract String title();

    public abstract boolean completed();

    @DrawableRes
    public abstract int backgroundDrawableId();

    public abstract String id();

    public static TaskViewData create(
        String title, boolean completed, int backgroundDrawableId, String id) {
      return new AutoValue_TasksListViewData_TaskViewData(
          title, completed, backgroundDrawableId, id);
    }
  }

  @DataEnum
  public interface ViewState_dataenum {
    dataenum_case AwaitingTasks();

    dataenum_case EmptyTasks(EmptyTasksViewData viewData);

    dataenum_case HasTasks(ImmutableList<TaskViewData> taskViewData);
  }
}
