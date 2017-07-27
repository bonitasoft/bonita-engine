#!/bin/sh

# Set some JVM system properties required by Bonita

PLATFORM_SETUP="-Dorg.bonitasoft.platform.setup.folder=${CATALINA_HOME}/../setup"
#When changing org.bonitasoft.h2.database.dir location, update ${CATALINA_HOME}/conf/bitronix-resources.properties datasources accordingly.
H2_DATABASE_DIR="-Dorg.bonitasoft.h2.database.dir=${CATALINA_HOME}/../h2_database"
INCIDENT_LOG_DIR="-Dorg.bonitasoft.engine.incident.folder=${CATALINA_HOME}/logs"

# Define the RDMBS vendor use by Bonita Engine to store data. Valid values are: h2, postgres, sqlserver, oracle, mysql.
DB_OPTS="-Dsysprop.bonita.db.vendor=h2"

# Define the RDMBS vendor use by Bonita Engine to store Business Data. Valid values are: h2, postgres, sqlserver, oracle, mysql.
# If you use different DB engines by tenants, please update directly bonita-tenant-community-custom.properties
BDM_DB_OPTS="-Dsysprop.bonita.bdm.db.vendor=h2"

# Bitronix (JTA service added to Tomcat and required by Bonita Engine for transaction management)
BTM_OPTS="-Dbtm.root=${CATALINA_HOME} -Dbitronix.tm.configuration=${CATALINA_HOME}/conf/bitronix-config.properties"

# Optional JAAS configuration. Usually used when delegating authentication to LDAP / Active Directory server
#SECURITY_OPTS="-Djava.security.auth.login.config=${CATALINA_HOME}/conf/jaas-standard.cfg"

# Pass the JVM system properties to Tomcat JVM using CATALINA_OPTS variable
CATALINA_OPTS="${CATALINA_OPTS} ${PLATFORM_SETUP} ${H2_DATABASE_DIR} ${DB_OPTS} ${BDM_DB_OPTS} ${BTM_OPTS} ${INCIDENT_LOG_DIR} -Dfile.encoding=UTF-8 -Xshare:auto -Xms1024m -Xmx1024m -XX:+HeapDumpOnOutOfMemoryError"
export CATALINA_OPTS

# Only set CATALINA_PID if not already set (check for empty value) by startup script (usually done by /etc/init.d/tomcat8 but not by startup.sh nor catalina.sh)
if [ -z ${CATALINA_PID+x} ]; then
        CATALINA_PID=${CATALINA_BASE}/catalina.pid;
        export CATALINA_PID;
fi
