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

import android.app.Activity
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.addedittask.domain.AddEditTaskEvent
import com.example.android.architecture.blueprints.todoapp.addedittask.domain.AddEditTaskModel
import com.example.android.architecture.blueprints.todoapp.addedittask.domain.AddEditTaskModel.Mode.Add
import com.example.android.architecture.blueprints.todoapp.addedittask.domain.AddEditTaskModel.Mode.Edit
import com.example.android.architecture.blueprints.todoapp.addedittask.domain.Model
import com.example.android.architecture.blueprints.todoapp.addedittask.effecthandlers.createEffectHandlers
import com.example.android.architecture.blueprints.todoapp.addedittask.view.AddEditTaskViews
import com.example.android.architecture.blueprints.todoapp.addedittask.view.toAddEditTaskModel
import com.example.android.architecture.blueprints.todoapp.addedittask.view.toBundle
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.TaskBundlePacker
import com.spotify.mobius.MobiusLoop
import io.reactivex.functions.Action

/** Main UI for the add task screen. Users can enter a task title and description.  */
class AddEditTaskFragment : Fragment() {

    private lateinit var mController: MobiusLoop.Controller<AddEditTaskModel, AddEditTaskEvent>

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val fab = activity!!.findViewById<FloatingActionButton>(R.id.fab_edit_task_done)
        val views = AddEditTaskViews(inflater, container!!, fab)

        mController = createController(
                createEffectHandlers(context!!, Action { finishWithResultOk() }, Action { views.showEmptyTaskError() }),
                resolveDefaultModel(savedInstanceState))
        mController.connect(views)

        setHasOptionsMenu(true)
        return views.rootView
    }

    private fun resolveDefaultModel(savedInstanceState: Bundle?): Model {
        if (savedInstanceState != null && savedInstanceState.containsKey(ADD_EDIT_TASK_MODEL_RESTORE_KEY)) {
            return savedInstanceState.getBundle(ADD_EDIT_TASK_MODEL_RESTORE_KEY).toAddEditTaskModel()
        }

        val arguments = arguments
        if (arguments != null && arguments.containsKey(TASK_ARGUMENT)) {
            val (id, details) = TaskBundlePacker.taskFromBundle(arguments.getBundle(TASK_ARGUMENT))
            return Model(details = details, mode = Edit(id))
        }

        return Model(mode = Add)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBundle(ADD_EDIT_TASK_MODEL_RESTORE_KEY, mController.model.toBundle())
    }

    override fun onResume() {
        super.onResume()
        mController.start()
    }

    override fun onPause() {
        mController.stop()
        super.onPause()
    }

    private fun finishWithResultOk() {
        activity!!.setResult(Activity.RESULT_OK)
        activity!!.finish()
    }

    companion object {

        const val TASK_ARGUMENT = "task"
        const val ADD_EDIT_TASK_MODEL_RESTORE_KEY = "add_edit_task_model"

        @JvmStatic
        fun newInstanceForTaskCreation(): AddEditTaskFragment {
            return AddEditTaskFragment()
        }

        @JvmStatic
        fun newInstanceForTaskUpdate(task: Task): AddEditTaskFragment {
            val fragment = AddEditTaskFragment()
            val b = Bundle()
            b.putBundle("task", TaskBundlePacker.taskToBundle(task))
            fragment.arguments = b
            return fragment
        }
    }
}
