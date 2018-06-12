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
import static com.example.android.architecture.blueprints.todoapp.addedittask.domain.AddEditTaskEvent.taskCreatedSuccessfully;
import static com.example.android.architecture.blueprints.todoapp.addedittask.domain.AddEditTaskEvent.taskCreationFailed;
import static com.example.android.architecture.blueprints.todoapp.addedittask.domain.AddEditTaskEvent.taskDefinitionCompleted;
import static com.example.android.architecture.blueprints.todoapp.addedittask.domain.AddEditTaskEvent.taskUpdateFailed;
import static com.example.android.architecture.blueprints.todoapp.addedittask.domain.AddEditTaskEvent.taskUpdatedSuccessfully;
import static com.spotify.mobius.test.NextMatchers.hasEffects;
import static com.spotify.mobius.test.NextMatchers.hasModel;
import static com.spotify.mobius.test.NextMatchers.hasNoModel;
import static com.spotify.mobius.test.UpdateSpec.assertThatNext;

import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.TaskDetails;
import com.spotify.mobius.test.UpdateSpec;
import org.junit.Before;
import org.junit.Test;

public class AddEditTaskLogicTest {

  private UpdateSpec<AddEditTaskModel, AddEditTaskEvent, AddEditTaskEffect> updateSpec;

  @Before
  public void setUp() {
    updateSpec = new UpdateSpec<>(AddEditTaskLogic::update);
  }

  @Test
  public void
      completingTaskDefinitionWhenCreatingTaskWithEmptyTitleAndDescriptionDoesNotUpdateState() {
    AddEditTaskModel creatingTask =
        AddEditTaskModel.builder()
            .mode(AddEditTaskMode.create())
            .details(TaskDetails.DEFAULT)
            .build();

    updateSpec
        .given(creatingTask)
        .when(taskDefinitionCompleted("", "     "))
        .then(assertThatNext(hasNoModel(), hasEffects(notifyEmptyTaskNotAllowed())));
  }

  @Test
  public void completingTaskDefinitionWithEmptyTitleAndDescriptionDoesNotUpdateState() {
    AddEditTaskModel updatingATask =
        AddEditTaskModel.builder()
            .mode(AddEditTaskMode.update("123"))
            .details(TaskDetails.create("T1", "This is a task"))
            .build();

    updateSpec
        .given(updatingATask)
        .when(taskDefinitionCompleted("", "     "))
        .then(assertThatNext(hasNoModel(), hasEffects(notifyEmptyTaskNotAllowed())));
  }

  @Test
  public void tasksCanBeUpdatedWithEmptyTitle() {
    TaskDetails details = TaskDetails.create("T1", "This is a task");
    AddEditTaskModel updatingATask =
        AddEditTaskModel.builder().mode(AddEditTaskMode.update("123")).details(details).build();

    TaskDetails updatedDetails = details.toBuilder().title("").description("Hello World").build();

    updateSpec
        .given(updatingATask)
        .when(taskDefinitionCompleted("    ", "Hello World"))
        .then(
            assertThatNext(
                hasModel(updatingATask.withDetails(updatedDetails)),
                hasEffects(saveTask(Task.create("123", updatedDetails)))));
  }

  @Test
  public void tasksCanBeUpdatedWithEmptyDescription() {
    TaskDetails details = TaskDetails.create("T1", "This is a task");
    AddEditTaskModel updatingATask =
        AddEditTaskModel.builder().mode(AddEditTaskMode.update("123")).details(details).build();

    TaskDetails updatedDetails = details.toBuilder().title("Hello Tasks!").description("").build();

    updateSpec
        .given(updatingATask)
        .when(taskDefinitionCompleted("Hello Tasks!", ""))
        .then(
            assertThatNext(
                hasModel(updatingATask.withDetails(updatedDetails)),
                hasEffects(saveTask(Task.create("123", updatedDetails)))));
  }

  @Test
  public void tasksCanBeCreatedWithEmptyTitle() {
    AddEditTaskModel creatingATask =
        AddEditTaskModel.builder()
            .mode(AddEditTaskMode.create())
            .details(TaskDetails.DEFAULT)
            .build();

    updateSpec
        .given(creatingATask)
        .when(taskDefinitionCompleted("    ", "Hello World"))
        .then(
            assertThatNext(
                hasModel(creatingATask.withDetails(TaskDetails.create("", "Hello World"))),
                hasEffects(createTask(TaskDetails.create("", "Hello World")))));
  }

  @Test
  public void tasksCanBeCreatedWithEmptyDescription() {
    AddEditTaskModel creatingATask =
        AddEditTaskModel.builder()
            .mode(AddEditTaskMode.create())
            .details(TaskDetails.DEFAULT)
            .build();

    updateSpec
        .given(creatingATask)
        .when(taskDefinitionCompleted("Hello Tasks!", ""))
        .then(
            assertThatNext(
                hasModel(creatingATask.withDetails(TaskDetails.create("Hello Tasks!", ""))),
                hasEffects(createTask(TaskDetails.create("Hello Tasks!", "")))));
  }

  @Test
  public void taskCreationSuccessShouldExit() {
    AddEditTaskModel creatingATask =
        AddEditTaskModel.builder()
            .mode(AddEditTaskMode.create())
            .details(TaskDetails.create("hello", "world"))
            .build();

    updateSpec
        .given(creatingATask)
        .when(taskCreatedSuccessfully())
        .then(assertThatNext(hasNoModel(), hasEffects(exit(true))));
  }

  @Test
  public void taskUpdateSuccessShouldExit() {
    AddEditTaskModel updatingATask =
        AddEditTaskModel.builder()
            .mode(AddEditTaskMode.update("1234"))
            .details(TaskDetails.create("hello", "world"))
            .build();

    updateSpec
        .given(updatingATask)
        .when(taskUpdatedSuccessfully())
        .then(assertThatNext(hasNoModel(), hasEffects(exit(true))));
  }

  @Test
  public void taskCreationFailureShouldExit() {
    AddEditTaskModel creatingATask =
        AddEditTaskModel.builder()
            .mode(AddEditTaskMode.create())
            .details(TaskDetails.create("hello", "world"))
            .build();

    updateSpec
        .given(creatingATask)
        .when(taskCreationFailed("Database Unavailable"))
        .then(assertThatNext(hasNoModel(), hasEffects(exit(false))));
  }

  @Test
  public void taskUpdateFailureShouldExit() {
    AddEditTaskModel updatingATask =
        AddEditTaskModel.builder()
            .mode(AddEditTaskMode.update("1234"))
            .details(TaskDetails.create("hello", "world"))
            .build();

    updateSpec
        .given(updatingATask)
        .when(taskUpdateFailed("Database Unavailable"))
        .then(assertThatNext(hasNoModel(), hasEffects(exit(false))));
  }
}
