@echo on

rem Set some JVM system properties required by Bonita

set PLATFORM_SETUP="-Dorg.bonitasoft.platform.setup.folder=%CATALINA_HOME%\..\setup"
set H2_DATABASE_DIR="-Dorg.bonitasoft.h2.database.dir=%CATALINA_HOME%\..\h2_database"
set INCIDENT_LOG_DIR="-Dorg.bonitasoft.engine.incident.folder=%CATALINA_HOME%\logs"

rem Define the RDMBS vendor use by Bonita Engine to store data. Valid values are: h2, postgres, sqlserver, oracle, mysql.
set DB_OPTS="-Dsysprop.bonita.db.vendor=h2"

rem Define the RDMBS vendor use by Bonita Engine to store Business Data. Valid values are: h2, postgres, sqlserver, oracle, mysql.
rem If you use different DB engines by tenants, please update directly bonita-tenant-community-custom.properties
set BDM_DB_OPTS="-Dsysprop.bonita.bdm.db.vendor=h2"

rem Arjuna (JTA service added to Tomcat and required by Bonita Engine for transaction management)
set ARJUNA_OPTS="-Dcom.arjuna.ats.arjuna.common.propertiesFile=%CATALINA_HOME%\conf\jbossts-properties.xml"

rem Optional JAAS configuration. Usually used when delegating authentication to LDAP / Active Directory server
rem set SECURITY_OPTS="-Djava.security.auth.login.config=%CATALINA_HOME%\conf\jaas-standard.cfg"

rem Pass the JVM system properties to Tomcat JVM using CATALINA_OPTS variable
set CATALINA_OPTS=%CATALINA_OPTS% %PLATFORM_SETUP% %H2_DATABASE_DIR% %DB_OPTS% %BDM_DB_OPTS% %ARJUNA_OPTS% %INCIDENT_LOG_DIR% -Dfile.encoding=UTF-8 -Xshare:auto -Xms1024m -Xmx1024m -XX:+HeapDumpOnOutOfMemoryError

set CATALINA_PID=%CATALINA_BASE%\catalina.pid
