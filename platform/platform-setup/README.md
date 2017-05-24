Bonita platform-setup
=============

What it does?
-------------
Project **Bonita Platform Setup** sets up Bonita Platform before **Bonita** can be run:

* it creates the structure of the database (tables, primary / foreign keys, indexes, ...)
* it inserts the default minimum data 
* it inserts the Bonita Engine + Bonita Portal minimum configuration in database

Requirements
-------------
>     Java JDK 1.8 or higher

Building platform-setup
-----------------
Run the following Maven command:
>     mvn clean install

It generates a **ZIP file** ready to run.

If you want to skip the tests, run:
>     mvn clean install -DskipTests

Running bonita-platform-setup
---
To run the tool:

* extract the Zip file
* configure access to the database in file `database.properties` at the root directory.
* add drivers (specific to your database) in /lib folder (for Oracle and SqlServer only, as open-source drivers are already included) 
* run ./setup.sh [init | push | pull | configure]
