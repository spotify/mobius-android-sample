/*
 * -\-\-
 * --
 * Copyright (c) 2017-2018 Spotify AB
 * --
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
 * -/-/-
 */
package com.example.android.architecture.blueprints.todoapp.data

data class Task(@get:JvmName("id") val id: String,
                @get:JvmName("details") val details: TaskDetails) {
    fun complete() = copy(details = details.copy(completed = true))
    fun activate() = copy(details = details.copy(completed = false))

    companion object {
        @JvmStatic
        fun create(id: String, details: TaskDetails) = Task(id, details)
    }
}