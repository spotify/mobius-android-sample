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
package com.example.android.architecture.blueprints.todoapp.taskdetail.domain;

import static com.example.android.architecture.blueprints.todoapp.taskdetail.domain.TaskDetailEffect.deleteTask;
import static com.example.android.architecture.blueprints.todoapp.taskdetail.domain.TaskDetailEffect.exit;
import static com.example.android.architecture.blueprints.todoapp.taskdetail.domain.TaskDetailEffect.notifyTaskMarkedActive;
import static com.example.android.architecture.blueprints.todoapp.taskdetail.domain.TaskDetailEffect.notifyTaskMarkedComplete;
import static com.example.android.architecture.blueprints.todoapp.taskdetail.domain.TaskDetailEffect.openTaskEditor;
import static com.example.android.architecture.blueprints.todoapp.taskdetail.domain.TaskDetailEffect.saveTask;
import static com.example.android.architecture.blueprints.todoapp.taskdetail.domain.TaskDetailEvent.activateTaskRequested;
import static com.example.android.architecture.blueprints.todoapp.taskdetail.domain.TaskDetailEvent.completeTaskRequested;
import static com.example.android.architecture.blueprints.todoapp.taskdetail.domain.TaskDetailEvent.deleteTaskRequested;
import static com.example.android.architecture.blueprints.todoapp.taskdetail.domain.TaskDetailEvent.editTaskRequested;
import static com.example.android.architecture.blueprints.todoapp.taskdetail.domain.TaskDetailEvent.taskDeleted;
import static com.example.android.architecture.blueprints.todoapp.taskdetail.domain.TaskDetailEvent.taskMarkedActive;
import static com.example.android.architecture.blueprints.todoapp.taskdetail.domain.TaskDetailEvent.taskMarkedComplete;
import static com.spotify.mobius.test.NextMatchers.hasEffects;
import static com.spotify.mobius.test.NextMatchers.hasModel;
import static com.spotify.mobius.test.NextMatchers.hasNothing;
import static com.spotify.mobius.test.UpdateSpec.assertThatNext;

import androidx.annotation.NonNull;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.TaskDetails;
import com.spotify.mobius.test.UpdateSpec;
import org.junit.Before;
import org.junit.Test;

public class TaskDetailLogicTest {
  private UpdateSpec<Task, TaskDetailEvent, TaskDetailEffect> spec;

  @Before
  public void setUp() throws Exception {
    spec = new UpdateSpec<>(TaskDetailLogic::update);
  }

  @Test
  public void completingATaskCompletesAndSavesIt() {
    Task activeTask = createTask();
    spec.given(activeTask)
        .when(completeTaskRequested())
        .then(
            assertThatNext(
                hasModel(activeTask.complete()), hasEffects(saveTask(activeTask.complete()))));
  }

  @Test
  public void activatingATaskUncompletesAndSavesIt() {
    TaskDetails details = TaskDetails.create("T1", "description", true);
    Task completedTask = Task.create("123", details);
    spec.given(completedTask)
        .when(activateTaskRequested())
        .then(
            assertThatNext(
                hasModel(completedTask.activate()),
                hasEffects(saveTask(completedTask.activate()))));
  }

  @Test
  public void completingACompletedTaskDoesNothing() {
    TaskDetails details = TaskDetails.create("T1", "description", true);
    Task completedTask = Task.create("123", details);
    spec.given(completedTask).when(completeTaskRequested()).then(assertThatNext(hasNothing()));
  }

  @Test
  public void activatingAnActiveTaskDoesNothing() {
    Task activeTask = createTask();
    spec.given(activeTask).when(activateTaskRequested()).then(assertThatNext(hasNothing()));
  }

  @Test
  public void editingATaskWillOpenTaskEditor() {
    Task someTask = createTask();
    spec.given(someTask)
        .when(editTaskRequested())
        .then(assertThatNext(hasEffects(openTaskEditor(someTask))));
  }

  @Test
  public void requestingTaskDeletionDeletesTheTask() {
    Task someTask = createTask();
    spec.given(someTask)
        .when(deleteTaskRequested())
        .then(assertThatNext(hasEffects(deleteTask(someTask))));
  }

  @Test
  public void taskDeletionExitsDetailsFeature() {
    Task someTask = createTask();
    spec.given(someTask).when(taskDeleted()).then(assertThatNext(hasEffects(exit())));
  }

  @Test
  public void taskCompletionShowsFeedback() {
    Task someTask = createTask();
    spec.given(someTask)
        .when(taskMarkedComplete())
        .then(assertThatNext(hasEffects(notifyTaskMarkedComplete())));
  }

  @Test
  public void taskUpdateShowsFeedback() {
    Task someTask = createTask();
    spec.given(someTask)
        .when(taskMarkedActive())
        .then(assertThatNext(hasEffects(notifyTaskMarkedActive())));
  }

  @NonNull
  private Task createTask() {
    TaskDetails details = TaskDetails.create("T1", "description");
    return Task.create("123", details);
  }
}
