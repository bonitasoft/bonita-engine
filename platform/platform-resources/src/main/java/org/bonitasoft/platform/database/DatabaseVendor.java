/**
 * Copyright (C) 2024 Bonitasoft S.A.
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
package org.bonitasoft.platform.database;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum DatabaseVendor {

    H2("h2"), //
    MYSQL("mysql"), //
    ORACLE("oracle"), //
    POSTGRES("postgres"), //
    SQLSERVER("sqlserver");

    private final String value;

    public static DatabaseVendor parseValue(String databaseVendorValue) {
        for (DatabaseVendor databaseVendor : DatabaseVendor.values()) {
            if (databaseVendor.value.equalsIgnoreCase(databaseVendorValue)) {
                return databaseVendor;
            }
        }
        throw new IllegalArgumentException("Unknown database vendor: " + databaseVendorValue);
    }

    public boolean equalsValue(String anotherValue) {
        return value.equals(anotherValue);
    }
}
