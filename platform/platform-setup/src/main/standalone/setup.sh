#!/bin/sh
set -e

# Function that exits with an error message if the vendor is not supported
# - first argument is the database vendor value to check
check_vendor_supported() {
  db_vendor=$1
  is_supported=false
  # supported databases:
  set -- h2 postgres
  for db in "$@"; do
    if [ "$db" = "$db_vendor" ]; then
      is_supported=true
      break
    fi
  done
  if [ "$is_supported" = false ]; then
    echo "ERROR: Unsupported database vendor (valid values are h2, postgres)."
    echo "For access to additional databases (oracle, mysql, sqlserver), please consider upgrading to the Enterprise Edition."
    echo "Please update file ${BASEDIR}/database.properties to set a valid value."
    exit 1
  fi
}

# Let's position into the folder containing this script:
BASEDIR=$(cd "$(dirname "$(dirname "$0")/..")" && pwd -P)
cd "${BASEDIR}"

# JAVA_CMD is exported by start-bonita.sh, so that same Java command is used:
JAVA_EXE=${JAVA_CMD:-java}

CFG_FOLDER=${BASEDIR}/platform_conf
INITIAL_CFG_FOLDER=$CFG_FOLDER/initial

for lib in lib/*.jar; do
  LIBS_CP="${LIBS_CP}:${lib}"
done

BONITA_DATABASE=$(grep '^db.vendor=' database.properties | sed -e 's/db.vendor=//g')
check_vendor_supported "$BONITA_DATABASE"

BONITA_BDM_DATABASE=$(grep '^bdm.db.vendor=' database.properties | sed -e 's/bdm.db.vendor=//g')
check_vendor_supported "$BONITA_BDM_DATABASE"

"${JAVA_EXE}" -cp "${BASEDIR}:${CFG_FOLDER}:${INITIAL_CFG_FOLDER}${LIBS_CP}" "${JVM_OPTS}" \
    -Dsysprop.bonita.db.vendor="${BONITA_DATABASE}" \
    -Dsysprop.bonita.bdm.db.vendor="${BONITA_BDM_DATABASE}" \
    org.bonitasoft.platform.setup.PlatformSetupApplication "$@"
COD_RET=$?
if [ ${COD_RET} -ne 0 ]; then
  cd - 1>/dev/null
  exit ${COD_RET}
fi
# restore previous folder:
cd - 1>/dev/null
