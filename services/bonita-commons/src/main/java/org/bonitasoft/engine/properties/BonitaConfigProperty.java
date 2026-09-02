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

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;

/**
 * Represents a Bonita configuration property. It supports properties passed as Java System property (like
 * -Dmy.custom-property.subproperty=MY_VALUE) or as environment variables (like MY_CUSTOMPROPERTY_SUBPROPERTY=MY_VALUE).
 * If both are defined, System Property has precedence.
 *
 * @author Emmanuel Duchastenier
 */
@Slf4j
public abstract class BonitaConfigProperty {

    /*
     * System property version of the property (lowercase, with dots):
     */
    protected final String propertyKey;

    /*
     * Display name for logs
     */
    protected final String displayName;

    // A simple "cache" to avoid logging the same property multiple times
    private static final Set<String> alreadyLoggedProperties = ConcurrentHashMap.newKeySet();

    /**
     * @param displayName the display name of the property, used in logs
     * @param propertyKey the "system property" version of the property, typically in lowercase and with dots.
     */
    public BonitaConfigProperty(String displayName, String propertyKey) {
        this.displayName = displayName;
        this.propertyKey = propertyKey;
    }

    protected void logInitializationMessagesIfFirstTime() {
        if (alreadyLoggedProperties.add(this.propertyKey)) {
            log.info(getInitializationMessage());
        }
    }

    abstract String getInitializationMessage();

    protected String envPropertyKey() {
        return propertyKey.toUpperCase().replace(".", "_").replaceAll("-", "");
    }

    protected String getProperty(String defaultValue) {
        return System.getProperty(propertyKey, System.getenv().getOrDefault(envPropertyKey(), defaultValue));
    }

    // for test reset
    static void clearLoggedProperties() {
        alreadyLoggedProperties.clear();
    }
}
