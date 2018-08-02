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
package com.example.android.architecture.blueprints.todoapp.tasks

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v7.widget.PopupMenu
import android.view.*
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskActivity
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.taskdetail.TaskDetailActivity
import com.example.android.architecture.blueprints.todoapp.tasks.domain.*
import com.example.android.architecture.blueprints.todoapp.tasks.domain.tasksListModelFromBundle
import com.example.android.architecture.blueprints.todoapp.tasks.domain.tasksListModelToBundle
import com.example.android.architecture.blueprints.todoapp.tasks.effecthandlers.createEffectHandler
import com.example.android.architecture.blueprints.todoapp.tasks.view.DeferredEventSource
import com.example.android.architecture.blueprints.todoapp.tasks.view.TasksViews
import com.example.android.architecture.blueprints.todoapp.tasks.view.tasksListModelToViewData
import com.example.android.architecture.blueprints.todoapp.util.contramap
import com.spotify.mobius.MobiusLoop
import io.reactivex.subjects.PublishSubject

/** Display a grid of [Task]s. User can choose to view all, active or completed allTasks.  */
class TasksFragment : Fragment() {

    private lateinit var mController: MobiusLoop.Controller<TasksListModel, TasksListEvent>
    private val mMenuEvents = PublishSubject.create<TasksListEvent>()
    private lateinit var mViews: TasksViews
    private val mEventSource = DeferredEventSource<TasksListEvent>()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View? {

        val fab = activity!!.findViewById<FloatingActionButton>(R.id.fab_add_task)
        mViews = TasksViews(inflater, container!!, fab, mMenuEvents)

        mController = createController(
                createEffectHandler(context!!, mViews, this::showAddTask, this::showTaskDetailsUi),
                mEventSource,
                resolveDefaultModel(savedInstanceState))

        mController.connect(mViews.contramap(::tasksListModelToViewData))
        setHasOptionsMenu(true)
        return mViews.rootView
    }

    private fun resolveDefaultModel(savedInstanceState: Bundle?): TasksListModel {
        return if (savedInstanceState != null)
            tasksListModelFromBundle(savedInstanceState.getBundle("model")!!)
        else
            TasksListModel()
    }

    override fun onResume() {
        super.onResume()
        mController.start()
    }

    override fun onPause() {
        mController.stop()
        super.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBundle("model", tasksListModelToBundle(mController.model))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (AddEditTaskActivity.REQUEST_ADD_TASK == requestCode && Activity.RESULT_OK == resultCode) {
            mEventSource.notifyEvent(TaskCreated)
        }
    }

    override fun onDestroyView() {
        mController.disconnect()
        super.onDestroyView()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.tasks_fragment_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_clear -> mMenuEvents.onNext(ClearCompletedTasksRequested)
            R.id.menu_filter -> showFilteringPopUpMenu()
            R.id.menu_refresh -> mMenuEvents.onNext(RefreshRequested)
        }
        return true
    }

    private fun onFilterSelected(filter: TasksFilterType) {
        mMenuEvents.onNext(FilterSelected(filter))
    }

    private fun showFilteringPopUpMenu() {
        val popup = PopupMenu(context!!, activity!!.findViewById(R.id.menu_filter))
        popup.menuInflater.inflate(R.menu.filter_tasks, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.active -> onFilterSelected(TasksFilterType.ACTIVE_TASKS)
                R.id.completed -> onFilterSelected(TasksFilterType.COMPLETED_TASKS)
                else -> onFilterSelected(TasksFilterType.ALL_TASKS)
            }
            true
        }

        popup.show()
    }

    fun showAddTask() {
        startActivityForResult(
                AddEditTaskActivity.addTask(context!!), AddEditTaskActivity.REQUEST_ADD_TASK)
    }

    fun showTaskDetailsUi(task: Task) {
        // in it's own Activity, since it makes more sense that way and it gives us the flexibility
        // to show some Intent stubbing.
        startActivity(TaskDetailActivity.showTask(context, task))
    }

    companion object {

        @JvmStatic
        fun newInstance(): TasksFragment {
            return TasksFragment()
        }
    }
}
