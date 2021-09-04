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
package com.example.android.architecture.blueprints.todoapp.data.source.local;

import static com.google.common.base.Preconditions.checkNotNull;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.TaskDetails;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.local.TasksPersistenceContract.TaskEntry;
import com.example.android.architecture.blueprints.todoapp.util.schedulers.BaseSchedulerProvider;
import com.google.common.base.Optional;
import com.squareup.sqlbrite2.BriteDatabase;
import com.squareup.sqlbrite2.SqlBrite;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.functions.Function;
import java.util.List;

/** Concrete implementation of a data source as a db. */
public class TasksLocalDataSource implements TasksDataSource {

  @Nullable private static TasksLocalDataSource INSTANCE;

  @NonNull private final BriteDatabase mDatabaseHelper;

  @NonNull private Function<Cursor, Task> mTaskMapperFunction;

  // Prevent direct instantiation.
  private TasksLocalDataSource(
      @NonNull Context context, @NonNull BaseSchedulerProvider schedulerProvider) {
    checkNotNull(context, "context cannot be null");
    checkNotNull(schedulerProvider, "scheduleProvider cannot be null");
    TasksDbHelper dbHelper = new TasksDbHelper(context);
    SqlBrite sqlBrite = new SqlBrite.Builder().build();
    mDatabaseHelper = sqlBrite.wrapDatabaseHelper(dbHelper, schedulerProvider.io());
    mTaskMapperFunction = this::getTask;
  }

  @NonNull
  private Task getTask(@NonNull Cursor c) {
    String itemId = c.getString(c.getColumnIndexOrThrow(TaskEntry.COLUMN_NAME_ENTRY_ID));
    String title = c.getString(c.getColumnIndexOrThrow(TaskEntry.COLUMN_NAME_TITLE));
    String description = c.getString(c.getColumnIndexOrThrow(TaskEntry.COLUMN_NAME_DESCRIPTION));
    boolean completed = c.getInt(c.getColumnIndexOrThrow(TaskEntry.COLUMN_NAME_COMPLETED)) == 1;
    TaskDetails details =
        TaskDetails.builder().title(title).description(description).completed(completed).build();
    return Task.create(itemId, details);
  }

  public static TasksLocalDataSource getInstance(
      @NonNull Context context, @NonNull BaseSchedulerProvider schedulerProvider) {
    if (INSTANCE == null) {
      INSTANCE = new TasksLocalDataSource(context, schedulerProvider);
    }
    return INSTANCE;
  }

  public static void destroyInstance() {
    INSTANCE = null;
  }

  @Override
  public Flowable<List<Task>> getTasks() {
    String[] projection = {
      TaskEntry.COLUMN_NAME_ENTRY_ID,
      TaskEntry.COLUMN_NAME_TITLE,
      TaskEntry.COLUMN_NAME_DESCRIPTION,
      TaskEntry.COLUMN_NAME_COMPLETED
    };
    String sql =
        String.format("SELECT %s FROM %s", TextUtils.join(",", projection), TaskEntry.TABLE_NAME);
    return mDatabaseHelper
        .createQuery(TaskEntry.TABLE_NAME, sql)
        .mapToList(mTaskMapperFunction)
        .toFlowable(BackpressureStrategy.BUFFER);
  }

  @Override
  public Flowable<Optional<Task>> getTask(@NonNull String taskId) {
    String[] projection = {
      TaskEntry.COLUMN_NAME_ENTRY_ID,
      TaskEntry.COLUMN_NAME_TITLE,
      TaskEntry.COLUMN_NAME_DESCRIPTION,
      TaskEntry.COLUMN_NAME_COMPLETED
    };
    String sql =
        String.format(
            "SELECT %s FROM %s WHERE %s LIKE ?",
            TextUtils.join(",", projection), TaskEntry.TABLE_NAME, TaskEntry.COLUMN_NAME_ENTRY_ID);
    return mDatabaseHelper
        .createQuery(TaskEntry.TABLE_NAME, sql, taskId)
        .mapToOneOrDefault(
            cursor -> Optional.of(mTaskMapperFunction.apply(cursor)), Optional.<Task>absent())
        .toFlowable(BackpressureStrategy.BUFFER);
  }

  @Override
  public void saveTask(@NonNull Task task) {
    checkNotNull(task);
    ContentValues values = new ContentValues();
    values.put(TaskEntry.COLUMN_NAME_ENTRY_ID, task.id());
    values.put(TaskEntry.COLUMN_NAME_TITLE, task.details().title());
    values.put(TaskEntry.COLUMN_NAME_DESCRIPTION, task.details().description());
    values.put(TaskEntry.COLUMN_NAME_COMPLETED, task.details().completed());
    mDatabaseHelper.insert(TaskEntry.TABLE_NAME, values, SQLiteDatabase.CONFLICT_REPLACE);
  }

  @Override
  public void deleteAllTasks() {
    mDatabaseHelper.delete(TaskEntry.TABLE_NAME, null);
  }

  @Override
  public void deleteTask(@NonNull String taskId) {
    String selection = TaskEntry.COLUMN_NAME_ENTRY_ID + " LIKE ?";
    String[] selectionArgs = {taskId};
    mDatabaseHelper.delete(TaskEntry.TABLE_NAME, selection, selectionArgs);
  }
}
