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
package com.example.android.architecture.blueprints.todoapp.tasks.view;

import static com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksListEvent.navigateToTaskDetailsRequested;
import static com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksListEvent.newTaskClicked;
import static com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksListEvent.refreshRequested;
import static com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksListEvent.taskMarkedActive;
import static com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksListEvent.taskMarkedComplete;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksListEvent;
import com.example.android.architecture.blueprints.todoapp.tasks.view.TasksListViewData.TaskViewData;
import com.google.common.collect.ImmutableList;
import com.spotify.mobius.Connectable;
import com.spotify.mobius.Connection;
import com.spotify.mobius.functions.Consumer;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import javax.annotation.Nonnull;

public class TasksViews
    implements TasksListViewActions, Connectable<TasksListViewData, TasksListEvent> {
  private final View mRoot;
  private final ScrollChildSwipeRefreshLayout mSwipeRefreshLayout;
  private final FloatingActionButton mFab;
  private final Observable<TasksListEvent> menuEvents;

  private TasksAdapter mListAdapter;

  private View mNoTasksView;

  private ImageView mNoTaskIcon;

  private TextView mNoTaskMainView;

  private TextView mNoTaskAddView;

  private LinearLayout mTasksView;

  private TextView mFilteringLabelView;

  public TasksViews(
      LayoutInflater inflater,
      ViewGroup parent,
      FloatingActionButton fab,
      Observable<TasksListEvent> menuEvents) {
    this.menuEvents = menuEvents;
    mRoot = inflater.inflate(R.layout.tasks_frag, parent, false);
    mListAdapter = new TasksAdapter();
    // Set up allTasks view
    ListView listView = mRoot.findViewById(R.id.tasks_list);
    listView.setAdapter(mListAdapter);
    mFilteringLabelView = mRoot.findViewById(R.id.filteringLabel);
    mTasksView = mRoot.findViewById(R.id.tasksLL);

    // Set up  no allTasks view
    mNoTasksView = mRoot.findViewById(R.id.noTasks);
    mNoTaskIcon = mRoot.findViewById(R.id.noTasksIcon);
    mNoTaskMainView = mRoot.findViewById(R.id.noTasksMain);
    mNoTaskAddView = mRoot.findViewById(R.id.noTasksAdd);
    fab.setImageResource(R.drawable.ic_add);
    mFab = fab;
    // Set up progress indicator
    mSwipeRefreshLayout = mRoot.findViewById(R.id.refresh_layout);
    mSwipeRefreshLayout.setColorSchemeColors(
        ContextCompat.getColor(mRoot.getContext(), R.color.colorPrimary),
        ContextCompat.getColor(mRoot.getContext(), R.color.colorAccent),
        ContextCompat.getColor(mRoot.getContext(), R.color.colorPrimaryDark));
    // Set the scrolling view in the custom SwipeRefreshLayout.
    mSwipeRefreshLayout.setScrollUpChild(listView);
  }

  public View getRootView() {
    return mRoot;
  }

  @Override
  public void showSuccessfullySavedMessage() {
    showMessage(R.string.successfully_saved_task_message);
  }

  @Override
  public void showTaskMarkedComplete() {
    showMessage(R.string.task_marked_complete);
  }

  @Override
  public void showTaskMarkedActive() {
    showMessage(R.string.task_marked_active);
  }

  @Override
  public void showCompletedTasksCleared() {
    showMessage(R.string.completed_tasks_cleared);
  }

  @Override
  public void showLoadingTasksError() {
    showMessage(R.string.loading_tasks_error);
  }

  private void showMessage(int messageRes) {
    Snackbar.make(mRoot, messageRes, Snackbar.LENGTH_LONG).show();
  }

  @Nonnull
  @Override
  public Connection<TasksListViewData> connect(Consumer<TasksListEvent> output) {

    addUiListeners(output);
    Disposable disposable = menuEvents.subscribe(output::accept);

    return new Connection<TasksListViewData>() {
      @Override
      public void accept(TasksListViewData value) {
        render(value);
      }

      @Override
      public void dispose() {
        disposable.dispose();
        mNoTaskAddView.setOnClickListener(null);
        mFab.setOnClickListener(null);
        mSwipeRefreshLayout.setOnRefreshListener(null);
        mListAdapter.setItemListener(null);
      }
    };
  }

  private void addUiListeners(Consumer<TasksListEvent> output) {
    mNoTaskAddView.setOnClickListener(__ -> output.accept(newTaskClicked()));
    mFab.setOnClickListener(__ -> output.accept(newTaskClicked()));
    mSwipeRefreshLayout.setOnRefreshListener(() -> output.accept(refreshRequested()));
    mListAdapter.setItemListener(
        new TasksAdapter.TaskItemListener() {
          @Override
          public void onTaskClick(String id) {
            output.accept(navigateToTaskDetailsRequested(id));
          }

          @Override
          public void onCompleteTaskClick(String id) {
            output.accept(taskMarkedComplete(id));
          }

          @Override
          public void onActivateTaskClick(String id) {
            output.accept(taskMarkedActive(id));
          }
        });
  }

  private void showEmptyTaskState(TasksListViewData.EmptyTasksViewData vd) {
    mTasksView.setVisibility(View.GONE);
    mNoTasksView.setVisibility(View.VISIBLE);

    mNoTaskMainView.setText(vd.title());
    mNoTaskIcon.setImageResource(vd.icon());
    mNoTaskAddView.setVisibility(vd.addViewVisibility());
  }

  private void showNoTasksViewState() {
    mTasksView.setVisibility(View.GONE);
    mNoTasksView.setVisibility(View.GONE);
  }

  private void showTasks(ImmutableList<TaskViewData> tasks) {
    mListAdapter.replaceData(tasks);

    mTasksView.setVisibility(View.VISIBLE);
    mNoTasksView.setVisibility(View.GONE);
  }

  private void render(TasksListViewData value) {
    // Make sure setRefreshing() is called after the layout is done with everything else.
    mSwipeRefreshLayout.setRefreshing(value.loading());
    mFilteringLabelView.setText(value.filterLabel());
    value
        .viewState()
        .match(
            awaitingTasks -> showNoTasksViewState(),
            emptyTasks -> showEmptyTaskState(emptyTasks.viewData()),
            hasTasks -> showTasks(hasTasks.taskViewData()));
  }
}
