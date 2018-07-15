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
package com.example.android.architecture.blueprints.todoapp.statistics.ui

import android.os.Bundle
import com.example.android.architecture.blueprints.todoapp.statistics.StatisticsState

private const val STATISTICS_BUNDLE_KEY = "statistics"
private const val ACTIVE_COUNT_BUNDLE_KEY = "active_count"
private const val COMPLETED_COUNT_BUNDLE_KEY = "statistics"

fun Bundle.storeStatistics(state: StatisticsState) {
    if (state is StatisticsState.Loaded) {
        this.putBundle(STATISTICS_BUNDLE_KEY, Bundle().apply {
            putInt(ACTIVE_COUNT_BUNDLE_KEY, state.activeCount)
            putInt(COMPLETED_COUNT_BUNDLE_KEY, state.completedCount)
        })
    }
}

fun Bundle?.getStatistics(): StatisticsState {
    this?.getBundle(STATISTICS_BUNDLE_KEY)?.run {
        return StatisticsState.Loaded(getInt(ACTIVE_COUNT_BUNDLE_KEY), getInt(COMPLETED_COUNT_BUNDLE_KEY))
    }

    return StatisticsState.Loading
}