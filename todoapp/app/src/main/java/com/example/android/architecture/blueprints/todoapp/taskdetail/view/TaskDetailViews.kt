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
package com.example.android.architecture.blueprints.todoapp.taskdetail.view

import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.taskdetail.domain.ActivateTaskRequested
import com.example.android.architecture.blueprints.todoapp.taskdetail.domain.CompleteTaskRequested
import com.example.android.architecture.blueprints.todoapp.taskdetail.domain.EditTaskRequested
import com.example.android.architecture.blueprints.todoapp.taskdetail.domain.TaskDetailEvent
import com.example.android.architecture.blueprints.todoapp.util.onAccept
import com.spotify.mobius.Connectable
import com.spotify.mobius.Connection
import com.spotify.mobius.functions.Consumer
import io.reactivex.Observable

class TaskDetailViews(
        inflater: LayoutInflater,
        container: ViewGroup,
        private val mFab: FloatingActionButton,
        private val mMenuEvents: Observable<TaskDetailEvent>) : TaskDetailViewActions, Connectable<TaskDetailViewData, TaskDetailEvent> {

    val rootView: View = inflater.inflate(R.layout.taskdetail_frag, container, false)

    private val mDetailTitle: TextView = rootView.findViewById(R.id.task_detail_title)

    private val mDetailDescription: TextView = rootView.findViewById(R.id.task_detail_description)

    private val mDetailCompleteStatus: CheckBox = rootView.findViewById(R.id.task_detail_complete)

    override fun showTaskMarkedComplete() {
        Snackbar.make(rootView, R.string.task_marked_complete, Snackbar.LENGTH_LONG).show()
    }

    override fun showTaskMarkedActive() {
        Snackbar.make(rootView, R.string.task_marked_active, Snackbar.LENGTH_LONG).show()
    }

    override fun showTaskDeletionFailed() {
        Snackbar.make(rootView, "Failed to delete task", Snackbar.LENGTH_LONG).show()
    }

    override fun showTaskSavingFailed() {
        Snackbar.make(rootView, "Failed to save change", Snackbar.LENGTH_LONG).show()
    }

    private fun render(viewData: TaskDetailViewData) {
        mDetailCompleteStatus.isChecked = viewData.completedChecked
        bindTextViewData(mDetailTitle, viewData.title)
        bindTextViewData(mDetailDescription, viewData.description)
    }

    private fun bindTextViewData(textView: TextView, viewData: TextViewData) {
        textView.visibility = viewData.visibility
        textView.text = viewData.text
    }

    override fun connect(output: Consumer<TaskDetailEvent>): Connection<TaskDetailViewData> {
        mFab.setOnClickListener { _ -> output.accept(EditTaskRequested) }

        mDetailCompleteStatus.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                output.accept(CompleteTaskRequested)
            } else {
                output.accept(ActivateTaskRequested)
            }
        }

        val disposable = mMenuEvents
                .retry()
                .subscribe(
                        { output.accept(it) },
                        { Log.e(TaskDetailViews::class.java.simpleName, "Menu events seem to fail", it) })

        return onAccept<TaskDetailViewData> { render(it) }
                .onDispose {
                    disposable.dispose()
                    mFab.setOnClickListener(null)
                    mDetailCompleteStatus.setOnCheckedChangeListener(null)
                }
    }
}
