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
package com.example.android.architecture.blueprints.todoapp.statistics.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.statistics.StatisticsEvent
import com.example.android.architecture.blueprints.todoapp.statistics.StatisticsState
import com.example.android.architecture.blueprints.todoapp.util.onAccept
import com.spotify.mobius.Connectable
import com.spotify.mobius.Connection
import com.spotify.mobius.functions.Consumer

class StatisticsViews(inflater: LayoutInflater, parent: ViewGroup) : Connectable<StatisticsState, StatisticsEvent> {

    val rootView: View = inflater.inflate(R.layout.statistics_frag, parent, false)

    private val statisticsTV: TextView

    init {
        statisticsTV = rootView.findViewById(R.id.statistics)
    }

    override fun connect(output: Consumer<StatisticsEvent>): Connection<StatisticsState> {
        return onAccept<StatisticsState> { renderState(it) }.onDispose { }
    }

    private fun renderState(state: StatisticsState) {
        when (state) {
            is StatisticsState.Loading -> statisticsTV.setText(R.string.loading)
            is StatisticsState.Loaded -> {
                if (state.hasTasks()) {
                    statisticsTV.text = rootView.resources.getString(R.string.statistics_tasks_count, state.activeCount, state.completedCount)
                } else {
                    statisticsTV.setText(R.string.statistics_no_tasks)
                }
            }
            is StatisticsState.Failed -> statisticsTV.setText(R.string.statistics_error)
        }
    }

    private fun StatisticsState.Loaded.hasTasks() = this.activeCount != 0 || this.completedCount != 0
}
