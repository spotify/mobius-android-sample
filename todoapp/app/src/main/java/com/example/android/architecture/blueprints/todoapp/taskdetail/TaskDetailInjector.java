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
package com.example.android.architecture.blueprints.todoapp.taskdetail;

import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.taskdetail.domain.TaskDetailEffect;
import com.example.android.architecture.blueprints.todoapp.taskdetail.domain.TaskDetailEvent;
import com.example.android.architecture.blueprints.todoapp.taskdetail.domain.TaskDetailLogic;
import com.spotify.mobius.MobiusLoop;
import com.spotify.mobius.android.AndroidLogger;
import com.spotify.mobius.android.MobiusAndroid;
import com.spotify.mobius.rx2.RxMobius;
import io.reactivex.ObservableTransformer;

public class TaskDetailInjector {

  public static MobiusLoop.Controller<Task, TaskDetailEvent> createController(
      ObservableTransformer<TaskDetailEffect, TaskDetailEvent> effectHandlers, Task defaultModel) {
    return MobiusAndroid.controller(createLoop(effectHandlers), defaultModel);
  }

  private static MobiusLoop.Factory<Task, TaskDetailEvent, TaskDetailEffect> createLoop(
      ObservableTransformer<TaskDetailEffect, TaskDetailEvent> effectHandlers) {
    return RxMobius.loop(TaskDetailLogic::update, effectHandlers)
        .logger(AndroidLogger.tag("Task Detail"));
  }
}
