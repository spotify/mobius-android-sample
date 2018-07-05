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
package com.example.android.architecture.blueprints.todoapp.addedittask.view

import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.addedittask.domain.AddEditTaskEvent
import com.example.android.architecture.blueprints.todoapp.addedittask.domain.AddEditTaskModel
import com.example.android.architecture.blueprints.todoapp.addedittask.domain.TaskDefinitionCompleted
import com.example.android.architecture.blueprints.todoapp.util.onAccept
import com.spotify.mobius.Connectable
import com.spotify.mobius.Connection
import com.spotify.mobius.functions.Consumer

class AddEditTaskViews(inflater: LayoutInflater, parent: ViewGroup, private val mFab: FloatingActionButton)
    : Connectable<AddEditTaskModel, AddEditTaskEvent> {
    val rootView: View = inflater.inflate(R.layout.addtask_frag, parent, false)
    private val mTitle: TextView
    private val mDescription: TextView

    init {
        mTitle = rootView.findViewById(R.id.add_task_title)
        mDescription = rootView.findViewById(R.id.add_task_description)
        mFab.setImageResource(R.drawable.ic_done)
    }

    fun showEmptyTaskError() {
        Snackbar.make(mTitle, R.string.empty_task_message, Snackbar.LENGTH_LONG).show()
    }

    fun setTitle(title: String) {
        mTitle.text = title
    }

    fun setDescription(description: String) {
        mDescription.text = description
    }

    fun render(m: AddEditTaskModel) = with(m) {
        setTitle(details.title)
        setDescription(details.description)
    }

    override fun connect(output: Consumer<AddEditTaskEvent>): Connection<AddEditTaskModel> {
        mFab.setOnClickListener {
            output.accept(
                    TaskDefinitionCompleted(mTitle.text.toString(), mDescription.text.toString()))
        }

        return onAccept<AddEditTaskModel> { render(it) }
                .onDispose { mFab.setOnClickListener(null) }
    }
}
