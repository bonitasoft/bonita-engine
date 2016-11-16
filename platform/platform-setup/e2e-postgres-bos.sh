#!/bin/sh

testReturnCode() {
  COD_RET=$1
  if [ ${COD_RET} -ne 0 ]; then
    echo "ERROR $1 $2"
    exit ${COD_RET}
  fi
}

mvn clean install -DskipTests -f ../pom.xml

export VERSION=`cat ../platform-setup/target/classes/PLATFORM_ENGINE_VERSION`

echo "========================================"
echo "version:${VERSION}"
echo "========================================"
export E2E_DIR="target/e2e-postgres-bos"
export ZIP=Bonita-BPM-platform-setup-${VERSION}.zip

unzip -q -d ${E2E_DIR} target/${ZIP}

echo "Setting 'org.bonitasoft.platform.setup' log level to DEBUG"
# Activate in debug development phase:
#sed -i "s/org.bonitasoft.platform.setup\" level=\"INFO/org.bonitasoft.platform.setup\" level=\"DEBUG/g" ${E2E_DIR}/logback.xml
#sed -i "s/PlatformSetupApplication\" level=\"WARN/PlatformSetupApplication\" level=\"DEBUG/g" ${E2E_DIR}/logback.xml


echo "============================================================="
echo "Default H2 configuration detected should ask for confirmation"
echo "============================================================="
${E2E_DIR}/setup.sh init <<EOF
y
EOF
testReturnCode $? "setup.sh init with default H2 configuration confirmed"

${E2E_DIR}/setup.sh init <<EOF
n
EOF

if [ $? -eq 0 ]; then
    echo "setup should have exited with code != 0"
    exit 12
fi

echo "========================================"
echo "clean all tables"
echo "========================================"
psql postgresql://bonita:bpm@localhost:5432/bonita -q -f ${E2E_DIR}/platform_conf/sql/postgres/dropTables.sql
psql postgresql://bonita:bpm@localhost:5432/bonita -q -f ${E2E_DIR}/platform_conf/sql/postgres/dropQuartzTables.sql

echo "========================================"
echo "configure postgres"
sed -i s/^db.vendor=h2/db.vendor=postgres/g ${E2E_DIR}/database.properties
sed -i s/^db.user=.*/db.user=bonita/g ${E2E_DIR}/database.properties
sed -i s/^db.password=.*/db.password=bpm/g ${E2E_DIR}/database.properties
sed -i "s/^[# ]*db.server.name=.*/db.server.name=localhost/g" ${E2E_DIR}/database.properties
sed -i "s/^[# ]*db.server.port=.*/db.server.port=5432/g" ${E2E_DIR}/database.properties
sed -i s/^db.database.name=.*/db.database.name=bonita/g ${E2E_DIR}/database.properties
cat ${E2E_DIR}/database.properties
echo "========================================"

echo "=================================================="
echo "should skip non-supported environment for 'configure'"
echo "=================================================="
${E2E_DIR}/setup.sh configure

testReturnCode $? "setup.sh configure"

