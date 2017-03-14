bonita-engine
=============

What it does?
-------------
This project builds bonita BPM Execution Engine (Community Edition)

Requirements
-------------
>     Java JDK 1.8 or higher
>     Maven 3.x or higher

Building the Engine
-----------------
just run the following Maven command:
```
mvn clean install
```

To be able to successfully build the Bonita BPM components that use the Engine, run:
```
 mvn clean install -Ppackage,javadoc
```

The command above runs all unit tests. To skip them, add "-DskipTests" option.

To run all **unit + integration tests**, run the following command:
```
mvn clean verify -Ptests
```

To run all **unit + integration tests**  on H2 run the following command:
```
mvn clean verify -Ptests
```

To run all **unit + integration tests**  on mysql, postgres or oracle using a _Docker_ container run the following command:
```
mvn clean verify -Ptests,mysql
mvn clean verify -Ptests,oracle
mvn clean verify -Ptests,postgres
```
