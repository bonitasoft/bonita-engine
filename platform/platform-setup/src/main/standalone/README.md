Bonita Platform Setup tool
===

What it does?
---
**Bonita Platform Setup tool** sets up Bonita Platform before a Bonita runtime can be run:

* it creates the structure of the database (tables, primary / foreign keys, indices, ...)
* it inserts the default minimum data 
* it inserts the Bonita Engine + Bonita Portal minimum configuration in database
* it also allows to automatically configure a Bonita Tomcat bundle to run on the right database

Requirements
---
>     Java JDK 8 / 11

Running bonita-platform-setup
---
To run the tool:

* extract the Zip file
* configure access to the database in file `database.properties` at the root directory.
* run `./setup.sh [init | push | pull | configure | help]`

More details
---
See [the dedicated page](https://documentation.bonitasoft.com/bonita/latest/BonitaBPM_platform_setup) for more info
about the Bonita Platform Setup tool.