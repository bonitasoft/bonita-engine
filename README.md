# Bonita Engine

[![Travis-ci Status](https://travis-ci.org/bonitasoft/bonita-engine.svg)](https://travis-ci.org/bonitasoft/bonita-engine)

[![Github Actions status](https://github.com/bonitasoft/bonita-engine/workflows/Build%20Engine%20Community/badge.svg)](https://github.com/bonitasoft/bonita-engine/actions)
Deploy, execute, manage applications made with Bonita Studio.


## Using the Engine

The engine is included as part of either [Bonita Studio][downloads] or [Bonita Web Platform][downloads], and executes the BPMN process logic.
The engine can however be included as a standalone dependency in a custom Application, as explained [here][standalone]

## Running the Project

### Prerequisites
>     Java JDK 1.8 (to compile), and JVM 8 or 11 (to run)

This project bundles the [Gradle Wrapper][wrapper], so the `gradlew` script is available at
the project root.

### Compiling

Just run the following Gradle command:
```
./gradlew build
```

To be able to successfully build other Bonita components that use the Engine, run:
```
 ./gradlew publishToMavenLocal
```
Among other things, it also generates the javadoc used by Bonita Studio.

The command above runs all unit tests. To skip them, add the `-x test`
option.

### Running unit / integration tests

To run all **unit + integration tests** (on the default embedded H2
database), run the following command:
```
./gradlew test integrationTest
```

## Project Structure
The project is composed of several modules. Unit tests are contained in the modules, integration tests are regrouped in bonita-integration-tests.

* `bonita-engine-spring-boot-starter` : Run the engine in standalone mode using Spring boot, see [documentation][standalone]
* `bonita-engine-standalone` : Run the engine in standalone programmatically, see [documentation][standalone]
* `bonita-test-api` : Junit Rule to include the engine in your tests
* `bpm` : Services related to bpm process execution
* `buildSrc` : Internal Gradle plugins used to build Bonita Engine
* `platform` : Services that handle the platform creation/configuration
* `services` : Generic services used by the engine
 
## How to contribute
In order to contribute to the project, read the [guide][guide].
To report an issue use the official [bugtracker][bugtracker].




[downloads]: https://www.bonitasoft.com/downloads
[standalone]: https://documentation.bonitasoft.com/bonita/7.9/embed-engine
[guide]: https://github.com/bonitasoft/bonita-developer-resources/blob/master/CONTRIBUTING.MD
[wrapper]: https://docs.gradle.org/current/userguide/gradle_wrapper.html
[bugtracker]: https://bonita.atlassian.net/projects/BBPMC/issues
