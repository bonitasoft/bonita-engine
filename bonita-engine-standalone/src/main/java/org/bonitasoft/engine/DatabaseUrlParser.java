/**
 * Copyright (C) 2020 Bonitasoft S.A.
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.AllArgsConstructor;
import lombok.Data;

public class DatabaseUrlParser {

    @Data
    @AllArgsConstructor
    public static class DatabaseMetadata {

        private String serverName;
        private String port;
        private String databaseName;

    }

    public static DatabaseMetadata parsePostgresUrl(String url) {
        String regex = "jdbc:postgresql://([\\w\\d\\.-]+):(\\d+)/([\\w\\-_\\d]+).*";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);
        if (!matcher.find()) {
            throw new IllegalArgumentException(
                    "Unable to parse postgres url (no groups found): " + url + " using regex " + regex);
        }
        return new DatabaseMetadata(matcher.group(1), matcher.group(2), matcher.group(3));
    }
}
