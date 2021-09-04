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
package com.example.android.architecture.blueprints.todoapp.statistics;

import static com.example.android.architecture.blueprints.todoapp.statistics.StatisticsInjector.createController;
import static com.example.android.architecture.blueprints.todoapp.statistics.StatisticsStateBundler.bundleToStatisticsState;
import static com.example.android.architecture.blueprints.todoapp.statistics.StatisticsStateBundler.statisticsStateToBundle;
import static com.example.android.architecture.blueprints.todoapp.statistics.effecthandlers.StatisticsEffectHandlers.createEffectHandler;
import static com.google.common.base.Preconditions.checkNotNull;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.android.architecture.blueprints.todoapp.statistics.domain.StatisticsEvent;
import com.example.android.architecture.blueprints.todoapp.statistics.domain.StatisticsState;
import com.example.android.architecture.blueprints.todoapp.statistics.view.StatisticsViews;
import com.google.common.base.Optional;
import com.spotify.mobius.MobiusLoop;

/** Main UI for the statistics screen. */
public class StatisticsFragment extends Fragment {

  public static StatisticsFragment newInstance() {
    return new StatisticsFragment();
  }

  private MobiusLoop.Controller<StatisticsState, StatisticsEvent> mController;

  @Nullable
  @Override
  public View onCreateView(
      LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    StatisticsViews views = new StatisticsViews(inflater, checkNotNull(container));

    mController =
        createController(
            createEffectHandler(getContext()), bundleToStatisticsState(savedInstanceState));
    mController.connect(views);
    return views.getRootView();
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    Optional<Bundle> optionalBundle = statisticsStateToBundle(mController.getModel());
    if (optionalBundle.isPresent()) {
      outState.putBundle("statistics", optionalBundle.get());
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    mController.start();
  }

  @Override
  public void onPause() {
    mController.stop();
    super.onPause();
  }

  @Override
  public void onDestroyView() {
    mController.disconnect();
    super.onDestroyView();
  }
}
