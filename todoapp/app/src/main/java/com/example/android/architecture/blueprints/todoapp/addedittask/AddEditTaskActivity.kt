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
package com.example.android.architecture.blueprints.todoapp.addedittask

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.TaskBundlePacker
import com.example.android.architecture.blueprints.todoapp.util.ActivityUtils.addFragmentToActivity
import com.google.common.base.Preconditions.checkNotNull

/** Displays an add or edit task screen.  */
class AddEditTaskActivity : AppCompatActivity() {

    private var mActionBar: ActionBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.addtask_act)

        // Set up the toolbar.
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        if (toolbar != null) {
            setSupportActionBar(toolbar)
            supportActionBar?.let {
                it.setDisplayHomeAsUpEnabled(true)
                it.setDisplayShowHomeEnabled(true)
            }
        }

        val task: Task?
        val extras = intent.extras
        if (extras != null && extras.containsKey("task_to_edit")) {
            val bundledTask = checkNotNull(extras.getBundle("task_to_edit"))
            task = TaskBundlePacker.taskFromBundle(bundledTask)
            setToolbarTitle(R.string.edit_task)
        } else {
            task = null
            setToolbarTitle(R.string.add_task)
        }

        if (supportFragmentManager.findFragmentById(R.id.contentFrame) == null) {

            addFragmentToActivity(supportFragmentManager,
                    task?.let { edit(it) } ?: createNewTask(),
                    R.id.contentFrame)
        }
    }

    private fun createNewTask() = AddEditTaskFragment.newInstanceForTaskCreation()

    private fun edit(task: Task) = AddEditTaskFragment.newInstanceForTaskUpdate(task)

    private fun setToolbarTitle(stringResource: Int) = supportActionBar?.let { it.setTitle(stringResource) }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    companion object {

        const val REQUEST_ADD_TASK = 1

        @JvmStatic
        fun editTask(c: Context, task: Task): Intent {
            val i = Intent(c, AddEditTaskActivity::class.java)
            i.putExtra("task_to_edit", TaskBundlePacker.taskToBundle(task))
            return i
        }

        @JvmStatic
        fun addTask(c: Context): Intent {
            return Intent(c, AddEditTaskActivity::class.java)
        }
    }
}
