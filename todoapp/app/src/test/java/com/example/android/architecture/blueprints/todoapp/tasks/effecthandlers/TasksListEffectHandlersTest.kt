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
package com.example.android.architecture.blueprints.todoapp.tasks.effecthandlers

import com.example.android.architecture.blueprints.todoapp.data.FakeTasksRemoteDataSource
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.TaskDetails
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource
import com.example.android.architecture.blueprints.todoapp.tasks.domain.*
import com.example.android.architecture.blueprints.todoapp.tasks.view.TasksListViewActions
import com.google.common.base.Optional
import com.google.common.collect.ImmutableList
import com.spotify.mobius.test.RecordingConsumer
import io.reactivex.Flowable
import io.reactivex.ObservableTransformer
import io.reactivex.observers.TestObserver
import io.reactivex.subjects.PublishSubject
import org.hamcrest.CoreMatchers.hasItems
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import java.util.*

object TasksListEffectHandlersTest {

    private val TASK_1 = Task("1234", TaskDetails("Title1", "", false))
    private val TASK_2 = Task("4321", TaskDetails("Title2", "", false))
    private val TASK_3 = Task("5321", TaskDetails("Title3", "", false))

    class RefreshingTasks {

        @Before
        @Throws(Exception::class)
        fun setUp() {
            FakeTasksRemoteDataSource.getInstance().addTasks(TASK_1)
        }

        @Test
        fun callsBackendThenSavesThingsToLocalDataSource() {
            val remoteSource = FakeTasksRemoteDataSource.getInstance()
            val localSource = FakeDataSource()

            val underTest = refreshTasksHandler(remoteSource, localSource)

            val testCase = TestCase(underTest)
            testCase.dispatchEffect(RefreshTasks)

            assertThat<List<Task>>(localSource.tasks, contains(TASK_1))
            testCase.assertEvents(TasksRefreshed)
        }

        @Test
        fun failureToStoreTasksResultsInErrorEvent() {
            val localSource = FakeDataSource()
            localSource.fail = true
            val underTest = refreshTasksHandler(FakeTasksRemoteDataSource.getInstance(), localSource)

            val testCase = TestCase(underTest)

            testCase.dispatchEffect(RefreshTasks)
            assertThat(localSource.tasks.isEmpty(), `is`(true))
            testCase.assertEvents(TasksLoadingFailed)
        }

        @Test
        fun failureToLoadBackendResultsInErrorEvent() {
            val localSource = FakeDataSource()
            val remoteSource = FakeDataSource()
            remoteSource.fail = true
            val underTest = refreshTasksHandler(remoteSource, localSource)
            val testCase = TestCase(underTest)
            testCase.dispatchEffect(RefreshTasks)
            assertThat(localSource.tasks.isEmpty(), `is`(true))
            testCase.assertEvents(TasksLoadingFailed)
        }

        @After
        @Throws(Exception::class)
        fun tearDown() {
            FakeTasksRemoteDataSource.getInstance().deleteAllTasks()
        }
    }

    class LoadingTasks {

        @Test
        fun loadingTasksEmitsTaskLoadedEvent() {
            val dataSource = FakeDataSource()
            dataSource.addTasks(TASK_1)

            val result = ImmutableList.of(TASK_1)
            val testCase = TestCase(loadTasksHandler(dataSource))
            testCase.dispatchEffect(LoadTasks)
            testCase.assertEvents(TasksLoaded(result))
        }

        @Test
        fun failingToLoadTasksShouldResultInErrorEvent() {
            val dataSource = FakeDataSource()
            dataSource.fail = true

            val testCase = TestCase(loadTasksHandler(dataSource))
            testCase.dispatchEffect(LoadTasks)
            testCase.assertEvents(TasksLoadingFailed)
        }

        @Test
        fun failingToLoadTasksTheFirstTimeShouldResultInErrorEventAndAcceptSubsequentEffects() {
            val dataSource = FakeDataSource()
            dataSource.addTasks(TASK_1)
            dataSource.fail = true

            val testCase = TestCase(loadTasksHandler(dataSource))
            testCase.dispatchEffect(LoadTasks)

            dataSource.fail = false
            testCase.dispatchEffect(LoadTasks)
            testCase.assertEvents(TasksLoadingFailed, TasksLoaded(listOf(TASK_1)))
        }
    }

    class SavingAndDeletingTasks {

