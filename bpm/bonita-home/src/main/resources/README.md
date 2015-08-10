Bonita home
============
What is it
------------
The bonita home contains all configuration files needed to run the engine. It also contains some runtime data stored by the engine itself.

How it works
------------
Each part of the bonita home contains at least two folders:

#### work
This folder must never be modified. Here the engine stores runtime data like process definitions, and jars for the classpath. it also contains all the default spring files and properties files
#### conf
Here you can customize the engine configuration. Everything uncommented in properties files overrides the default configuration.

Structure
--------
# `engine-client`
Configuration of the connection to the engine server.
## `engine-client/conf`
Contains `bonita-client-custom.properties` that should be changed in order to connect to e.g. a remote engine.
## `engine-client/work`
Default configuration for the client is the local jvm connection.
# `engine-server`
Contains configuration and runtime data of the engine server.
## `engine-server/conf`
Customize the configuration here.
### `engine-server/conf/platform`
You can change the platform configuration in the `bonita-platform-community-custom.properties` file by uncommenting lines, and replace services implementations by adding them in the `bonita-platform-custom.xml`.
### `engine-server/conf/platform-init`
You can here specify which edition you are using in the `bonita-platform-init-community-custom.properties`
### `engine-server/conf/tenants`
Contains configurations of tenants.
#### `engine-server/conf/tenants/template`
Default files that are copied when a new tenant is created. You can change the new tenant configuration in the `bonita-tenant-community-custom.properties` by uncommenting lines, and replace service implementations by adding them in the `bonita-tenants-custom.xml`.
#### `engine-server/conf/tenants/<tenant_id>`
Configuration of the tenant having the id `<tenant_id>`, see section on `engine-server/conf/tenants/template`  for more details.
### `engine-server/temp`
Contains temporary data, can be deleted when the engine is Shutdown
### `engine-server/work`
Contains runtime data and default configuration. Content is handled by the engine **Should not be modified**
