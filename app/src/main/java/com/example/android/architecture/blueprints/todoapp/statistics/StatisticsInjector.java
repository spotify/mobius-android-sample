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

import com.example.android.architecture.blueprints.todoapp.statistics.domain.StatisticsEffect;
import com.example.android.architecture.blueprints.todoapp.statistics.domain.StatisticsEvent;
import com.example.android.architecture.blueprints.todoapp.statistics.domain.StatisticsLogic;
import com.example.android.architecture.blueprints.todoapp.statistics.domain.StatisticsState;
import com.spotify.mobius.MobiusLoop;
import com.spotify.mobius.android.MobiusAndroid;
import com.spotify.mobius.rx2.RxMobius;
import io.reactivex.ObservableTransformer;

public class StatisticsInjector {
  public static MobiusLoop.Controller<StatisticsState, StatisticsEvent> createController(
      ObservableTransformer<StatisticsEffect, StatisticsEvent> effectHandler,
      StatisticsState defaultState) {
    return MobiusAndroid.controller(createLoop(effectHandler), defaultState);
  }

  private static MobiusLoop.Factory<StatisticsState, StatisticsEvent, StatisticsEffect> createLoop(
      ObservableTransformer<StatisticsEffect, StatisticsEvent> effectHandler) {
    return RxMobius.loop(StatisticsLogic::update, effectHandler).init(StatisticsLogic::init);
  }
}
