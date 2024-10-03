#!/bin/sh


echo "========================================"
echo "disconnect users"
echo "========================================"
psql postgresql://postgres:postgres@localhost:5432/postgres <<EOF
 SELECT pg_terminate_backend(pg_stat_activity.pid)
                    FROM pg_stat_activity
                    WHERE pg_stat_activity.datname = 'bonita'
                    AND pid in ( SELECT pid
                    FROM pg_stat_activity
                    WHERE upper(pg_stat_activity.datname) = upper('bonita')
                      AND pid <> pg_backend_pid())
EOF

echo "========================================"
echo "drop DB"
echo "========================================"
psql postgresql://postgres:postgres@localhost:5432/postgres <<EOF
DROP DATABASE bonita;
EOF


echo "========================================"
echo "create DB"
echo "========================================"
psql postgresql://postgres:postgres@localhost:5432/postgres <<EOF
CREATE DATABASE bonita
  WITH OWNER = bonita
       ENCODING = 'UTF8'
       TABLESPACE = pg_default
       LC_COLLATE = 'fr_FR.UTF-8'
       LC_CTYPE = 'fr_FR.UTF-8'
       CONNECTION LIMIT = -1;
GRANT CONNECT, TEMPORARY ON DATABASE bonita TO public;
GRANT ALL ON DATABASE bonita TO bonita;
EOF



echo "========================================"
echo "clean all tables"
echo "========================================"
psql postgresql://bonita:bpm@localhost:5432/bonita -f ../platform-resources/src/main/resources/sql/postgres/dropTables.sql
psql postgresql://bonita:bpm@localhost:5432/bonita -f ../platform-resources/src/main/resources/sql/postgres/dropQuartzTables.sql



echo "========================================"
echo "check tables"
echo "========================================"
psql postgresql://bonita:bpm@localhost:5432/bonita <<EOF
select t.table_catalog, t.table_schema, t.table_name from information_schema.tables t
where t.table_schema='public'
order by t.table_name;
EOF

