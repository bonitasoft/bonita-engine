/**
 * Copyright (C) 2016 Bonitasoft S.A.
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
package org.bonitasoft.console.common.server.preferences.properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * @author Baptiste Mesta
 */
public class PropertiesWithSet extends Properties {

    public PropertiesWithSet(Properties properties) {
        super(properties);
    }

    public PropertiesWithSet(File file) {
        try (FileInputStream inStream = new FileInputStream(file)) {
            this.load(inStream);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public Set<String> getPropertyAsSet(final String propertyName) {
        return stringToSet(getProperty(propertyName));
    }

    public static Set<String> stringToSet(final String propertyValueAsString) {
        if (propertyValueAsString != null) {
            final Set<String> propertiesSet = new HashSet<>();
            final String propertyValueAsStringTrimmed = propertyValueAsString.trim();
            if (propertyValueAsStringTrimmed.startsWith("[") && propertyValueAsStringTrimmed.endsWith("]")) {
                String propertyCSV = propertyValueAsStringTrimmed.substring(1,
                        propertyValueAsStringTrimmed.length() - 1);
                propertyCSV = propertyCSV.trim();
                if (propertyCSV.isEmpty()) {
                    return Collections.emptySet();
                }
                final String[] propertyArray = propertyCSV.split(",");
                for (final String propertyValue : propertyArray) {
                    propertiesSet.add(propertyValue.trim());
                }
            } else {
                propertiesSet.add(propertyValueAsString);
            }
            return propertiesSet;
        } else {
            return Collections.emptySet();
        }
    }

}
