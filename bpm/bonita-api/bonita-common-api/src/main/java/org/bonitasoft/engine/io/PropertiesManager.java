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
import java.io.FileInputStream;
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

    public static void saveProperties(final Properties properties, final String pathName) throws IOException {
        saveProperties(properties, new File(pathName));
    }

    public static void saveProperties(final Properties properties, final File file) throws IOException {
        final FileOutputStream outputStream = new FileOutputStream(file);
        try {
            properties.store(outputStream, "Storing modified properties");
        } finally {
            outputStream.close();
        }
    }

    public static Properties getProperties(final URL url) throws IOException {
        final InputStreamReader reader = new InputStreamReader(url.openStream());
        try {
            return getProperties(reader);
        } finally {
            reader.close();
        }
    }

    public static Properties getProperties(final String fileName) throws IOException {
        return getProperties(new File(fileName));
    }

    public static Properties getProperties(final File file) throws IOException {
        final FileReader reader = new FileReader(file);
        try {
            return getProperties(reader);
        } finally {
            reader.close();
        }
    }

    private static Properties getProperties(final Reader reader) throws IOException {
        final Properties properties = new Properties();
        properties.load(reader);
        return properties;
    }

    public static void savePropertiesToXML(final Properties properties, final File file) throws IOException {
        final FileOutputStream outputStream = new FileOutputStream(file);
        try {
            properties.storeToXML(outputStream, "Storing modified properties", "UTF-8");
        } finally {
            outputStream.close();
        }
    }

    public static Properties getPropertiesFromXML(final File file) throws IOException {
        final Properties properties = new Properties();
        final FileInputStream fileInputStream = new FileInputStream(file);
        try {
            properties.loadFromXML(fileInputStream);
            return properties;
        } finally {
            fileInputStream.close();
        }
    }

}
