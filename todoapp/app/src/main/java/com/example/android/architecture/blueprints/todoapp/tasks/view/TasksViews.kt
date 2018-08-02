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
package com.example.android.architecture.blueprints.todoapp.tasks.view

import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.tasks.domain.*
import com.example.android.architecture.blueprints.todoapp.util.onAccept
import com.spotify.mobius.Connectable
import com.spotify.mobius.Connection
import com.spotify.mobius.functions.Consumer
import io.reactivex.Observable

class TasksViews(
        inflater: LayoutInflater,
        parent: ViewGroup,
        private val mFab: FloatingActionButton,
        private val menuEvents: Observable<TasksListEvent>)
    : TasksListViewActions, Connectable<TasksListViewData, TasksListEvent> {
    val rootView: View = inflater.inflate(R.layout.tasks_frag, parent, false)

    private val mListAdapter: TasksAdapter = TasksAdapter()

    private val mSwipeRefreshLayout: ScrollChildSwipeRefreshLayout = rootView.findViewById(R.id.refresh_layout)

    private val mNoTasksView: View = rootView.findViewById(R.id.noTasks)

    private val mNoTaskIcon: ImageView = rootView.findViewById(R.id.noTasksIcon)

    private val mNoTaskMainView: TextView = rootView.findViewById(R.id.noTasksMain)

    private val mNoTaskAddView: TextView = rootView.findViewById(R.id.noTasksAdd)

    private val mTasksView: LinearLayout = rootView.findViewById(R.id.tasksLL)

    private val mFilteringLabelView: TextView = rootView.findViewById(R.id.filteringLabel)

    init {
        // Set up allTasks view
        val listView = rootView.findViewById<ListView>(R.id.tasks_list)
        listView.adapter = mListAdapter
        // Set up  no allTasks view
        mFab.setImageResource(R.drawable.ic_add)
        // Set up progress indicator
        mSwipeRefreshLayout.setColorSchemeColors(
                ContextCompat.getColor(rootView.context, R.color.colorPrimary),
                ContextCompat.getColor(rootView.context, R.color.colorAccent),
                ContextCompat.getColor(rootView.context, R.color.colorPrimaryDark))
        // Set the scrolling view in the custom SwipeRefreshLayout.
        mSwipeRefreshLayout.setScrollUpChild(listView)
    }

    override fun showSuccessfullySavedMessage() {
        showMessage(R.string.successfully_saved_task_message)
    }

    override fun showTaskMarkedComplete() {
        showMessage(R.string.task_marked_complete)
    }

    override fun showTaskMarkedActive() {
        showMessage(R.string.task_marked_active)
    }

    override fun showCompletedTasksCleared() {
        showMessage(R.string.completed_tasks_cleared)
    }

    override fun showLoadingTasksError() {
        showMessage(R.string.loading_tasks_error)
    }

    private fun showMessage(messageRes: Int) {
        Snackbar.make(rootView, messageRes, Snackbar.LENGTH_LONG).show()
    }

    override fun connect(output: Consumer<TasksListEvent>): Connection<TasksListViewData> {

        addUiListeners(output)
        val disposable = menuEvents.subscribe({ output.accept(it) }, {})

        return onAccept<TasksListViewData> { render(it) }
                .onDispose {
                    disposable.dispose()
                    mNoTaskAddView.setOnClickListener(null)
                    mFab.setOnClickListener(null)
                    mSwipeRefreshLayout.setOnRefreshListener(null)
                    mListAdapter.setItemListener(null)
                }
    }

    private fun addUiListeners(output: Consumer<TasksListEvent>) {
        mNoTaskAddView.setOnClickListener { output.accept(NewTaskClicked) }
        mFab.setOnClickListener { output.accept(NewTaskClicked) }
        mSwipeRefreshLayout.setOnRefreshListener { output.accept(RefreshRequested) }
        mListAdapter.setItemListener(
                object : TasksAdapter.TaskItemListener {
                    override fun onTaskClick(id: String) {
                        output.accept(NavigateToTaskDetailsRequested(id))
                    }

                    override fun onCompleteTaskClick(id: String) {
                        output.accept(TaskMarkedComplete(id))
                    }

                    override fun onActivateTaskClick(id: String) {
                        output.accept(TaskMarkedActive(id))
                    }
                })
    }

    private fun showEmptyTaskState(vd: EmptyTasksViewData) {
        mTasksView.visibility = View.GONE
        mNoTasksView.visibility = View.VISIBLE

        mNoTaskMainView.setText(vd.title)
        mNoTaskIcon.setImageResource(vd.icon)
        mNoTaskAddView.visibility = vd.addButtonVisibility
    }

    private fun showNoTasksViewState() {
        mTasksView.visibility = View.GONE
        mNoTasksView.visibility = View.GONE
    }

    private fun showTasks(tasks: List<TaskViewData>) {
        mListAdapter.replaceData(tasks)

        mTasksView.visibility = View.VISIBLE
        mNoTasksView.visibility = View.GONE
    }

    private fun render(value: TasksListViewData) {
        // Make sure setRefreshing() is called after the layout is done with everything else.
        mSwipeRefreshLayout.isRefreshing = value.loading
        mFilteringLabelView.setText(value.filterLabel)
        when(value.viewState) {
            is HasTasks -> showTasks(value.viewState.tasks)
            AwaitingTasks -> showNoTasksViewState()
            is EmptyTasks -> showEmptyTaskState(value.viewState.viewData)
            else -> throw RuntimeException("Unhandled case")
        }
    }
}
