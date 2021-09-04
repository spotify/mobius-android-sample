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
package com.example.android.architecture.blueprints.todoapp.addedittask;

import com.example.android.architecture.blueprints.todoapp.addedittask.domain.AddEditTaskEffect;
import com.example.android.architecture.blueprints.todoapp.addedittask.domain.AddEditTaskEvent;
import com.example.android.architecture.blueprints.todoapp.addedittask.domain.AddEditTaskLogic;
import com.example.android.architecture.blueprints.todoapp.addedittask.domain.AddEditTaskModel;
import com.spotify.mobius.MobiusLoop;
import com.spotify.mobius.android.AndroidLogger;
import com.spotify.mobius.android.MobiusAndroid;
import com.spotify.mobius.rx2.RxMobius;
import io.reactivex.ObservableTransformer;

public class AddEditTaskInjector {

  private AddEditTaskInjector() {}

  public static MobiusLoop.Controller<AddEditTaskModel, AddEditTaskEvent> createController(
      ObservableTransformer<AddEditTaskEffect, AddEditTaskEvent> effectHandlers,
      AddEditTaskModel defaultModel) {
    return MobiusAndroid.controller(createLoop(effectHandlers), defaultModel);
  }

  private static MobiusLoop.Factory<AddEditTaskModel, AddEditTaskEvent, AddEditTaskEffect>
      createLoop(ObservableTransformer<AddEditTaskEffect, AddEditTaskEvent> effectHandlers) {

    return RxMobius.loop(AddEditTaskLogic::update, effectHandlers)
        .logger(AndroidLogger.tag("Add/Edit Tasks"));
  }
}
