/*
 * Copyright 2016, The Android Open Source Project
 * Copyright (c) 2017-2018 Spotify AB
 *
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
 */
package com.example.android.architecture.blueprints.todoapp.statistics.view;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.statistics.domain.StatisticsEvent;
import com.example.android.architecture.blueprints.todoapp.statistics.domain.StatisticsState;
import com.spotify.mobius.Connectable;
import com.spotify.mobius.Connection;
import com.spotify.mobius.functions.Consumer;
import javax.annotation.Nonnull;

public class StatisticsViews implements Connectable<StatisticsState, StatisticsEvent> {

  private final View mRoot;
  private final TextView mStatisticsTV;

  public StatisticsViews(LayoutInflater inflater, ViewGroup parent) {
    mRoot = inflater.inflate(R.layout.statistics_frag, parent, false);
    mStatisticsTV = mRoot.findViewById(R.id.statistics);
  }

  public View getRootView() {
    return mRoot;
  }

  @Nonnull
  @Override
  public Connection<StatisticsState> connect(Consumer<StatisticsEvent> output) {
    return new Connection<StatisticsState>() {
      @Override
      public void accept(StatisticsState state) {
        renderState(state);
      }

      @Override
      public void dispose() {}
    };
  }

  private void renderState(StatisticsState state) {
    state.match(
        loading -> mStatisticsTV.setText(R.string.loading),
        loaded -> {
          int numberOfCompletedTasks = loaded.completedCount();
          int numberOfIncompleteTasks = loaded.activeCount();
          if (numberOfCompletedTasks == 0 && numberOfIncompleteTasks == 0) {
            mStatisticsTV.setText(R.string.statistics_no_tasks);
          } else {
            Resources resources = mRoot.getContext().getResources();
            String activeTasksString =
                resources.getString(R.string.statistics_active_tasks, numberOfIncompleteTasks);
            String completedTasksString =
                resources.getString(R.string.statistics_completed_tasks, numberOfCompletedTasks);
            String displayString = activeTasksString + "\n" + completedTasksString;
            mStatisticsTV.setText(displayString);
          }
        },
        failed -> mStatisticsTV.setText(R.string.statistics_error));
  }
}
