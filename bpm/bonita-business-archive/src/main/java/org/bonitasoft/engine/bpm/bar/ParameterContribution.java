/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.bpm.bar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * @author Matthieu Chaffotte
 */
public class ParameterContribution implements BusinessArchiveContribution {

    static final String PARAMETERS_FILE = "parameters.properties";

    public static final String NULL = "-==NULLL==-";

    @Override
    public boolean readFromBarFolder(final BusinessArchive businessArchive, final File barFolder) throws IOException {
        final File file = new File(barFolder, PARAMETERS_FILE);
        if (!file.exists()) {
            return false;
        }
        final Properties properties = loadProperties(file);
        final Map<String, String> parameters = new HashMap<>(properties.size());
        for (final Entry<Object, Object> property : properties.entrySet()) {
            parameters.put((String) property.getKey(),
                    (String) (NULL.equals(property.getValue()) ? null : property.getValue()));
        }
        businessArchive.setParameters(parameters);
        return true;
    }

    @Override
    public void saveToBarFolder(final BusinessArchive businessArchive, final File barFolder) throws IOException {
        final Map<String, String> parameters = businessArchive.getParameters();
        if (parameters != null) {
            final Properties properties = new Properties();
            for (final Entry<String, String> entry : parameters.entrySet()) {
                properties.put(entry.getKey(), entry.getValue() == null ? NULL : entry.getValue());
            }
            final File file = new File(barFolder, PARAMETERS_FILE);
            saveProperties(properties, file);
        }
    }

    @Override
    public boolean isMandatory() {
        return false;
    }

    @Override
    public String getName() {
        return "Parameters";
    }

    static Properties loadProperties(File parametersFile) throws IOException {
        try (var reader = new FileReader(parametersFile)) {
            var properties = new Properties();
            properties.load(reader);
            return properties;
        }
    }

    static void saveProperties(Properties properties, File parametersFile) throws IOException {
        try (FileOutputStream outputStream = new FileOutputStream(parametersFile)) {
            properties.store(outputStream, "Storing modified properties");
        }
    }

}
