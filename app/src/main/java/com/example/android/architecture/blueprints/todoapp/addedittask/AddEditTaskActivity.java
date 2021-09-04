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

import static com.google.common.base.Preconditions.checkNotNull;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.TaskBundlePacker;
import com.example.android.architecture.blueprints.todoapp.util.ActivityUtils;

/** Displays an add or edit task screen. */
public class AddEditTaskActivity extends AppCompatActivity {

  public static final int REQUEST_ADD_TASK = 1;

  private ActionBar mActionBar;

  public static Intent editTask(Context c, Task task) {
    Intent i = new Intent(c, AddEditTaskActivity.class);
    i.putExtra("task_to_edit", TaskBundlePacker.taskToBundle(task));
    return i;
  }

  public static Intent addTask(Context c) {
    return new Intent(c, AddEditTaskActivity.class);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.addtask_act);

    // Set up the toolbar.
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    mActionBar = checkNotNull(getSupportActionBar());
    mActionBar.setDisplayHomeAsUpEnabled(true);
    mActionBar.setDisplayShowHomeEnabled(true);

    AddEditTaskFragment addEditTaskFragment =
        (AddEditTaskFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
    Task task;
    Bundle extras = getIntent().getExtras();
    if (extras != null && extras.containsKey("task_to_edit")) {
      Bundle bundledTask = checkNotNull(extras.getBundle("task_to_edit"));
      task = TaskBundlePacker.taskFromBundle(bundledTask);
      setToolbarTitle(task.id());
    } else {
      task = null;
      setToolbarTitle(null);
    }

    if (addEditTaskFragment == null) {

      addEditTaskFragment =
          task == null
              ? AddEditTaskFragment.newInstanceForTaskCreation()
              : AddEditTaskFragment.newInstanceForTaskUpdate(task);

      ActivityUtils.addFragmentToActivity(
          getSupportFragmentManager(), addEditTaskFragment, R.id.contentFrame);
    }
  }

  private void setToolbarTitle(@Nullable String taskId) {
    if (taskId == null) {
      mActionBar.setTitle(R.string.add_task);
    } else {
      mActionBar.setTitle(R.string.edit_task);
    }
  }

  @Override
  public boolean onSupportNavigateUp() {
    onBackPressed();
    return true;
  }
}
