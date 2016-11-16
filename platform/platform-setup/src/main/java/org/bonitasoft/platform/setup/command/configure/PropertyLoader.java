/*
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
 */
package org.bonitasoft.platform.setup.command.configure;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.bonitasoft.platform.exception.PlatformException;

/**
 * @author Emmanuel Duchastenier
 */
public class PropertyLoader {

    private final List<String> propertyFiles;

    public PropertyLoader(String... propertyFiles) {
        this.propertyFiles = Arrays.asList(propertyFiles);
    }

    public PropertyLoader() {
        this("/database.properties", "/internal.properties");
    }

    public Properties loadProperties() throws PlatformException {
        final Properties properties = new Properties();
        for (String propertyFile : propertyFiles) {
            try {
                properties.load(this.getClass().getResourceAsStream(propertyFile));
            } catch (IOException e) {
                throw new PlatformException("Error reading configuration file " + propertyFile +
                        ". Please make sure the file is present at the root of the Platform Setup Tool folder, and that is has not been moved of deleted", e);
            }
        }
        return properties;
    }
}
