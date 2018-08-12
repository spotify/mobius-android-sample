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

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.android.architecture.blueprints.todoapp.statistics.StatisticsEvent
import com.example.android.architecture.blueprints.todoapp.statistics.StatisticsState
import com.example.android.architecture.blueprints.todoapp.statistics.createEffectHandler
import com.spotify.mobius.MobiusLoop

/** Main UI for the statistics screen.  */
class StatisticsFragment : Fragment() {

    private lateinit var mController: MobiusLoop.Controller<StatisticsState, StatisticsEvent>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val views = StatisticsViews(inflater, container!!)

        mController = createController(createEffectHandler(inflater.context), savedInstanceState.getStatistics())
        mController.connect(views)

        return views.rootView
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.storeStatistics(mController.model)
    }

    override fun onResume() {
        super.onResume()
        mController.start()
    }

    override fun onPause() {
        mController.stop()
        super.onPause()
    }

    override fun onDestroyView() {
        mController.disconnect()
        super.onDestroyView()
    }
}