        @Test
        @Throws(Exception::class)
        fun savingTasksAddsThemToBothLocalAndRemoteDataSources() {
            val remote = FakeDataSource()
            val local = FakeDataSource()
            val saveTaskConsumer = saveTaskHandler(remote, local)
            saveTaskConsumer(SaveTask(TASK_1))
            assertThat<List<Task>>(remote.tasks, contains(TASK_1))
            assertThat<List<Task>>(local.tasks, contains(TASK_1))
        }

        @Test
        @Throws(Exception::class)
        fun deletingTasksRemovesThemFromBothLocalAndRemoteDataSources() {
            val remote = FakeDataSource()
            val local = FakeDataSource()
            remote.addTasks(TASK_1, TASK_2, TASK_3)
            local.addTasks(TASK_1, TASK_2, TASK_3)
            val saveTaskConsumer = deleteTasksHandler(remote, local)
            saveTaskConsumer(DeleteTasks(listOf(TASK_1, TASK_3)))

            assertThat<List<Task>>(remote.tasks, allOf(not(hasItems(TASK_1)), not(hasItems(TASK_3))))
            assertThat<List<Task>>(local.tasks, allOf(not(hasItems(TASK_1)), not(hasItems(TASK_3))))

            assertThat<List<Task>>(remote.tasks, hasItems(TASK_2))
            assertThat<List<Task>>(local.tasks, hasItems(TASK_2))
        }
    }

    class UIEffects {
        @Test
        @Throws(Exception::class)
        fun showFeedbackHandlerInvokesAppropriateViewMethods() {
            val view = mock(TasksListViewActions::class.java)
            val underTest = showFeedbackHandler(view)

            underTest(ShowFeedback(FeedbackType.LOADING_ERROR))
            verify(view).showLoadingTasksError()

            reset(view)
            underTest(ShowFeedback(FeedbackType.CLEARED_COMPLETED))
            verify(view).showCompletedTasksCleared()

            reset(view)
            underTest(ShowFeedback(FeedbackType.MARKED_ACTIVE))
            verify(view).showTaskMarkedActive()

            reset(view)
            underTest(ShowFeedback(FeedbackType.MARKED_COMPLETE))
            verify(view).showTaskMarkedComplete()

            reset(view)
            underTest(ShowFeedback(FeedbackType.SAVED_SUCCESSFULLY))
            verify(view).showSuccessfullySavedMessage()
        }

        @Test
        @Throws(Exception::class)
        fun navigationShowsTaskDetailsUi() {
            val consumer = RecordingConsumer<Task>()
            val underTest = navigateToDetailsHandler { consumer.accept(it) }
            underTest(NavigateToTaskDetails(TASK_1))
            consumer.assertValues(TASK_1)
        }
    }

    internal class TestCase<F, E>(underTest: ObservableTransformer<F, E>) {
        val upstream = PublishSubject.create<F>()
        val observer = TestObserver<E>()

        init {
            upstream.compose(underTest).subscribe(observer)
        }

        fun dispatchEffect(effect: F) {
            upstream.onNext(effect)
        }

        @SafeVarargs
        fun assertEvents(vararg events: E) {
            observer.assertValues(*events)
        }
    }

    internal class FakeDataSource : TasksDataSource {

        var tasks: MutableList<Task> = ArrayList()
        var fail = false

        override fun getTasks(): Flowable<List<Task>> {
            return if (fail) Flowable.error(RuntimeException("Could not load tasks")) else Flowable.just(tasks)
        }

        override fun getTask(taskId: String): Flowable<Optional<Task>> {
            return if (fail) Flowable.error(RuntimeException("Could not load task"))
            else Flowable.fromCallable {
                tasks.firstOrNull { it.id == taskId }
                        .let { Optional.fromNullable(it) }
            }
        }

        override fun saveTask(task: Task) {
            if (fail) throw RuntimeException("Failed to operate")
            if (tasks.indexOf(task) > -1) {
                tasks[tasks.indexOf(task)] = task
            } else {
                tasks.add(task)
            }
        }

        override fun deleteAllTasks() {
            if (fail) throw RuntimeException("Failed to operate")
            tasks = ArrayList()
        }

        override fun deleteTask(taskId: String) {
            if (fail) throw RuntimeException("Failed to operate")
            val taskOptional = getTask(taskId).blockingFirst()
            if (!taskOptional.isPresent) throw IllegalArgumentException("Task does not exist")

            tasks.remove(taskOptional.get())
        }

        fun addTasks(vararg testTasks: Task) {
            tasks.addAll(Arrays.asList(*testTasks))
        }
    }
}
