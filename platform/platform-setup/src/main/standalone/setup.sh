#!/bin/sh

BASEDIR=$(cd $(dirname $( dirname "$0" )/..) && pwd -P)
cd ${BASEDIR}

# JAVA_CMD is exported by start-bonita.sh, so that same Java command is used:
JAVA_EXE=${JAVA_CMD:-java}

CFG_FOLDER=${BASEDIR}/platform_conf
INITIAL_CFG_FOLDER=$CFG_FOLDER/initial
LIB_FOLDER=${BASEDIR}/lib

BONITA_DATABASE=$( grep '^db.vendor=' database.properties | sed -e 's/db.vendor=//g' )

if [ "$BONITA_DATABASE" != "h2" -a "$BONITA_DATABASE" != "postgres" -a "$BONITA_DATABASE" != "sqlserver" -a "$BONITA_DATABASE" != "oracle" -a "$BONITA_DATABASE" != "mysql"  ]; then
    echo "Cannot determine database vendor (valid values are h2, postgres, sqlserver, oracle, mysql)."
    echo "Please configure file ${BASEDIR}/database.properties properly."
    exit 1
fi

"${JAVA_EXE}" -cp "${BASEDIR}:${CFG_FOLDER}:${INITIAL_CFG_FOLDER}:${LIB_FOLDER}/*" -Dspring.profiles.active=default -Dsysprop.bonita.db.vendor=${BONITA_DATABASE} org.bonitasoft.platform.setup.PlatformSetupApplication "$@"
COD_RET=$?
if [ ${COD_RET} -ne 0 ]; then
        cd - 1>/dev/null
        exit ${COD_RET}
fi
# restore previous folder:
cd - 1>/dev/null
