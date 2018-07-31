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
package com.example.android.architecture.blueprints.todoapp.data.source.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class TasksDbHelper(
    context: Context
) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

  override fun onCreate(db: SQLiteDatabase) {
    db.execSQL(SQL_CREATE_ENTRIES)
  }

  override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    // Not required as at version 1
  }

  override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    // Not required as at version 1
  }

  private companion object {
    const val DATABASE_VERSION = 1
    const val DATABASE_NAME = "Tasks.db"
    const val TEXT_TYPE = " TEXT"
    const val BOOLEAN_TYPE = " INTEGER"
    const val COMMA_SEP = ","

    const val SQL_CREATE_ENTRIES = (
        "CREATE TABLE "
            + TasksPersistenceContract.TaskEntry.TABLE_NAME
            + " ("
            + TasksPersistenceContract.TaskEntry.COLUMN_NAME_ENTRY_ID
            + TEXT_TYPE
            + " PRIMARY KEY,"
            + TasksPersistenceContract.TaskEntry.COLUMN_NAME_TITLE
            + TEXT_TYPE
            + COMMA_SEP
            + TasksPersistenceContract.TaskEntry.COLUMN_NAME_DESCRIPTION
            + TEXT_TYPE
            + COMMA_SEP
            + TasksPersistenceContract.TaskEntry.COLUMN_NAME_COMPLETED
            + BOOLEAN_TYPE
            + " )")
  }
}
