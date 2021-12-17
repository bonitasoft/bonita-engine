/**
 * Copyright (C) 2021 Bonitasoft S.A.
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

/**
 * @author Emmanuel Duchastenier
 */
@Slf4j
public class BooleanProperty {

    private final String displayName;
    /*
     * System property version of the property (lowercase, with dots):
     */
    private final String propertyKey;
    private final boolean propertyValue;

    public BooleanProperty(String displayName, String propertyKey, boolean defaultValue) {
        this.propertyKey = propertyKey;
        this.displayName = displayName;
        propertyValue = initBooleanProperty(defaultValue);
    }

    boolean initBooleanProperty(boolean dynamicPermissionsEnabled) {
        boolean enabled = Boolean.parseBoolean(
                System.getProperty(propertyKey,
                        System.getenv().getOrDefault(envProperty(), valueOf(dynamicPermissionsEnabled))));
        log.info(
                "{} {}, you may {} it using env property {} or System property -D{} [=true/false]",
                displayName, enabled ? "enabled" : "disabled", enabled ? "disable" : "enable", envProperty(),
                propertyKey);
        return enabled;
    }

    private String envProperty() {
        return propertyKey.toUpperCase().replace(".", "_");
    }

    public boolean isEnabled() {
        return propertyValue;
    }

}
