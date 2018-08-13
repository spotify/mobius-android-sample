package com.example.android.architecture.blueprints.todoapp.tasks.domain

import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksFilterType.*

fun List<Task>.filter(filter: TasksFilterType) =
        when(filter) {
            ALL_TASKS -> this
            ACTIVE_TASKS -> filter { !it.details.completed }
            COMPLETED_TASKS -> filter { it.details.completed }
        }