/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine;

public class DefaultBonitaDatabaseConfigurations {

    private static BonitaDatabaseConfiguration defaultH2Configuration(String schemaName) {
        return BonitaDatabaseConfiguration.builder()
                .dbVendor("h2")
                .url("jdbc:h2:file:" + System.getProperty("org.bonitasoft.h2.database.dir", "./h2databasedir")
                        + "/" + schemaName + ";MVCC=TRUE;DB_CLOSE_ON_EXIT=FALSE;IGNORECASE=TRUE;AUTO_SERVER=TRUE")
                .user("bonita")
                .password("bpm").build();
    }

    private static BonitaDatabaseConfiguration defaultPostgresConfiguration(String schemaName) {
        return BonitaDatabaseConfiguration.builder()
                .dbVendor("postgres")
                .url("jdbc:postgresql://localhost:5432/" + schemaName)
                .user("bonita")
                .password("bpm").build();
    }

    private static BonitaDatabaseConfiguration defaultMysqlConfiguration(String schemaName) {
        return BonitaDatabaseConfiguration.builder()
                .dbVendor("mysql")
                .url("jdbc:mysql://localhost:3306/" + schemaName
                        + "?useUnicode=true&characterEncoding=UTF-8&useSSL=false&allowPublicKeyRetrieval=true")
                .user("bonita")
                .password("bpm").build();
    }

    private static BonitaDatabaseConfiguration defaultOracleConfiguration(String schemaName) {
        return BonitaDatabaseConfiguration.builder()
                .dbVendor("oracle")
                .url("jdbc:oracle:thin:@//localhost:1521/ORCLPDB1?oracle.net.disableOob=true")
                .user(schemaName)
                .password("bpm").build();
    }

    private static BonitaDatabaseConfiguration defaultSqlserverConfiguration(String schemaName) {
        return BonitaDatabaseConfiguration.builder()
                .dbVendor("sqlserver")
                .url("jdbc:sqlserver://localhost:1433;database=" + schemaName)
                .user("bonita")
                .password("bpm").build();
    }

    public static BonitaDatabaseConfiguration defaultConfiguration(String dbVendor, String schemaName) {
        switch (dbVendor) {
            case "h2":
                //clone it
                return defaultH2Configuration(schemaName);
            case "postgres":
                return defaultPostgresConfiguration(schemaName);
            case "mysql":
                return defaultMysqlConfiguration(schemaName);
            case "oracle":
                return defaultOracleConfiguration(schemaName);
            case "sqlserver":
                return defaultSqlserverConfiguration(schemaName);
            default:
                throw new IllegalArgumentException("dbVendor " + dbVendor + " is not valid");
        }
    }

}
