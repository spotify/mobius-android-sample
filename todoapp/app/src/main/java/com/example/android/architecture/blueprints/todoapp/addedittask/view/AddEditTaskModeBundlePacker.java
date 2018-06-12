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
package com.example.android.architecture.blueprints.todoapp.addedittask.view;

import static com.example.android.architecture.blueprints.todoapp.data.TaskBundlePacker.taskDetailsFromBundle;
import static com.google.common.base.Preconditions.checkNotNull;

import android.os.Bundle;
import com.example.android.architecture.blueprints.todoapp.addedittask.domain.AddEditTaskMode;
import com.example.android.architecture.blueprints.todoapp.addedittask.domain.AddEditTaskModel;
import com.example.android.architecture.blueprints.todoapp.data.TaskBundlePacker;
import com.google.common.base.Optional;

public class AddEditTaskModeBundlePacker {

  public static Bundle addEditTaskModelToBundle(AddEditTaskModel model) {
    Bundle b = new Bundle();
    b.putBundle("task_details", TaskBundlePacker.taskDetailsToBundle(model.details()));
    Optional<Bundle> modeBundle = addEditModeToBundle(model.mode());
    if (modeBundle.isPresent()) b.putBundle("add_edit_mode", modeBundle.get());
    return b;
  }

  public static AddEditTaskModel addEditTaskModelFromBundle(Bundle bundle) {
    return AddEditTaskModel.builder()
        .details(taskDetailsFromBundle(checkNotNull(bundle.getBundle("task_details"))))
        .mode(addEditTaskModeFromBundle(bundle.getBundle("add_edit_mode")))
        .build();
  }

  private static Optional<Bundle> addEditModeToBundle(AddEditTaskMode mode) {
    return mode.map(
        create -> Optional.absent(),
        update -> {
          Bundle b = new Bundle();
          b.putString("task_id", update.id());
          return Optional.of(b);
        });
  }

  private static AddEditTaskMode addEditTaskModeFromBundle(Bundle bundle) {
    if (bundle == null) return AddEditTaskMode.create();
    return AddEditTaskMode.update(checkNotNull(bundle.getString("task_id")));
  }
}
