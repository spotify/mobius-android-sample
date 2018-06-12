# TODO-MOBIUS

### Summary

This sample is based on the [TODO-MVP-RXJAVA](https://github.com/googlesamples/android-architecture/tree/todo-mvp-rxjava) project from the Android Architecture Blueprints repository. It converts the code from MVP using RxJava to a [Mobius](https://github.com/spotify/mobius) implementation.

Compared to the TODO-MVP-RXJAVA, this project implements all business logic and presentation logic as pure functions, and utilizes Mobius to manage state and side-effects. Presenters are called ViewDataMappers and they are pure functions that accept `Model` instances and return `ViewData` objects that are then bound to the UI. Furthermore, views were extracted into separate classes used by fragments. Data Source implementations were reused.

The ``TasksRepository`` class has been removed as its responsibility is achieved using a Mobius update function.

The project contains four features. Each feature's implementation is a little unique to show the different approaches to modeling in Mobius. Here are the highlights:
* Tasks List: This uses a Data class as the `Model`. It also has a `ViewData` class that encapsulates what the view should render. A pure function that maps `Model` instances to `ViewData` ones is applied for every `Model` update and the resulting `ViewData` object is passed to the View for rendering.
* Add/Edit Task: Also uses a Data class as the `Model` but binds that model directly to the View without the need for `ViewData`
* Task Details: Same as Tasks List
* Statistics: This uses a Sum Type as the `Model` to represent the different states.

Each feature is divided into the following package structure:
- featurename.domain: Contains all things describing the domain of the feature. The definition of the ``Model``, Logic (``Init``/``Update`` functions), ``Event``s and ``Effect``s that describe the feature.
- featurename.effecthandlers: Contains the definition of all effect handlers that execute effects the logic functions dispatch.
- featurename.view: Contains all things that have to do with presentation logic (i.e. ViewDataMappers), the definition of ViewData types, and the views implementation.
- featurename: Contains the feature's Fragment/Activity that is responsible for creating a ``MobiusLoop.Controller``, connecting it to the View and managing it.

Features in this project treat `Fragment`s as builders that are only responsible for managing lifecycle and creating dependencies. Each `Fragment` creates a `MobiusLoop.Controller` and connects it to the view, which is now a separate `Views` class, owned by the `Fragment`. Once a `MobiusLoop.Controller` is started, it'll start the `MobiusLoop` which kicks things off by invoking the `Init` function. The `Init` and `Update` functions define how state should evolve and what effects should happen. The UI is purely derived from the representation of state, i.e. the `Model`. There are helper methods that turn `Model`s into `Bundle`s and vice versa. These are used for state restore.

Mobius uses a computation thread to process events, i.e. invoke `Init`/`Update` functions. This thread is synchronized and events are processed one at a time. It also utilizes an `ExecutorService` to process effects returned by the logic functions. `MobiusLoop.Controller` makes sure `Model` updates are delivered on the main thread for rendering.

There's a helper builder accessible through `RxMobius.subtypeEffectHandler()` that allows you to specify an effect handler per effect type. Here's the definition of the effect handlers for the AddEditTask feature
```java
ObservableTransformer<AddEditTaskEffect, AddEditTaskEvent> effectHandler =
     RxMobius.<AddEditTaskEffect, AddEditTaskEvent>subtypeEffectHandler()
       .addAction(NotifyEmptyTaskNotAllowed.class, showEmptyTaskError, mainThread()) // where showEmptyTaskError is an Action implementation
       .addAction(Exit.class, showTasksList, mainThread()) // where showTasksList is an Action implementation
       .addFunction(CreateTask.class, createTaskHandler(remoteSource, localSource)) // where createTaskHandler returns an Function<CreateTask, AddEditTaskEvent>
       .addFunction(SaveTask.class, saveTaskHandler(remoteSource, localSource)) //where saveTaskHandler returns an Function<SaveTask, AddEditTaskEvent>
       .build();
```
For more information about Effect Handlers, please refer to the [Mobius Wiki](https://github.com/spotify/mobius/wiki/Mobius-and-RxJava#rxmobiussubtypeeffecthandler)

### Dependencies

* [Mobius 1.2.0](https://github.com/spotify/mobius)
* [DataEnum 1.3.1](https://github.com/spotify/DataEnum)
* [AutoValue 1.6.0](https://github.com/google/auto/tree/master/value)
* [HamcrestPojo 1.1.1](https://github.com/spotify/java-hamcrest)
* [RxJava 2.x](https://github.com/ReactiveX/RxJava)
* [RxAndroid 2.x](https://github.com/ReactiveX/RxAndroid)
* [SqlBrite 2.x](https://github.com/square/sqlbrite)

## Features

### Complexity - understandability

#### Use of architectural frameworks/libraries/tools:

Mobius simplifies building of business logic by turning it into a synchronous pure function.

#### Conceptual complexity

A couple of new concepts that need to be learned such as Pure functions and Sum Types.

### Testability

#### Unit testing

Very High. For the following reasons:
* Logic is written as pure functions which are the easiest form of functions to test. Consequently tests can be written in a BDD behavior specification style. Furthermore, TDD becomes a lot simpler since there's no need for mocks/fakes/stubs.
* Each effect handler has a single responsibility and the least amount of logic needed to perform the requested effect. This makes them a lot simpler and consequently easier to reason about and test.

#### UI testing
UI testing can be split into two categories of tests:
* Presenatation: Since presentation logic is also defined as a Pure function, testing it becomes very simple.
* View: Same as TODO-MVP-RXJAVA. This implementation does not change anything with regards to integration/end to end testing.

### Maintainability

#### Ease of amending or adding a feature

High.

#### Learning cost

* Low as Pure functions are easy to write/test
* Medium as RxJava is not trivial. There are, however, several utilities in the framework that help build RxJava chains for users. It's worth mentioning that Mobius as a framework can also be used in apps that do not utilize RxJava.

## External contributors
Mobius Implementation:
* [Ahmed Nawara](https://github.com/anawara)
* [Petter Måhlén](https://github.com/pettermahlen)

Original Implementation:
* [Voicu Klein](https://github.com/kleinsenberg)
* [Erik Hellman](https://github.com/erikhellman)

This project adheres to the [Open Code of Conduct][code-of-conduct]. By participating, you are expected to honor this code.

[code-of-conduct]: https://github.com/spotify/code-of-conduct/blob/master/code-of-conduct.md
