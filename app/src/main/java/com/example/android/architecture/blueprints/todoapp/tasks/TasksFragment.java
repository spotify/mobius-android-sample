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
package com.example.android.architecture.blueprints.todoapp.tasks;

import static com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksListEvent.clearCompletedTasksRequested;
import static com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksListEvent.filterSelected;
import static com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksListEvent.refreshRequested;
import static com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksListEvent.taskCreated;
import static com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksListModelBundlePacker.tasksListModelFromBundle;
import static com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksListModelBundlePacker.tasksListModelToBundle;
import static com.example.android.architecture.blueprints.todoapp.tasks.effecthandlers.TasksListEffectHandlers.createEffectHandler;
import static com.spotify.mobius.extras.Connectables.contramap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskActivity;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.taskdetail.TaskDetailActivity;
import com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksFilterType;
import com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksListEvent;
import com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksListModel;
import com.example.android.architecture.blueprints.todoapp.tasks.view.DeferredEventSource;
import com.example.android.architecture.blueprints.todoapp.tasks.view.TasksListViewDataMapper;
import com.example.android.architecture.blueprints.todoapp.tasks.view.TasksViews;
import com.spotify.mobius.MobiusLoop;
import io.reactivex.subjects.PublishSubject;

/** Display a grid of {@link Task}s. User can choose to view all, active or completed allTasks. */
public class TasksFragment extends Fragment {

  private MobiusLoop.Controller<TasksListModel, TasksListEvent> mController;
  private PublishSubject<TasksListEvent> mMenuEvents = PublishSubject.create();
  private TasksViews mViews;
  private DeferredEventSource<TasksListEvent> mEventSource = new DeferredEventSource<>();

  public static TasksFragment newInstance() {
    return new TasksFragment();
  }

  @Nullable
  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    FloatingActionButton fab = getActivity().findViewById(R.id.fab_add_task);
    mViews = new TasksViews(inflater, container, fab, mMenuEvents);

    mController =
        TasksInjector.createController(
            createEffectHandler(getContext(), mViews, this::showAddTask, this::showTaskDetailsUi),
            mEventSource,
            resolveDefaultModel(savedInstanceState));

    mController.connect(contramap(TasksListViewDataMapper::tasksListModelToViewData, mViews));
    setHasOptionsMenu(true);
    return mViews.getRootView();
  }

  private TasksListModel resolveDefaultModel(@Nullable Bundle savedInstanceState) {
    return savedInstanceState != null
        ? tasksListModelFromBundle(savedInstanceState.getBundle("model"))
        : TasksListModel.DEFAULT;
  }

  @Override
  public void onResume() {
    super.onResume();
    mController.start();
  }

  @Override
  public void onPause() {
    mController.stop();
    super.onPause();
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putBundle("model", tasksListModelToBundle(mController.getModel()));
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (AddEditTaskActivity.REQUEST_ADD_TASK == requestCode && Activity.RESULT_OK == resultCode) {
      mEventSource.notifyEvent(taskCreated());
    }
  }

  @Override
  public void onDestroyView() {
    mController.disconnect();
    super.onDestroyView();
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.tasks_fragment_menu, menu);
    super.onCreateOptionsMenu(menu, inflater);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.menu_clear:
        mMenuEvents.onNext(clearCompletedTasksRequested());
        break;
      case R.id.menu_filter:
        showFilteringPopUpMenu();
        break;
      case R.id.menu_refresh:
        mMenuEvents.onNext(refreshRequested());
        break;
    }
    return true;
  }

  private void onFilterSelected(TasksFilterType filter) {
    mMenuEvents.onNext(filterSelected(filter));
  }

  private void showFilteringPopUpMenu() {
    PopupMenu popup = new PopupMenu(getContext(), getActivity().findViewById(R.id.menu_filter));
    popup.getMenuInflater().inflate(R.menu.filter_tasks, popup.getMenu());

    popup.setOnMenuItemClickListener(
        item -> {
          switch (item.getItemId()) {
            case R.id.active:
              onFilterSelected(TasksFilterType.ACTIVE_TASKS);
              break;
            case R.id.completed:
              onFilterSelected(TasksFilterType.COMPLETED_TASKS);
              break;
            default:
              onFilterSelected(TasksFilterType.ALL_TASKS);
              break;
          }
          return true;
        });

    popup.show();
  }

  public void showAddTask() {
    startActivityForResult(
        AddEditTaskActivity.addTask(getContext()), AddEditTaskActivity.REQUEST_ADD_TASK);
  }

  public void showTaskDetailsUi(Task task) {
    // in it's own Activity, since it makes more sense that way and it gives us the flexibility
    // to show some Intent stubbing.
    startActivity(TaskDetailActivity.showTask(getContext(), task));
  }
}
