# Bonita Engine

[![Build](https://github.com/bonitasoft/bonita-engine/workflows/Build/badge.svg)](https://github.com/bonitasoft/bonita-engine/actions)

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.bonitasoft.engine/bonita-server/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.bonitasoft.engine/bonita-server)

Deploy, execute, manage applications made with Bonita Studio.


## Using the Engine

The engine is included as part of either [Bonita Studio][downloads] or [Bonita Runtime][downloads], and executes the BPMN process logic.
The engine can however be included as a standalone dependency in a custom Application, as explained [here][standalone]

## Running the Project

### Prerequisites
>     Java JDK 11 (to compile), and 11 (to run)

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

### Version
Version is declared in gradle.properties

To override the current version on build, use the parameter **-Pversion** like:

```
 ./gradlew -Pversion=7.9.3 <tasks>
```

### Extra repositories

repositories can be added using comma separated list of repositories
using property `extraRepositories` in format `repo_name::repo_url`

credentials can be passed using properties `repo_nameUsername` and
`repo_namePassword`

it can be configured using `-PextraRepositories=` or gradle.properties
file.

example of gradle properties set in `~/.gradle/gradle.properties`

```properties
extraRepositories=releases::https://repo1/releases,snapshots::https://repo2/snapshots/
releasesUsername=username
releasesPassword=password
snapshotsUsername=username
snapshotsPassword=password
```

The same can be done for publishing repository (single repo) using property `altDeploymentRepository`


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
[standalone]: https://documentation.bonitasoft.com/bonita/latest/runtime/embed-engine
[guide]: https://github.com/bonitasoft/bonita-developer-resources/blob/master/CONTRIBUTING.MD
[wrapper]: https://docs.gradle.org/current/userguide/gradle_wrapper.html
[bugtracker]: https://bonita.atlassian.net/projects/BBPMC/issues
