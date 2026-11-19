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

import static java.lang.String.format;
import static java.lang.String.valueOf;

/**
 * @author Emmanuel Duchastenier
 */
public class BooleanProperty extends BonitaConfigProperty {

    private final boolean propertyValue;

    public BooleanProperty(String displayName, String propertyKey, boolean defaultValue) {
        super(displayName, propertyKey);
        propertyValue = Boolean.parseBoolean(getProperty(valueOf(defaultValue)));
        logInitializationMessagesIfFirstTime();
    }

    public boolean isEnabled() {
        return propertyValue;
    }

    @Override
    String getInitializationMessage() {
        return format("%s %s, you may %s it using env property %s or System property -D%s [=true/false]",
                displayName, isEnabled() ? "enabled" : "disabled", isEnabled() ? "disable" : "enable", envPropertyKey(),
                propertyKey);
    }
}
