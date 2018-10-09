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
package com.example.android.architecture.blueprints.todoapp.data.source.local

import com.google.common.base.Preconditions.checkNotNull

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.TaskDetails
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource
import com.example.android.architecture.blueprints.todoapp.data.source.local.TasksPersistenceContract.TaskEntry.Companion.COLUMN_NAME_COMPLETED
import com.example.android.architecture.blueprints.todoapp.data.source.local.TasksPersistenceContract.TaskEntry.Companion.COLUMN_NAME_DESCRIPTION
import com.example.android.architecture.blueprints.todoapp.data.source.local.TasksPersistenceContract.TaskEntry.Companion.COLUMN_NAME_ENTRY_ID
import com.example.android.architecture.blueprints.todoapp.data.source.local.TasksPersistenceContract.TaskEntry.Companion.COLUMN_NAME_TITLE
import com.example.android.architecture.blueprints.todoapp.data.source.local.TasksPersistenceContract.TaskEntry.Companion.TABLE_NAME
import com.example.android.architecture.blueprints.todoapp.util.schedulers.BaseSchedulerProvider
import com.google.common.base.Optional
import com.squareup.sqlbrite2.BriteDatabase
import com.squareup.sqlbrite2.SqlBrite
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable

/** Concrete implementation of a data source as a db.  */
class TasksLocalDataSource private constructor(
    context: Context,
    schedulerProvider: BaseSchedulerProvider
) : TasksDataSource {

  private val databaseHelper: BriteDatabase
  private val taskMapperFunction: (Cursor) -> (Task)

  init {
    val dbHelper = TasksDbHelper(context)
    val sqlBrite = SqlBrite.Builder().build()
    databaseHelper = sqlBrite.wrapDatabaseHelper(dbHelper, schedulerProvider.io())
    taskMapperFunction = { this.getTask(it) }
  }

  override fun getTasks(): Flowable<List<Task>> {
    val projection = listOf(
        COLUMN_NAME_ENTRY_ID,
        COLUMN_NAME_TITLE,
        COLUMN_NAME_DESCRIPTION,
        COLUMN_NAME_COMPLETED
    ).joinToString(",")
    val sql = "SELECT $projection FROM $TABLE_NAME"
    return databaseHelper
        .createQuery(TABLE_NAME, sql)
        .mapToList(taskMapperFunction)
        .toFlowable(BackpressureStrategy.BUFFER)
  }

  private fun getTask(c: Cursor): Task {
    fun indexString(column: String) = c.getString(c.getColumnIndexOrThrow(column))
    fun indexInt(column: String) = c.getInt(c.getColumnIndexOrThrow(column))

    val itemId = indexString(COLUMN_NAME_ENTRY_ID)
    val title = indexString(COLUMN_NAME_TITLE)
    val description = indexString(COLUMN_NAME_DESCRIPTION)
    val completed = indexInt(COLUMN_NAME_COMPLETED) == 1
    val details = TaskDetails.builder().title(title).description(description).completed(completed).build()
    return Task.create(itemId, details)
  }

  override fun getTask(taskId: String): Flowable<Optional<Task>> {
    val projection = listOf(
        COLUMN_NAME_ENTRY_ID,
        COLUMN_NAME_TITLE,
        COLUMN_NAME_DESCRIPTION,
        COLUMN_NAME_COMPLETED
    ).joinToString(",")
    val sql =
            "SELECT $projection " +
            "FROM $TABLE_NAME " +
            "WHERE $COLUMN_NAME_ENTRY_ID LIKE ?"
    return databaseHelper
        .createQuery(TABLE_NAME, sql, taskId)
        .mapToOneOrDefault(
            { Optional.of(taskMapperFunction(it)) }, Optional.absent<Task>())
        .toFlowable(BackpressureStrategy.BUFFER)
  }

  override fun saveTask(task: Task) {
    checkNotNull(task)
    val values = ContentValues().apply {
      put(COLUMN_NAME_ENTRY_ID, task.id)
      put(COLUMN_NAME_TITLE, task.details.title)
      put(COLUMN_NAME_DESCRIPTION, task.details.description)
      put(COLUMN_NAME_COMPLETED, task.details.completed)
    }
    databaseHelper.insert(TABLE_NAME, values, SQLiteDatabase.CONFLICT_REPLACE)
  }

  override fun deleteAllTasks() {
    databaseHelper.delete(TABLE_NAME, null)
  }

  override fun deleteTask(taskId: String) {
    val selection = "$COLUMN_NAME_ENTRY_ID LIKE ?"
    val selectionArgs = arrayOf(taskId)
    databaseHelper.delete(TABLE_NAME, selection, *selectionArgs)
  }

  companion object {
    private var INSTANCE: TasksLocalDataSource? = null

    @JvmStatic
    fun getInstance(
        context: Context,
        schedulerProvider: BaseSchedulerProvider
    ) : TasksLocalDataSource {
      if (INSTANCE == null) {
        INSTANCE = TasksLocalDataSource(context, schedulerProvider)
      }
      return INSTANCE as TasksLocalDataSource
    }
  }
}
