# Bonita Engine

[![Build](https://github.com/bonitasoft/bonita-engine/actions/workflows/build.yml/badge.svg)](https://github.com/bonitasoft/bonita-engine/actions/workflows/build.yml)

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.bonitasoft.engine/bonita-server/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.bonitasoft.engine/bonita-server)

Deploy, execute, manage applications made with Bonita Studio.


## Using the Engine

The engine is included as part of either [Bonita Studio][downloads] or [Bonita Runtime][downloads], and executes the BPMN process logic.
The engine can however be included as a standalone dependency in a custom Application, as explained [here][standalone]

## Running the Project

### Prerequisites
>     Java JDK 17 (to compile), and 17 (to run)

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
```bash
./gradlew test integrationTest
```

### Test Retry Configuration

The project uses the [Gradle Test Retry Plugin](https://plugins.gradle.org/plugin/org.gradle.test-retry) to automatically retry failed tests and detect flaky tests.

#### Default Retry Behavior

By default:
- Failed tests are retried up to **2 more times**
- Build fails if more than **6 tests** fail in one round
- Flaky tests (pass on retry) fail the build locally but not in CI
- Only applies to '*IT' test classes (tasks `integrationTest` and database's: `postgresDatabaseTest`, etc.)

#### Overriding Retry Settings

```bash
# Retry up to 5 times instead of 2
./gradlew integrationTest -PtestRetryMaxRetries=5

# Retry and allow up to 20 failures before stopping
./gradlew integrationTest -PtestRetryMaxRetries=5 -PtestRetryMaxFailures=20

# Disable retry (set to 0)
./gradlew integrationTest -PtestRetryMaxRetries=0

# Don't fail on flaky tests (tests that pass on retry). Example for database tests:
./gradlew postgresDatabaseTest -PtestRetryFailOnFlaky=false
```

#### Important Note

The test-retry plugin is designed to **detect** flaky tests, not to mask them. Always investigate and fix the root cause of flaky tests rather than just relying on retries.


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




[downloads]: https://www.ofelia.com/downloads
[standalone]: https://documentation.ofelia.com/bonita/latest/runtime/embed-engine
[guide]: https://github.com/bonitasoft/bonita-developer-resources/blob/master/CONTRIBUTING.MD
[wrapper]: https://docs.gradle.org/current/userguide/gradle_wrapper.html
[bugtracker]: https://bonita.atlassian.net/projects/BBPMC/issues
