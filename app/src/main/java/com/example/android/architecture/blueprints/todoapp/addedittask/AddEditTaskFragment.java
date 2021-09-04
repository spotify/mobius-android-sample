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
package com.example.android.architecture.blueprints.todoapp.addedittask;

import static com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskInjector.createController;
import static com.example.android.architecture.blueprints.todoapp.addedittask.effecthandlers.AddEditTaskEffectHandlers.createEffectHandlers;
import static com.example.android.architecture.blueprints.todoapp.addedittask.view.AddEditTaskModeBundlePacker.addEditTaskModelToBundle;
import static com.google.common.base.Preconditions.checkNotNull;

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.addedittask.domain.AddEditTaskEvent;
import com.example.android.architecture.blueprints.todoapp.addedittask.domain.AddEditTaskMode;
import com.example.android.architecture.blueprints.todoapp.addedittask.domain.AddEditTaskModel;
import com.example.android.architecture.blueprints.todoapp.addedittask.view.AddEditTaskModeBundlePacker;
import com.example.android.architecture.blueprints.todoapp.addedittask.view.AddEditTaskViews;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.TaskBundlePacker;
import com.example.android.architecture.blueprints.todoapp.data.TaskDetails;
import com.spotify.mobius.MobiusLoop;

/** Main UI for the add task screen. Users can enter a task title and description. */
public class AddEditTaskFragment extends Fragment {

  public static final String TASK_ARGUMENT = "task";
  public static final String ADD_EDIT_TASK_MODEL_RESTORE_KEY = "add_edit_task_model";

  private MobiusLoop.Controller<AddEditTaskModel, AddEditTaskEvent> mController;

  public static AddEditTaskFragment newInstanceForTaskCreation() {
    return new AddEditTaskFragment();
  }

  public static AddEditTaskFragment newInstanceForTaskUpdate(Task task) {
    AddEditTaskFragment fragment = new AddEditTaskFragment();
    Bundle b = new Bundle();
    b.putBundle("task", TaskBundlePacker.taskToBundle(task));
    fragment.setArguments(b);
    return fragment;
  }

  @Nullable
  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    FloatingActionButton fab = getActivity().findViewById(R.id.fab_edit_task_done);
    AddEditTaskViews views = new AddEditTaskViews(inflater, container, fab);

    mController =
        createController(
            createEffectHandlers(getContext(), this::finishWithResultOk, views::showEmptyTaskError),
            resolveDefaultModel(savedInstanceState));
    mController.connect(views);

    setHasOptionsMenu(true);
    return views.getRootView();
  }

  @Nullable
  private AddEditTaskModel resolveDefaultModel(Bundle savedInstanceState) {
    Bundle arguments = getArguments();
    if (arguments != null && arguments.containsKey(TASK_ARGUMENT)) {
      Task task = TaskBundlePacker.taskFromBundle(checkNotNull(arguments.getBundle(TASK_ARGUMENT)));
      return AddEditTaskModel.builder()
          .details(task.details())
          .mode(AddEditTaskMode.update(task.id()))
          .build();
    }

    if (savedInstanceState != null
        && savedInstanceState.containsKey(ADD_EDIT_TASK_MODEL_RESTORE_KEY)) {
      return AddEditTaskModeBundlePacker.addEditTaskModelFromBundle(
          checkNotNull(savedInstanceState.getBundle(ADD_EDIT_TASK_MODEL_RESTORE_KEY)));
    }

    return AddEditTaskModel.builder()
        .mode(AddEditTaskMode.create())
        .details(TaskDetails.DEFAULT)
        .build();
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putBundle(
        ADD_EDIT_TASK_MODEL_RESTORE_KEY, addEditTaskModelToBundle(mController.getModel()));
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
  public void onDestroyView() {
    mController.disconnect();
    super.onDestroyView();
  }

  private void finishWithResultOk() {
    getActivity().setResult(Activity.RESULT_OK);
    getActivity().finish();
  }
}
