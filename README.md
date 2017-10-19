bonita-engine
=============

What it does?
-------------
This project builds Bonita Execution Engine (Community Edition)


Requirements
-------------
>     Java JDK 1.8 or higher

This project bundles the [Maven Wrapper](https://github.com/takari/maven-wrapper), so the `mvnw` script is available at
the project root.


Building the Engine
-----------------
Just run the following Maven command:
```
mvnw install
```

To be able to successfully build the Bonita components that use the Engine, run:
```
 mvnw install -Ppackage,javadoc
```

The command above runs all unit tests. To skip them, add "-DskipTests" option.

To run all **unit + integration tests**, run the following command:
```
mvnw verify -Ptests
```

To run all **unit + integration tests** on H2 run the following command:
```
mvnw verify -Ptests
```

To run all **unit + integration tests** on mysql, postgres or oracle using a _Docker_ container run the following command:
```
mvnw verify -Ptests,mysql
mvnw verify -Ptests,oracle
mvnw verify -Ptests,postgres
```
