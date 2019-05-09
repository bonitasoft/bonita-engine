bonita-engine [![Build Status](https://travis-ci.org/bonitasoft/bonita-engine.svg?branch=master)](https://travis-ci.org/bonitasoft/bonita-engine)
=============

What it does?
-------------
This project builds Bonita Execution Engine (Community Edition)


Requirements
-------------
>     Java JDK 1.8 (to compile), and JVM 8 or 11 (to run)

This project bundles the [Gradle Wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html), so the `gradlew` script is available at
the project root.


Building the Engine
-----------------
Just run the following Gradle command:
```
./gradlew build
```

To be able to successfully build the Bonita components that use the Engine, run:
```
 ./gradlew publishToMavenLocal
```
Among other things, it also generates the javadoc used by Bonita Studio.

The command above runs all unit tests. To skip them, add the `-x test`
option.

Running unit / integration tests
-----------------
To run all **unit + integration tests** (on the default embedded H2
database), run the following command:
```
./gradlew test integrationTest
```

To run all **unit + integration tests** on mysql, oracle, sqlserver or postgres using
a _Docker_ container, run one of the following commands:
```
./gradlew mysqlDatabaseTest
./gradlew oracleDatabaseTest
./gradlew sqlserverDatabaseTest
./gradlew postgresDatabaseTest
```

How to contribute
-----------------
In order to contribute to the project, read the [guide](https://github.com/bonitasoft/bonita-developer-resources/blob/master/CONTRIBUTING.MD).
