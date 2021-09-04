/*
 * Copyright 2016, The Android Open Source Project
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
package com.example.android.architecture.blueprints.todoapp.util.schedulers;

import androidx.annotation.NonNull;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;

/**
 * Implementation of the {@link BaseSchedulerProvider} making all {@link Scheduler}s execute
 * synchronously so we can easily run assertions in our tests.
 *
 * <p>To achieve this, we are using the {@link io.reactivex.internal.schedulers.TrampolineScheduler}
 * from the {@link Schedulers} class.
 */
public class ImmediateSchedulerProvider implements BaseSchedulerProvider {

  @NonNull
  @Override
  public Scheduler computation() {
    return Schedulers.trampoline();
  }

  @NonNull
  @Override
  public Scheduler io() {
    return Schedulers.trampoline();
  }

  @NonNull
  @Override
  public Scheduler ui() {
    return Schedulers.trampoline();
  }
}
