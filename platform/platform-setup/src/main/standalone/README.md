# Bonita BPM Platform Setup

## What it does

**Bonita BPM Platform Setup tool** sets up Bonita BPM database before **Bonita BPM** first start with initial configuration.
Use it also to update Bonita BPM configuration (including license files in subscription editions).

* init: creates tables in the database and inserts the initial configuration data
* pull / push: retrieves database configuration to local folder and updates database with locally modified configuration
* configure: configures automatically the Web Application Server, preventing the need for tedious manual configuration


## Requirements
>     Java JDK 1.7 or higher

## Running Bonita BPM Platform Setup

### Configuration 

* configure access to the database in `database.properties`: set up database credentials
* add JDBC drivers (specific to your database) in `/lib` folder (for Oracle and SqlServer only, as PostgreSQL and MySQL drivers are already included)
* specific log-level can be configured in file `logback.xml`

### Run

#### Init

If you use Bonita BPM bundles, you don't need to run this command. Else, run this init command once to create database structure before launching Bonita BPM.

on Linux / OS X:
```shell
setup.sh init
```
on Windows:
```shell
setup.bat init
```

 * Database structure (tables) will be created on target database (done only once).
 * All configuration files under `platform_conf/initial` and licenses under `platform_conf/licenses` will be written in database.

#### Pull

This step is mandatory before updating the configuration to ensure database consistency. For example, when a new tenant is created by Bonita BPM, related configuration files need to be extracted before any modification of the configuration.

on Linux / OS X:
```shell
setup.sh pull
```
on Windows:
```shell
setup.bat pull
```
 * The configuration currently in database will be written in the folder `platform_conf/current`.
 * License Files in database will be written in the folder `platform_conf/licenses` (Subscription editions only).
 * Any previous configuration, including license files will be overwritten.

#### Push

Once current configuration has been pulled and locally modified, use push to update configuration in database.
Those modifications will be applied only after Bonita BPM restart.

 on Linux / OS X:
 ```shell
 setup.sh push
 ```
 on Windows:
 ```shell
 setup.bat push
 ```
* All configuration files under `platform_conf/current` will be written in database.
* License files in the folder `platform_conf/licenses` will be written in database (Subscription editions only).
* Any previous configuration in database will be overwritten.
 
### Configure

The **Setup tool** also offers the possibility to configure the Application Server without the need for a manual configuration.
For now, only **Tomcat 7** auto-configuration is supported.

* sets db vendor + bdm db vendor in file setenv.sh / .bat
* sets all required database configuration in file conf/bitronix-resources.properties
* sets all required database configuration in file conf/Catalina/localhost/bonita.xml
* copies the required drivers from setup/lib to lib/bonita from setup/lib folder

For more detailed information, see file `database.properties`.

on Linux / OS X:
```shell
setup.sh configure
```
on Windows:
```shell
setup.bat configure
```