echo "========================================"
echo "should configure tomcat environment"
echo "========================================"
cp -rf src/test/resources/tomcat_conf/* ${E2E_DIR}/..

${E2E_DIR}/setup.sh configure
testReturnCode $? "setup.sh configure"

cat target/server/conf/bitronix-resources.properties | grep "^resource.ds1.className=org.postgresql.xa.PGXADataSource" > /dev/null
testReturnCode $? "Configuring bitronix-resources file with Postgres"

cat target/server/conf/Catalina/localhost/bonita.xml | grep "driverClassName=\"org.postgresql.Driver\"" > /dev/null
testReturnCode $? "Configuring bonita.xml file with Postgres"

cat target/server/bin/setenv.sh | grep "\-Dsysprop.bonita.db.vendor=postgres" > /dev/null
testReturnCode $? "Configuring setenv.sh file with Postgres"

echo "========================================"
echo "should configure wildfly environment"
echo "========================================"
# clean-up tomcat file and replace by wildfly:
rm -rf ${E2E_DIR}/../server ${E2E_DIR}/../setup
cp -rf src/test/resources/wildfly_conf/* ${E2E_DIR}/..

${E2E_DIR}/setup.sh configure
testReturnCode $? "setup.sh configure"

cat target/server/standalone/configuration/standalone.xml | grep "<connection-url>jdbc:postgresql://localhost:5432/bonita</connection-url>" > /dev/null
testReturnCode $? "Configuring standalone.xml file with Postgres for non-XA datasource"

cat target/server/standalone/configuration/standalone.xml | grep "<xa-datasource-property name=\"DatabaseName\">bonita</xa-datasource-property>" > /dev/null
testReturnCode $? "Configuring standalone.xml file with Postgres for XA datasource"

echo "========================================"
echo "should store to database "
echo "========================================"
${E2E_DIR}/setup.sh init

testReturnCode $? "setup.sh init"

echo "========================================"
echo "check tables"
echo "========================================"
psql postgresql://bonita:bpm@localhost:5432/bonita <<EOF
select t.table_catalog, t.table_schema, t.table_name from information_schema.tables t
where t.table_schema='public'
order by t.table_name;
EOF


echo "========================================"
echo "check platform data"
echo "========================================"
psql postgresql://bonita:bpm@localhost:5432/bonita <<EOF
SELECT
    p.id,
    p."version",
    p.initialversion,
    p.createdby,
    TO_CHAR(
        TO_TIMESTAMP(
            p.created / 1000
        ),
        'DD/MM/YYYY HH24:MI:SS'
    ) as creation_date
FROM
    platform p
EOF


echo "========================================"
echo "check configuration"
echo "========================================"
psql postgresql://bonita:bpm@localhost:5432/bonita  <<EOF
SELECT
    c.tenant_id,
    c.content_type,
    c.resource_name
FROM
    configuration c
ORDER BY
    c.tenant_id,
    c.content_type,
    c.resource_name
EOF



echo "========================================"
echo "simulation of engine start"
echo "========================================"
psql postgresql://bonita:bpm@localhost:5432/bonita  <<EOF
INSERT
    INTO
        configuration(
            tenant_id,
            content_type,
            resource_name,
            resource_content
        ) SELECT
            456,
            'TENANT_SECURITY_SCRIPTS',
            c.resource_name,
            c.resource_content
        FROM
            configuration c
        WHERE
            c.tenant_id = 0
            AND c.content_type ='TENANT_TEMPLATE_SECURITY_SCRIPTS'
EOF


echo "==========================================="
echo "retrieve configuration (to default folder) "
echo "==========================================="

${E2E_DIR}/setup.sh pull

echo "========================================"
echo "modify & push"
echo "========================================"

echo "new content" > ${E2E_DIR}/platform_conf/current/tenants/456/tenant_security_scripts/UserPermissionRule.groovy

echo "========================================"
echo "new content of file ${E2E_DIR}/platform_conf/current/tenants/456/tenant_security_scripts/UserPermissionRule.groovy is now:"
cat ${E2E_DIR}/platform_conf/current/tenants/456/tenant_security_scripts/UserPermissionRule.groovy
echo "========================================"

${E2E_DIR}/setup.sh push

echo "========================================"
echo "pull & check new value"
echo "========================================"

rm -rf ${E2E_DIR}/platform_conf/current/*

${E2E_DIR}/setup.sh pull

echo "------------------ Retrieved content should be 'new content' ---------"
cat ${E2E_DIR}/platform_conf/current/tenants/456/tenant_security_scripts/UserPermissionRule.groovy

echo "========================================"
echo "remove some files & push"
echo "========================================"

rm -rf  ${E2E_DIR}/platform_conf/current
mkdir -p ${E2E_DIR}/platform_conf/current/platform_engine
cp ${E2E_DIR}/platform_conf/initial/platform_engine/bonita-platform-custom.xml ${E2E_DIR}/platform_conf/current/platform_engine

tree ${E2E_DIR}/platform_conf/current

${E2E_DIR}/setup.sh push

echo "========================================"
echo "should contain only bonita-platform-custom.xml:"
tree ${E2E_DIR}/platform_conf/current
echo "========================================"

echo "========================================"
echo "should fail if driver class cannot be loaded:"
echo "========================================"
sed -i s/^postgres.nonXaDriver=.*$/postgres.nonXaDriver=org.UnknownClass/g ${E2E_DIR}/internal.properties
${E2E_DIR}/setup.sh push

echo "========================================"
echo "should fail if drivers not found:"
echo "========================================"
sed -i s/^postgres.nonXaDriver=org.UnknownClass*$/postgres.nonXaDriver=org.postgresql.Driver/g ${E2E_DIR}/internal.properties
rm ${E2E_DIR}/lib/postgres*.jar
${E2E_DIR}/setup.sh push

echo "========================================"
echo "end."
echo "========================================"


