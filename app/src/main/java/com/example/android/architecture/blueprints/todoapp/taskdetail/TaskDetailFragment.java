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
package com.example.android.architecture.blueprints.todoapp.taskdetail;

import static com.example.android.architecture.blueprints.todoapp.taskdetail.domain.TaskDetailEvent.deleteTaskRequested;
import static com.spotify.mobius.extras.Connectables.contramap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskActivity;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.TaskBundlePacker;
import com.example.android.architecture.blueprints.todoapp.taskdetail.domain.TaskDetailEvent;
import com.example.android.architecture.blueprints.todoapp.taskdetail.effecthandlers.TaskDetailEffectHandlers;
import com.example.android.architecture.blueprints.todoapp.taskdetail.view.TaskDetailViewDataMapper;
import com.example.android.architecture.blueprints.todoapp.taskdetail.view.TaskDetailViews;
import com.spotify.mobius.MobiusLoop;
import io.reactivex.subjects.PublishSubject;

/** Main UI for the task detail screen. */
public class TaskDetailFragment extends Fragment {

  @NonNull private static final String ARGUMENT_TASK = "TASK";

  @NonNull private static final int REQUEST_EDIT_TASK = 1;

  private MobiusLoop.Controller<Task, TaskDetailEvent> mController;
  private TaskDetailViews mTaskDetailsViews;
  private PublishSubject<TaskDetailEvent> mMenuEvents = PublishSubject.create();

  public static TaskDetailFragment newInstance(Task task) {
    Bundle arguments = new Bundle();
    arguments.putBundle(ARGUMENT_TASK, TaskBundlePacker.taskToBundle(task));
    TaskDetailFragment fragment = new TaskDetailFragment();
    fragment.setArguments(arguments);
    return fragment;
  }

  @Nullable
  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    setHasOptionsMenu(true);
    FloatingActionButton fab = getActivity().findViewById(R.id.fab_edit_task);
    mTaskDetailsViews = new TaskDetailViews(inflater, container, fab, mMenuEvents);
    mController =
        TaskDetailInjector.createController(
            TaskDetailEffectHandlers.createEffectHandlers(
                mTaskDetailsViews, getContext(), this::dismiss, this::openTaskEditor),
            resolveDefaultModel(savedInstanceState));
    mController.connect(contramap(TaskDetailViewDataMapper::taskToTaskViewData, mTaskDetailsViews));
    return mTaskDetailsViews.getRootView();
  }

  @NonNull
  private Task resolveDefaultModel(Bundle savedInstanceState) {
    Task t;
    if (savedInstanceState != null && savedInstanceState.containsKey(ARGUMENT_TASK)) {
      t = TaskBundlePacker.taskFromBundle(savedInstanceState.getBundle(ARGUMENT_TASK));
    } else {
      t = TaskBundlePacker.taskFromBundle(getArguments().getBundle(ARGUMENT_TASK));
    }
    return t;
  }

  @Override
  public void onDestroyView() {
    mController.disconnect();
    super.onDestroyView();
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putBundle(ARGUMENT_TASK, TaskBundlePacker.taskToBundle(mController.getModel()));
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
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.menu_delete:
        mMenuEvents.onNext(deleteTaskRequested());
        return true;
    }
    return false;
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.taskdetail_fragment_menu, menu);
    super.onCreateOptionsMenu(menu, inflater);
  }

  public void openTaskEditor(@NonNull Task task) {
    startActivityForResult(AddEditTaskActivity.editTask(getContext(), task), REQUEST_EDIT_TASK);
  }

  private void dismiss() {
    getActivity().finish();
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_EDIT_TASK) {
      // If the task was edited successfully, go back to the list.
      if (resultCode == Activity.RESULT_OK) {
        getActivity().finish();
        return;
      }
    }
    super.onActivityResult(requestCode, resultCode, data);
  }
}
