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
package com.example.android.architecture.blueprints.todoapp.addedittask.domain;

import static com.example.android.architecture.blueprints.todoapp.addedittask.domain.AddEditTaskEffect.createTask;
import static com.example.android.architecture.blueprints.todoapp.addedittask.domain.AddEditTaskEffect.exit;
import static com.example.android.architecture.blueprints.todoapp.addedittask.domain.AddEditTaskEffect.notifyEmptyTaskNotAllowed;
import static com.example.android.architecture.blueprints.todoapp.addedittask.domain.AddEditTaskEffect.saveTask;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.spotify.mobius.Effects.effects;
import static com.spotify.mobius.Next.dispatch;
import static com.spotify.mobius.Next.next;

import com.example.android.architecture.blueprints.todoapp.addedittask.domain.AddEditTaskEvent.TaskDefinitionCompleted;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.TaskDetails;
import com.spotify.mobius.Next;
import javax.annotation.Nonnull;

public class AddEditTaskLogic {

  @Nonnull
  public static Next<AddEditTaskModel, AddEditTaskEffect> update(
      AddEditTaskModel model, AddEditTaskEvent event) {
    return event.map(
        taskDefinitionCompleted -> onTaskDefinitionCompleted(model, taskDefinitionCompleted),
        taskCreatedSuccessfully -> exitWithSuccess(),
        taskCreationFailed -> exitWithFailure(),
        taskUpdatedSuccessfully -> exitWithSuccess(),
        taskUpdateFailed -> exitWithFailure());
  }

  private static Next<AddEditTaskModel, AddEditTaskEffect> onTaskDefinitionCompleted(
      AddEditTaskModel model, TaskDefinitionCompleted definitionCompleted) {
    String title = definitionCompleted.title().trim();
    String description = definitionCompleted.description().trim();

    if (isNullOrEmpty(title) && isNullOrEmpty(description)) {
      return dispatch(effects(notifyEmptyTaskNotAllowed()));
    }

    TaskDetails details = model.details().toBuilder().title(title).description(description).build();

    AddEditTaskModel newModel = model.withDetails(details);

    return newModel
        .mode()
        .map(
            create -> next(newModel, effects(createTask(newModel.details()))),
            update ->
                next(newModel, effects(saveTask(Task.create(update.id(), newModel.details())))));
  }

  private static Next<AddEditTaskModel, AddEditTaskEffect> exitWithSuccess() {
    return dispatch(effects(exit(true)));
  }

  private static Next<AddEditTaskModel, AddEditTaskEffect> exitWithFailure() {
    return dispatch(effects(exit(false)));
  }
}
