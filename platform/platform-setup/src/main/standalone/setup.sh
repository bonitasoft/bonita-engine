#!/bin/sh

# Let's position into folder containing this script:
BASEDIR=$(cd $(dirname $( dirname "$0" )/..) && pwd -P)
cd ${BASEDIR}

CFG_FOLDER=${BASEDIR}/platform_conf
INITIAL_CFG_FOLDER=$CFG_FOLDER/initial
LIB_FOLDER=${BASEDIR}/lib

testReturnCode() {
  COD_RET=$1
  if [ ${COD_RET} -ne 0 ]; then
    echo "ERROR $1 $2"
    cd - 1>/dev/null
    exit ${COD_RET}
  fi
}

check_is_installed() {
	command -v $1 >/dev/null 2>&1 || {
                # command $1 not present:
                echo >&2 "For better feedback, please consider installing Unix 'tree' command"
                return 1
          }
     return 0
}

BONITA_DATABASE=$( grep '^db.vendor=' database.properties | sed -e 's/db.vendor=//g' )

if [ "$BONITA_DATABASE" != "h2" -a "$BONITA_DATABASE" != "postgres" -a "$BONITA_DATABASE" != "sqlserver" -a "$BONITA_DATABASE" != "oracle" -a "$BONITA_DATABASE" != "mysql"  ]; then
    echo "Cannot determine database vendor (valid values are h2, postgres, sqlserver, oracle, mysql)."
    echo "Please configure file ${BASEDIR}/database.properties properly."
    exit 1
fi

ACTION=${1:-""}
if [ "${ACTION}" != "init" -a "${ACTION}" != "pull" -a "${ACTION}" != "push"  ]; then
    echo "Missing action argument. Available values are: init, pull, push"
    exit 1
fi

echo "Action is ${ACTION}"
echo "Using database ${BONITA_DATABASE}"
export BONITA_DATABASE

java -cp "${BASEDIR}:${CFG_FOLDER}:${INITIAL_CFG_FOLDER}:${LIB_FOLDER}/*" -Dorg.bonitasoft.platform.setup.action=${ACTION} -Dspring.profiles.active=default -Dsysprop.bonita.db.vendor=${BONITA_DATABASE} org.bonitasoft.platform.setup.PlatformSetupApplication

testReturnCode $? "Executing platform setup (Java command)"

if [ "${ACTION}" = "pull" ]; then
    check_is_installed tree
    if [ $? -eq 0 ]; then
        echo "Pulled configuration:"
        tree ${CFG_FOLDER}/current
    fi
fi

# restore previous folder:
cd - 1>/dev/null
