#!/bin/sh

# to be launched in the folder containing h2 / postgres / ... sub-folders:

sortSqlScripts(){
	db=$1
	cat ${db}/createTables.sql | tr -d '\n' | sed -e 's/;/;\n/g' -e 's/GO/\n/g' | sort > create_tables_${db}.sql
	cat ${db}/cleanTables.sql | tr -d '\n' | sed -e 's/;/;\n/g' -e 's/GO/\n/g' | sort > clean_tables_${db}.sql
	cat ${db}/dropTables.sql | tr -d '\n' | sed -e 's/;/;\n/g' -e 's/GO/\n/g' | sort > drop_tables_${db}.sql
	cat ${db}/deleteTenantObjects.sql | tr -d '\n' | sed -e 's/;/;\n/g' -e 's/GO/\n/g' | sort > deleteTenantObjects_${db}.sql
	cat ${db}/initTenantTables.sql | tr -d '\n' | sed -e 's/;/;\n/g' -e 's/GO/\n/g' | sort > init_tenant_tables_${db}.sql
}

dbs="h2 oracle postgres sqlserver mysql"

for x in ${dbs}
do
	sortSqlScripts $x
done
