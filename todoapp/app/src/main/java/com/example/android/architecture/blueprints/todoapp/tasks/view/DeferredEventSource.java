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
package com.example.android.architecture.blueprints.todoapp.tasks.view;

import com.spotify.mobius.EventSource;
import com.spotify.mobius.disposables.Disposable;
import com.spotify.mobius.functions.Consumer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Nonnull;

public class DeferredEventSource<E> implements EventSource<E> {
  private BlockingQueue<E> events = new LinkedBlockingQueue<>();

  @Nonnull
  @Override
  public Disposable subscribe(Consumer<E> eventConsumer) {
    AtomicBoolean run = new AtomicBoolean(true);
    Thread t =
        new Thread(
            () -> {
              while (run.get()) {
                try {
                  E e = events.take();
                  if (run.get()) {
                    eventConsumer.accept(e);
                  }
                } catch (InterruptedException e) {
                }
              }
            });
    t.start();
    return () -> {
      run.set(false);
      t.interrupt();
    };
  }

  public synchronized void notifyEvent(E e) {
    events.offer(e);
  }
}
