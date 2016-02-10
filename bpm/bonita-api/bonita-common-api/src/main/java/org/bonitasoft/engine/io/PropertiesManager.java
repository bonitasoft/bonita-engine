/**
 * Copyright (C) 2015 BonitaSoft S.A.
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
package org.bonitasoft.engine.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Properties;

/**
 * @author Matthieu Chaffotte
 * @author Frederic Bouquet
 * @author Celine Souchet
 */
public class PropertiesManager {

    public static void saveProperties(final Properties properties, final File file) throws IOException {
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            properties.store(outputStream, "Storing modified properties");
        }
    }

    public static Properties getProperties(final URL url) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(url.openStream())) {
            return getProperties(reader);
        }
    }

    public static Properties getProperties(final String fileName) throws IOException {
        return getProperties(new File(fileName));
    }

    public static Properties getProperties(final File file) throws IOException {
        try (FileReader reader = new FileReader(file)) {
            return getProperties(reader);
        }
    }

    private static Properties getProperties(final Reader reader) throws IOException {
        final Properties properties = new Properties();
        properties.load(reader);
        return properties;
    }

}
