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
package com.example.android.architecture.blueprints.todoapp.tasks.view;

import static com.google.common.base.Preconditions.checkNotNull;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.tasks.view.TasksListViewData.TaskViewData;
import com.google.common.collect.ImmutableList;

class TasksAdapter extends BaseAdapter {

  private ImmutableList<TaskViewData> mTasks;
  private TaskItemListener mItemListener;

  public void setItemListener(TaskItemListener itemListener) {
    mItemListener = itemListener;
  }

  public void replaceData(ImmutableList<TaskViewData> tasks) {
    mTasks = checkNotNull(tasks);
    notifyDataSetChanged();
  }

  @Override
  public int getCount() {
    return mTasks == null ? 0 : mTasks.size();
  }

  @Override
  public TaskViewData getItem(int i) {
    return mTasks.get(i);
  }

  @Override
  public long getItemId(int i) {
    return i;
  }

  @Override
  public View getView(int i, View view, ViewGroup viewGroup) {
    View rowView = view;
    if (rowView == null) {
      LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
      rowView = inflater.inflate(R.layout.task_item, viewGroup, false);
    }

    final TaskViewData task = getItem(i);

    TextView titleTV = rowView.findViewById(R.id.title);
    titleTV.setText(task.title());

    CheckBox completeCB = rowView.findViewById(R.id.complete);
    completeCB.setChecked(task.completed());

    Drawable background =
        viewGroup.getContext().getResources().getDrawable(task.backgroundDrawableId());
    rowView.setBackgroundDrawable(background);

    completeCB.setOnClickListener(
        __ -> {
          if (mItemListener == null) return;

          if (!task.completed()) {
            mItemListener.onCompleteTaskClick(task.id());
          } else {
            mItemListener.onActivateTaskClick(task.id());
          }
        });

    rowView.setOnClickListener(
        __ -> {
          if (mItemListener != null) mItemListener.onTaskClick(task.id());
        });

    return rowView;
  }

  public interface TaskItemListener {

    void onTaskClick(String id);

    void onCompleteTaskClick(String id);

    void onActivateTaskClick(String id);
  }
}
