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
package com.example.android.architecture.blueprints.todoapp.addedittask.view;

import static com.example.android.architecture.blueprints.todoapp.addedittask.domain.AddEditTaskEvent.taskDefinitionCompleted;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.addedittask.domain.AddEditTaskEvent;
import com.example.android.architecture.blueprints.todoapp.addedittask.domain.AddEditTaskModel;
import com.spotify.mobius.Connectable;
import com.spotify.mobius.Connection;
import com.spotify.mobius.functions.Consumer;
import javax.annotation.Nonnull;

public class AddEditTaskViews implements Connectable<AddEditTaskModel, AddEditTaskEvent> {
  private final View mRoot;
  private final FloatingActionButton mFab;
  private final TextView mTitle;
  private final TextView mDescription;

  public AddEditTaskViews(LayoutInflater inflater, ViewGroup parent, FloatingActionButton fab) {
    mRoot = inflater.inflate(R.layout.addtask_frag, parent, false);
    mTitle = mRoot.findViewById(R.id.add_task_title);
    mDescription = mRoot.findViewById(R.id.add_task_description);
    fab.setImageResource(R.drawable.ic_done);
    mFab = fab;
  }

  public View getRootView() {
    return mRoot;
  }

  public void showEmptyTaskError() {
    Snackbar.make(mTitle, R.string.empty_task_message, Snackbar.LENGTH_LONG).show();
  }

  public void setTitle(String title) {
    mTitle.setText(title);
  }

  public void setDescription(String description) {
    mDescription.setText(description);
  }

  @Nonnull
  @Override
  public Connection<AddEditTaskModel> connect(Consumer<AddEditTaskEvent> output) {
    mFab.setOnClickListener(
        __ ->
            output.accept(
                taskDefinitionCompleted(
                    mTitle.getText().toString(), mDescription.getText().toString())));
    return new Connection<AddEditTaskModel>() {
      @Override
      public void accept(AddEditTaskModel model) {
        setTitle(model.details().title());
        setDescription(model.details().description());
      }

      @Override
      public void dispose() {
        mFab.setOnClickListener(null);
      }
    };
  }
}
