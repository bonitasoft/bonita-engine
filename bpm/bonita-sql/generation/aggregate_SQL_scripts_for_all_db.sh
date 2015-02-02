#!/bin/sh

# to be launched at the root of bonita-engine folder

concat(){
	db=$1
	find . -name ${db}-createTables.sql | grep -v test | grep -v target | xargs cat | tr -d '\n' | sed -e 's/;/;\n/g' -e 's/GO/\n/g' | sort > create_tables_${db}.sql
	find . -name ${db}-cleanTables.sql | grep -v test | grep -v target | xargs cat | tr -d '\n' | sed -e 's/;/;\n/g' -e 's/GO/\n/g' | sort > clean_tables_${db}.sql
	find . -name ${db}-dropTables.sql | grep -v test | grep -v target | xargs cat | tr -d '\n' | sed -e 's/;/;\n/g' -e 's/GO/\n/g' | sort > drop_tables_${db}.sql
	find . -name ${db}-deleteTenantObjects.sql | grep -v test | grep -v target | xargs cat | tr -d '\n' | sed -e 's/;/;\n/g' -e 's/GO/\n/g' | sort > deleteTenantObjects_${db}.sql
	find . -name ${db}-initTables.sql | grep -v test | grep -v target | xargs cat | tr -d '\n' | sed -e 's/;/;\n/g' -e 's/GO/\n/g' | sort > init_tenant_tables_${db}.sql
}

dbs="h2 oracle postgres sqlserver mysql"

for x in ${dbs}
do
	concat $x
done
