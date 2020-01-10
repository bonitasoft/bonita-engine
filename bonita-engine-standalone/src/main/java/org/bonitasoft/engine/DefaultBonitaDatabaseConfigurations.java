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

    private static BonitaDatabaseConfiguration defaultH2Configuration = BonitaDatabaseConfiguration.builder()
            .dbVendor("h2")
            .url("jdbc:h2:file:" + System.getProperty("org.bonitasoft.h2.database.dir", "./h2databasedir")
                    + "/bonita;MVCC=TRUE;DB_CLOSE_ON_EXIT=FALSE;IGNORECASE=TRUE;AUTO_SERVER=TRUE")
            .user("bonita")
            .password("bpm").build();
    private static BonitaDatabaseConfiguration defaultPostgresConfiguration = BonitaDatabaseConfiguration.builder()
            .dbVendor("postgres")
            .url("jdbc:postgresql://localhost:5432/bonita")
            .user("bonita")
            .password("bpm").build();
    private static BonitaDatabaseConfiguration defaultMysqlConfiguration = BonitaDatabaseConfiguration.builder()
            .dbVendor("mysql")
            .url("jdbc:mysql://localhost:3306/bonita?useUnicode=true&characterEncoding=UTF-8&useSSL=false&allowPublicKeyRetrieval=true")
            .user("bonita")
            .password("bpm").build();
    private static BonitaDatabaseConfiguration defaultOracleConfiguration = BonitaDatabaseConfiguration.builder()
            .dbVendor("oracle")
            .url("jdbc:oracle:thin:@//localhost:1521/ORCLPDB1.localdomain")
            .user("bonita")
            .password("bpm").build();
    private static BonitaDatabaseConfiguration defaultSqlserverConfiguration = BonitaDatabaseConfiguration.builder()
            .dbVendor("sqlserver")
            .url("jdbc:sqlserver://localhost:1433;database=bonita")
            .user("bonita")
            .password("bpm").build();

    public static BonitaDatabaseConfiguration defaultConfiguration(String dbVendor) {
        switch (dbVendor) {
            case "h2":
                //clone it
                return defaultH2Configuration.toBuilder().build();
            case "postgres":
                return defaultPostgresConfiguration.toBuilder().build();
            case "mysql":
                return defaultMysqlConfiguration.toBuilder().build();
            case "oracle":
                return defaultOracleConfiguration.toBuilder().build();
            case "sqlserver":
                return defaultSqlserverConfiguration.toBuilder().build();
            default:
                throw new IllegalArgumentException("dbVendor " + dbVendor + " is not valid");
        }
    }

}
