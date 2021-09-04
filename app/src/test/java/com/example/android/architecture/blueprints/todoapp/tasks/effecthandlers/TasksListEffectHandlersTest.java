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
package com.example.android.architecture.blueprints.todoapp.tasks.effecthandlers;

import static com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksListEffect.deleteTasks;
import static com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksListEffect.loadTasks;
import static com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksListEffect.navigateToTaskDetails;
import static com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksListEffect.refreshTasks;
import static com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksListEffect.saveTask;
import static com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksListEffect.showFeedback;
import static com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksListEvent.tasksLoaded;
import static com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksListEvent.tasksLoadingFailed;
import static com.example.android.architecture.blueprints.todoapp.tasks.effecthandlers.TasksListEffectHandlers.deleteTasksHandler;
import static com.example.android.architecture.blueprints.todoapp.tasks.effecthandlers.TasksListEffectHandlers.loadTasksHandler;
import static com.example.android.architecture.blueprints.todoapp.tasks.effecthandlers.TasksListEffectHandlers.navigateToDetailsHandler;
import static com.example.android.architecture.blueprints.todoapp.tasks.effecthandlers.TasksListEffectHandlers.refreshTasksHandler;
import static com.example.android.architecture.blueprints.todoapp.tasks.effecthandlers.TasksListEffectHandlers.saveTaskHandler;
import static com.example.android.architecture.blueprints.todoapp.tasks.effecthandlers.TasksListEffectHandlers.showFeedbackHandler;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import androidx.annotation.NonNull;
import com.example.android.architecture.blueprints.todoapp.data.FakeTasksRemoteDataSource;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.TaskDetails;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.tasks.domain.FeedbackType;
import com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksListEffect;
import com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksListEffect.LoadTasks;
import com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksListEffect.RefreshTasks;
import com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksListEffect.SaveTask;
import com.example.android.architecture.blueprints.todoapp.tasks.domain.TasksListEvent;
import com.example.android.architecture.blueprints.todoapp.tasks.view.TasksListViewActions;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.spotify.mobius.test.RecordingConsumer;
import io.reactivex.Flowable;
import io.reactivex.ObservableTransformer;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.PublishSubject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TasksListEffectHandlersTest {

  private static final Task TASK_1 = Task.create("1234", TaskDetails.create("Title1", "", false));
  private static final Task TASK_2 = Task.create("4321", TaskDetails.create("Title2", "", false));
  private static final Task TASK_3 = Task.create("5321", TaskDetails.create("Title3", "", false));

  public static class RefreshingTasks {

    @Before
    public void setUp() throws Exception {
      FakeTasksRemoteDataSource.getInstance().addTasks(TASK_1);
    }

    @Test
    public void callsBackendThenSavesThingsToLocalDataSource() {
      FakeTasksRemoteDataSource remoteSource = FakeTasksRemoteDataSource.getInstance();
      FakeDataSource localSource = new FakeDataSource();

      ObservableTransformer<RefreshTasks, TasksListEvent> underTest =
          refreshTasksHandler(remoteSource, localSource);

      TestCase<RefreshTasks, TasksListEvent> testCase = new TestCase<>(underTest);
      testCase.dispatchEffect(refreshTasks().asRefreshTasks());

      assertThat(localSource.tasks, contains(TASK_1));
      testCase.assertEvents(TasksListEvent.tasksRefreshed());
    }

    @Test
    public void failureToStoreTasksResultsInErrorEvent() {
      FakeDataSource localSource = new FakeDataSource();
      localSource.fail = true;
      ObservableTransformer<RefreshTasks, TasksListEvent> underTest =
          refreshTasksHandler(FakeTasksRemoteDataSource.getInstance(), localSource);

      TestCase<RefreshTasks, TasksListEvent> testCase = new TestCase<>(underTest);

      testCase.dispatchEffect(refreshTasks().asRefreshTasks());
      assertThat(localSource.tasks.isEmpty(), is(true));
      testCase.assertEvents(tasksLoadingFailed());
    }

    @Test
    public void failureToLoadBackendResultsInErrorEvent() {
      FakeDataSource localSource = new FakeDataSource();
      FakeDataSource remoteSource = new FakeDataSource();
      remoteSource.fail = true;
      ObservableTransformer<RefreshTasks, TasksListEvent> underTest =
          refreshTasksHandler(remoteSource, localSource);
      TestCase<RefreshTasks, TasksListEvent> testCase = new TestCase<>(underTest);
      testCase.dispatchEffect(refreshTasks().asRefreshTasks());
      assertThat(localSource.tasks.isEmpty(), is(true));
      testCase.assertEvents(tasksLoadingFailed());
    }

    @After
    public void tearDown() throws Exception {
      FakeTasksRemoteDataSource.getInstance().deleteAllTasks();
    }
  }

  public static class LoadingTasks {

    @Test
    public void loadingTasksEmitsTaskLoadedEvent() {
      FakeDataSource dataSource = new FakeDataSource();
      dataSource.addTasks(TASK_1);

      ImmutableList<Task> result = ImmutableList.of(TASK_1);
      TestCase<LoadTasks, TasksListEvent> testCase = new TestCase<>(loadTasksHandler(dataSource));
      testCase.dispatchEffect(loadTasks().asLoadTasks());
      testCase.assertEvents(tasksLoaded(result));
    }

    @Test
    public void failingToLoadTasksShouldResultInErrorEvent() {
      FakeDataSource dataSource = new FakeDataSource();
      dataSource.fail = true;

      TestCase<LoadTasks, TasksListEvent> testCase = new TestCase<>(loadTasksHandler(dataSource));
      testCase.dispatchEffect(loadTasks().asLoadTasks());
      testCase.assertEvents(tasksLoadingFailed());
    }

    @Test
    public void failingToLoadTasksTheFirstTimeShouldResultInErrorEventAndAcceptSubsequentEffects() {
      FakeDataSource dataSource = new FakeDataSource();
      dataSource.addTasks(TASK_1);
      dataSource.fail = true;

      TestCase<LoadTasks, TasksListEvent> testCase = new TestCase<>(loadTasksHandler(dataSource));
      testCase.dispatchEffect(loadTasks().asLoadTasks());

      dataSource.fail = false;
      testCase.dispatchEffect(loadTasks().asLoadTasks());
      testCase.assertEvents(tasksLoadingFailed(), tasksLoaded(ImmutableList.of(TASK_1)));
    }
  }

  public static class SavingAndDeletingTasks {

    @Test
    public void savingTasksAddsThemToBothLocalAndRemoteDataSources() throws Exception {
      FakeDataSource remote = new FakeDataSource();
      FakeDataSource local = new FakeDataSource();
      Consumer<SaveTask> saveTaskConsumer = saveTaskHandler(remote, local);
      saveTaskConsumer.accept(saveTask(TASK_1).asSaveTask());
      assertThat(remote.tasks, contains(TASK_1));
      assertThat(local.tasks, contains(TASK_1));
    }

    @Test
    public void deletingTasksRemovesThemFromBothLocalAndRemoteDataSources() throws Exception {
      FakeDataSource remote = new FakeDataSource();
      FakeDataSource local = new FakeDataSource();
      remote.addTasks(TASK_1, TASK_2, TASK_3);
      local.addTasks(TASK_1, TASK_2, TASK_3);
      Consumer<TasksListEffect.DeleteTasks> saveTaskConsumer = deleteTasksHandler(remote, local);
      saveTaskConsumer.accept(deleteTasks(ImmutableList.of(TASK_1, TASK_3)).asDeleteTasks());

      assertThat(remote.tasks, allOf(not(hasItems(TASK_1)), not(hasItems(TASK_3))));
      assertThat(local.tasks, allOf(not(hasItems(TASK_1)), not(hasItems(TASK_3))));

      assertThat(remote.tasks, hasItems(TASK_2));
      assertThat(local.tasks, hasItems(TASK_2));
    }
  }

  public static class UIEffects {
    @Test
    public void showFeedbackHandlerInvokesAppropriateViewMethods() throws Exception {
      TasksListViewActions view = mock(TasksListViewActions.class);
      Consumer<TasksListEffect.ShowFeedback> underTest = showFeedbackHandler(view);

      underTest.accept(showFeedback(FeedbackType.LOADING_ERROR).asShowFeedback());
      verify(view).showLoadingTasksError();

      reset(view);
      underTest.accept(showFeedback(FeedbackType.CLEARED_COMPLETED).asShowFeedback());
      verify(view).showCompletedTasksCleared();

      reset(view);
      underTest.accept(showFeedback(FeedbackType.MARKED_ACTIVE).asShowFeedback());
      verify(view).showTaskMarkedActive();

      reset(view);
      underTest.accept(showFeedback(FeedbackType.MARKED_COMPLETE).asShowFeedback());
      verify(view).showTaskMarkedComplete();

      reset(view);
      underTest.accept(showFeedback(FeedbackType.SAVED_SUCCESSFULLY).asShowFeedback());
      verify(view).showSuccessfullySavedMessage();
    }

    @Test
    public void navigationShowsTaskDetailsUi() throws Exception {
      RecordingConsumer<Task> consumer = new RecordingConsumer<>();
      navigateToDetailsHandler(consumer::accept)
          .accept(navigateToTaskDetails(TASK_1).asNavigateToTaskDetails());
      consumer.assertValues(TASK_1);
    }
  }

  static class TestCase<F, E> {
    final PublishSubject<F> upstream = PublishSubject.create();
    final TestObserver<E> observer = new TestObserver<>();

    TestCase(ObservableTransformer<F, E> underTest) {
      upstream.compose(underTest).subscribe(observer);
    }

    public void dispatchEffect(F effect) {
      upstream.onNext(effect);
    }

    @SafeVarargs
    public final void assertEvents(E... events) {
      observer.assertValues(events);
    }
  }

  static class FakeDataSource implements TasksDataSource {

    public List<Task> tasks = new ArrayList<>();
    public boolean fail = false;

    @Override
    public Flowable<List<Task>> getTasks() {
      if (fail) return Flowable.error(new RuntimeException("Could not load tasks"));
      return Flowable.just(tasks);
    }

    @Override
    public Flowable<Optional<Task>> getTask(@NonNull String taskId) {
      if (fail) return Flowable.error(new RuntimeException("Could not load task"));
      return Flowable.fromCallable(
          () -> {
            for (Task task : tasks) {
              if (task.id().equals(taskId)) return Optional.of(task);
            }
            return Optional.absent();
          });
    }

    @Override
    public void saveTask(@NonNull Task task) {
      if (fail) throw new RuntimeException("Failed to operate");
      if (tasks.indexOf(task) > -1) {
        tasks.set(tasks.indexOf(task), task);
      } else {
        tasks.add(task);
      }
    }

    @Override
    public void deleteAllTasks() {
      if (fail) throw new RuntimeException("Failed to operate");
      tasks = new ArrayList<>();
    }

    @Override
    public void deleteTask(@NonNull String taskId) {
      if (fail) throw new RuntimeException("Failed to operate");
      Optional<Task> taskOptional = getTask(taskId).blockingFirst();
      if (!taskOptional.isPresent()) throw new IllegalArgumentException("Task does not exist");

      tasks.remove(taskOptional.get());
    }

    public void addTasks(Task... testTasks) {
      tasks.addAll(Arrays.asList(testTasks));
    }
  }
}
