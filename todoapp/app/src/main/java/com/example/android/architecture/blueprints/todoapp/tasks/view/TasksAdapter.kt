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
package com.example.android.architecture.blueprints.todoapp.tasks.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.TextView
import com.example.android.architecture.blueprints.todoapp.R

internal class TasksAdapter : BaseAdapter() {

    private var mTasks: List<TaskViewData>? = null
    private var mItemListener: TaskItemListener? = null

    fun setItemListener(itemListener: TaskItemListener?) {
        mItemListener = itemListener
    }

    fun replaceData(tasks: List<TaskViewData>) {
        mTasks = tasks
        notifyDataSetChanged()
    }

    override fun getCount() = mTasks?.let { it.size } ?: 0

    override fun getItem(i: Int) = mTasks!![i]

    override fun getItemId(i: Int) = i.toLong()

    override fun getView(i: Int, view: View?, viewGroup: ViewGroup): View {
        return view
                ?: LayoutInflater.from(viewGroup.context).inflate(R.layout.task_item, viewGroup, false)
                .apply {
                    val (title, completed, backgroundDrawableId, id) = getItem(i)

                    findViewById<TextView>(R.id.title).text = title

                    findViewById<CheckBox>(R.id.complete).apply {
                        isChecked = completed
                        setOnClickListener {
                            mItemListener?.run {
                                if (isChecked) {
                                    onCompleteTaskClick(id)
                                } else {
                                    onActivateTaskClick(id)
                                }
                            }
                        }
                    }

                    setBackgroundDrawable(viewGroup.context.resources.getDrawable(backgroundDrawableId))

                    setOnClickListener {
                        mItemListener?.run { onTaskClick(id) }
                    }
                }
    }

    interface TaskItemListener {

        fun onTaskClick(id: String)

        fun onCompleteTaskClick(id: String)

        fun onActivateTaskClick(id: String)
    }
}
