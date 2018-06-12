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
package com.example.android.architecture.blueprints.todoapp.statistics;

import static com.google.common.base.Preconditions.checkNotNull;

import android.os.Bundle;
import com.example.android.architecture.blueprints.todoapp.statistics.domain.StatisticsState;
import com.google.common.base.Optional;
import javax.annotation.Nullable;

class StatisticsStateBundler {

  static Optional<Bundle> statisticsStateToBundle(StatisticsState state) {
    return state.map(
        loading -> Optional.absent(),
        loaded -> {
          Bundle bundle = new Bundle();
          bundle.putInt("active_count", loaded.activeCount());
          bundle.putInt("completed_count", loaded.completedCount());
          return Optional.of(bundle);
        },
        failed -> Optional.absent());
  }

  static StatisticsState bundleToStatisticsState(@Nullable Bundle bundle) {
    if (bundle == null) return StatisticsState.loading();

    if (!bundle.containsKey("statistics")) return StatisticsState.loading();

    bundle = checkNotNull(bundle.getBundle("statistics"));

    if (bundle.containsKey("active_count") && bundle.containsKey("completed_count")) {
      return StatisticsState.loaded(
          bundle.getInt("active_count"), bundle.getInt("completed_count"));
    }

    return StatisticsState.loading();
  }
}
