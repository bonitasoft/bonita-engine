bonita-engine
=============

What it does?
-------------
This project builds bonita BPM Execution Engine (Community Edition)

Requirements
-------------
>     Java JDK 1.7 or higher
>     Maven 3.x or higher

Building the Engine
-----------------
just run the following Maven command:
>     mvn clean install

To be able to successfully build the Bonita BPM components that use the Engine, run:
>     mvn clean install -Ppackage,javadoc

The command above runs all unit tests. To skip them, add "-DskipTests" option.

To run all **unit + integration tests**, run the following command:
>     mvn clean install -Ptests


How to contribute
-----------------

In order to contribute read the [guide](https://github.com/bonitasoft/bonita-developer-resources/blob/master/CONTRIBUTING.MD).
