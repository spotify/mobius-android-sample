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
import static com.spotify.mobius.Effects.effects;
import static com.spotify.mobius.Next.dispatch;
import static com.spotify.mobius.Next.next;
import static com.spotify.mobius.Next.noChange;

import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.spotify.mobius.Next;
import javax.annotation.Nonnull;

public class TaskDetailLogic {

  @Nonnull
  public static Next<Task, TaskDetailEffect> update(Task task, TaskDetailEvent event) {
    return event.map(
        deleteTaskRequested -> dispatch(effects(deleteTask(task))),
        completeTaskRequested -> onCompleteTaskRequested(task),
        activateTaskRequested -> onActivateTaskRequested(task),
        editTaskRequested -> dispatch(effects(openTaskEditor(task))),
        taskDeleted -> dispatch(effects(exit())),
        taskCompleted -> dispatch(effects(notifyTaskMarkedComplete())),
        taskActivated -> dispatch(effects(notifyTaskMarkedActive())),
        taskSaveFailed -> noChange(),
        taskDeletionFailed -> noChange());
  }

  private static Next<Task, TaskDetailEffect> onActivateTaskRequested(Task task) {
    if (!task.details().completed()) {
      return noChange();
    }

    Task activatedTask = task.activate();
    return next(activatedTask, effects(saveTask(activatedTask)));
  }

  private static Next<Task, TaskDetailEffect> onCompleteTaskRequested(Task task) {
    if (task.details().completed()) {
      return noChange();
    }
    Task completedTask = task.complete();
    return next(completedTask, effects(saveTask(completedTask)));
  }
}
