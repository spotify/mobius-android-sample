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

data class TaskDetails(@get:JvmName("title") val title: String = "",
                       @get:JvmName("description") val description: String = "",
                       @get:JvmName("completed") val completed: Boolean = false) {

    class Builder {
        private var title: String = ""
        private var description: String = ""
        private var completed: Boolean = false
        fun title(title: String) = apply { this.title = title }
        fun description(description: String) = apply { this.description = description }
        fun completed(completed: Boolean) = apply { this.completed = completed }

        fun build() = TaskDetails(title, description, completed)
    }

    companion object {
        /**
         * Creates a static final instance called DEFAULT on the TaskDetails class
         **/
        @JvmField
        val DEFAULT = TaskDetails()

        /**
         * Creates a static method builder in the TaskDetails class to be used in Java
         **/
        @JvmStatic
        fun builder() = Builder()

        /**
         * Generates two overloaded static create methods to be used in Java one that accepts two
         * strings and a boolean, and another that accepts only two strings and uses the default
         * value specified in the function signature for the boolean argument
         **/
        @JvmStatic
        @JvmOverloads
        fun create(title: String,
                   description: String,
                   completed: Boolean = false) = TaskDetails(title, description, completed)
    }
}

