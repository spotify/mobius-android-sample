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
package com.example.android.architecture.blueprints.todoapp.tasks;

import com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksListEffect;
import com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksListEvent;
import com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksListLogic;
import com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksListModel;
import com.spotify.mobius.EventSource;
import com.spotify.mobius.MobiusLoop;
import com.spotify.mobius.android.AndroidLogger;
import com.spotify.mobius.android.MobiusAndroid;
import com.spotify.mobius.rx2.RxMobius;
import io.reactivex.ObservableTransformer;

public class TasksInjector {

  public static MobiusLoop.Controller<TasksListModel, TasksListEvent> createController(
      ObservableTransformer<TasksListEffect, TasksListEvent> effectHandler,
      EventSource<TasksListEvent> eventSource,
      TasksListModel defaultModel) {

    return MobiusAndroid.controller(createLoop(eventSource, effectHandler), defaultModel);
  }

  private static MobiusLoop.Factory<TasksListModel, TasksListEvent, TasksListEffect> createLoop(
      EventSource<TasksListEvent> eventSource,
      ObservableTransformer<TasksListEffect, TasksListEvent> effectHandler) {

    return RxMobius.loop(TasksListLogic::update, effectHandler)
        .init(TasksListLogic::init)
        .eventSource(eventSource)
        .logger(AndroidLogger.tag("TasksList"));
  }
}
