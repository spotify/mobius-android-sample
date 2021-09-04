/*
 * Copyright 2016, The Android Open Source Project
 * Copyright (c) 2017-2018 Spotify AB
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
package com.example.android.architecture.blueprints.todoapp.taskdetail.view;

import static com.example.android.architecture.blueprints.todoapp.taskdetail.domain.TaskDetailEvent.activateTaskRequested;
import static com.example.android.architecture.blueprints.todoapp.taskdetail.domain.TaskDetailEvent.completeTaskRequested;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.taskdetail.domain.TaskDetailEvent;
import com.spotify.mobius.Connectable;
import com.spotify.mobius.Connection;
import com.spotify.mobius.functions.Consumer;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import javax.annotation.Nonnull;

public class TaskDetailViews
    implements TaskDetailViewActions, Connectable<TaskDetailViewData, TaskDetailEvent> {
  private final FloatingActionButton mFab;
  private final Observable<TaskDetailEvent> mMenuEvents;

  private TextView mDetailTitle;

  private TextView mDetailDescription;

  private CheckBox mDetailCompleteStatus;

  private final View mRootView;

  public TaskDetailViews(
      LayoutInflater inflater,
      ViewGroup container,
      FloatingActionButton fab,
      Observable<TaskDetailEvent> menuEvents) {
    mRootView = inflater.inflate(R.layout.taskdetail_frag, container, false);
    mMenuEvents = menuEvents;
    mDetailTitle = mRootView.findViewById(R.id.task_detail_title);
    mDetailDescription = mRootView.findViewById(R.id.task_detail_description);
    mDetailCompleteStatus = mRootView.findViewById(R.id.task_detail_complete);
    mFab = fab;
  }

  public View getRootView() {
    return mRootView;
  }

  @Override
  public void showTaskMarkedComplete() {
    Snackbar.make(mRootView, R.string.task_marked_complete, Snackbar.LENGTH_LONG).show();
  }

  @Override
  public void showTaskMarkedActive() {
    Snackbar.make(mRootView, R.string.task_marked_active, Snackbar.LENGTH_LONG).show();
  }

  @Override
  public void showTaskDeletionFailed() {
    Snackbar.make(mRootView, "Failed to delete task", Snackbar.LENGTH_LONG).show();
  }

  @Override
  public void showTaskSavingFailed() {
    Snackbar.make(mRootView, "Failed to save change", Snackbar.LENGTH_LONG).show();
  }

  private void render(TaskDetailViewData viewData) {
    mDetailCompleteStatus.setChecked(viewData.completedChecked());
    bindTextViewData(mDetailTitle, viewData.title());
    bindTextViewData(mDetailDescription, viewData.description());
  }

  private void bindTextViewData(TextView textView, TaskDetailViewData.TextViewData viewData) {
    textView.setVisibility(viewData.visibility());
    textView.setText(viewData.text());
  }

  @Nonnull
  @Override
  public Connection<TaskDetailViewData> connect(Consumer<TaskDetailEvent> output) {
    mFab.setOnClickListener(__ -> output.accept(TaskDetailEvent.editTaskRequested()));

    mDetailCompleteStatus.setOnCheckedChangeListener(
        (buttonView, isChecked) -> {
          if (isChecked) {
            output.accept(completeTaskRequested());
          } else {
            output.accept(activateTaskRequested());
          }
        });

    Disposable disposable =
        mMenuEvents
            .retry()
            .subscribe(
                output::accept,
                t -> Log.e(TaskDetailViews.class.getSimpleName(), "Menu events seem to fail", t));

    return new Connection<TaskDetailViewData>() {
      @Override
      public void accept(TaskDetailViewData value) {
        render(value);
      }

      @Override
      public void dispose() {
        disposable.dispose();
        mFab.setOnClickListener(null);
        mDetailCompleteStatus.setOnCheckedChangeListener(null);
      }
    };
  }
}
