/**
 * Copyright (C) 2018 Bonitasoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.gradle.docker

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

class DbParser {

    static DbConnectionSettings extractDbConnectionSettings(String dburl) {
        if (dburl.contains("sqlserver")) {
            return extractSqlServerDbConnectionSettings(dburl)
        }
        else if (dburl.contains("oracle")) {
            return extractOracleDbConnectionSettings(dburl)
        }
        return extractGenericDbConnectionSettings(dburl)
    }

    private static DbConnectionSettings extractGenericDbConnectionSettings(String dburl) {
        DbConnectionSettings settings = new DbConnectionSettings()
        settings.dbUrl = dburl

        def parsedUrl = (dburl =~ /(jdbc:\w+:\/\/)([\w\d\.-]+):(\d+)\/([\w\-_\d]+).*/)
        settings.serverName = parsedUrl[0][2]
        settings.portNumber = parsedUrl[0][3]
        settings.databaseName = parsedUrl[0][4]
        settings.genericUrl = parsedUrl[0][1] + settings.serverName + ":" + settings.portNumber + "/"

        settings
    }

    private static DbConnectionSettings extractOracleDbConnectionSettings(String dburl) {
        DbConnectionSettings settings = new DbConnectionSettings()
        settings.dbUrl = dburl

        def parsedUrl = (dburl =~ /(jdbc:.*:@\/\/)([\w\d\.-]+):(\d+)\/([\w\-_\.\d]+).*/)
        settings.serverName = parsedUrl[0][2]
        settings.portNumber = parsedUrl[0][3]
        settings.databaseName = parsedUrl[0][4]
        settings.genericUrl = dburl

        settings
    }

    private static DbConnectionSettings extractSqlServerDbConnectionSettings(String dburl) {
        DbConnectionSettings settings = new DbConnectionSettings()
        settings.dbUrl = dburl

        def parsedUrl = (dburl =~ /(jdbc:\w+:\/\/)([\w\d\.-]+):(\d+);database=([\w\-_\d]+).*/)
        settings.serverName = parsedUrl[0][2]
        settings.portNumber = parsedUrl[0][3]
        settings.databaseName = parsedUrl[0][4]
        settings.genericUrl = parsedUrl[0][1] + settings.serverName + ":" + settings.portNumber

        settings
    }

    @ToString
    @EqualsAndHashCode
    static class DbConnectionSettings {
        String dbUrl
        String serverName
        String portNumber
        String databaseName
        String genericUrl
    }

}
