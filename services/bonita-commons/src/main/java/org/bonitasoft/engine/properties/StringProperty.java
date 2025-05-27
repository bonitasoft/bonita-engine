/**
 * Copyright (C) 2025 Bonitasoft S.A.
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
package org.bonitasoft.engine.properties;

import static java.lang.String.valueOf;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StringProperty extends BonitaConfigProperty {

    private final String propertyValue;

    public StringProperty(String displayName, String propertyKey, String defaultValue) {
        super(displayName, propertyKey);
        propertyValue = initStringProperty(defaultValue);
    }

    String initStringProperty(String defaultValue) {
        String propertyValue = getProperty(defaultValue);
        log.info(
                "{} {}, you may set it using env property {} or System property -D{}",
                displayName, valueOf(propertyValue), envPropertyKey(), propertyKey);
        return propertyValue;
    }

    public String getValue() {
        return propertyValue;
    }

}
