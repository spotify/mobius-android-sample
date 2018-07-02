package com.example.android.architecture.blueprints.todoapp.addedittask.domain

import com.example.android.architecture.blueprints.todoapp.addedittask.domain.AddEditTaskModel.Mode.Add
import com.example.android.architecture.blueprints.todoapp.addedittask.domain.AddEditTaskModel.Mode.Edit
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.TaskDetails
import com.spotify.mobius.test.NextMatchers.*
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test


class LogicTest {
    private var updateSpec: UpdateSpec<Model, Event, Effect> = UpdateSpec(::update)

    @Test
    fun completingTaskDefinitionWhenCreatingTaskWithEmptyTitleAndDescriptionDoesNotUpdateState() {
        val creatingTask = Model(mode = Add, details = TaskDetails())

        updateSpec
                .given(creatingTask)
                .whenEvent(TaskDefinitionCompleted(title = "", description = "     "))
                .then(assertThatNext(hasNoModel(), hasEffects(notifyEmptyTaskNotAllowed())))
    }

    @Test
    fun completingTaskDefinitionWithEmptyTitleAndDescriptionDoesNotUpdateState() {
        val updatingATask = Model(mode = Edit("123"),
                details = TaskDetails(title = "T1", description = "This is a task"))

        updateSpec
                .given(updatingATask)
                .whenEvent(TaskDefinitionCompleted(title = "", description = "     "))
                .then(assertThatNext(hasNoModel(), hasEffects(notifyEmptyTaskNotAllowed())))
    }

    @Test
    fun tasksCanBeUpdatedWithEmptyTitle() {
        val details = TaskDetails("T1", "This is a task")
        val updatingATask = Model(mode = Edit("123"),
                details = details)

        val expectedDetails = details.copy(title = "", description = "Hello World")

        updateSpec
                .given(updatingATask)
                .whenEvent(TaskDefinitionCompleted(title = "    ", description = "Hello World"))
                .then(assertThatNext(
                        hasModel(updatingATask.copy(details = expectedDetails)),
                        hasEffects(saveTask(Task("123", expectedDetails))))
                )
    }

    @Test
    fun tasksCanBeUpdatedWithEmptyDescription() {
        val details = TaskDetails("T1", "This is a task")
        val updatingATask = Model(mode = Edit("123"), details = details)

        val updatedDetails = details.copy(title = "Hello Tasks!", description = "")

        updateSpec
                .given(updatingATask)
                .whenEvent(TaskDefinitionCompleted(title = "Hello Tasks!", description = ""))
                .then(assertThatNext(
                        hasModel(updatingATask.copy(details = updatedDetails)),
                        hasEffects(saveTask(Task("123", updatedDetails)))))
    }

    @Test
    fun tasksCanBeCreatedWithEmptyTitle() {
        val creatingATask = Model(mode = Add, details = TaskDetails.DEFAULT)

        val expectedDetails = TaskDetails(title = "", description = "Hello World")
        updateSpec
                .given(creatingATask)
                .whenEvent(TaskDefinitionCompleted(title = "    ", description = "Hello World"))
                .then(assertThatNext(
                        hasModel(creatingATask.copy(details = expectedDetails)),
                        hasEffects(createTask(expectedDetails))))
    }

    @Test
    fun tasksCanBeCreatedWithEmptyDescription() {
        val creatingATask = Model(mode = Add, details = TaskDetails.DEFAULT)

        val expectedDetails = TaskDetails("Hello Tasks!", "")
        updateSpec
                .given(creatingATask)
                .whenEvent(TaskDefinitionCompleted(title = "Hello Tasks!", description = ""))
                .then(assertThatNext(
                        hasModel(creatingATask.copy(details = expectedDetails)),
                        hasEffects(createTask(expectedDetails))))
    }

    @Test
    fun taskCreationSuccessShouldExit() {
        val creatingATask = Model(mode = Add,
                details = TaskDetails("hello", "world"))

        updateSpec
                .given(creatingATask)
                .whenEvent(TaskCreatedSuccessfully)
                .then(assertThatNext(hasNoModel(), hasEffects(exit(true))))
    }

    @Test
    fun taskUpdateSuccessShouldExit() {
        val updatingATask = Model(mode = Edit("1234"),
                details = TaskDetails("hello", "world"))

        updateSpec
                .given(updatingATask)
                .whenEvent(TaskUpdatedSuccessfully)
                .then(assertThatNext(hasNoModel(), hasEffects(exit(true))))
    }

    @Test
    fun taskCreationFailureShouldExit() {
        val creatingATask = Model(mode = Add,
                details = TaskDetails("hello", "world"))

        updateSpec
                .given(creatingATask)
                .whenEvent(TaskCreationFailed("Database Unavailable"))
                .then(assertThatNext(hasNoModel(), hasEffects(exit(false))))
    }

    @Test
    fun taskUpdateFailureShouldExit() {
        val updatingATask = Model(mode = Edit("1234"),
                details = TaskDetails("hello", "world"))

        updateSpec
                .given(updatingATask)
                .whenEvent(TaskUpdateFailed("Database Unavailable"))
                .then(assertThatNext(hasNoModel(), hasEffects(exit(false))))
    }

    /** Convenience factory functions **/
    fun notifyEmptyTaskNotAllowed() = NotifyEmptyTaskNotAllowed as AddEditTaskEffect
    fun createTask(taskDetails: TaskDetails) = CreateTask(taskDetails) as AddEditTaskEffect
    fun saveTask(task: Task) = SaveTask(task) as AddEditTaskEffect
    fun exit(successful: Boolean) = Exit(successful) as AddEditTaskEffect
}