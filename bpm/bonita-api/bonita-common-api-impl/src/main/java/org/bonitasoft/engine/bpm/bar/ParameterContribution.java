/*
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.engine.bpm.bar;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.bonitasoft.engine.io.PropertiesManager;

/**
 * @author Matthieu Chaffotte
 */
public class ParameterContribution implements BusinessArchiveContribution {

    private static final String PARAMETERS_FILE = "parameters.properties";

    private static final String NULL = "-==NULLL==-";

    @Override
    public boolean readFromBarFolder(final BusinessArchive businessArchive, final File barFolder) throws IOException {
        final File file = new File(barFolder, PARAMETERS_FILE);
        if (!file.exists()) {
            return false;
        }
        final Properties properties = PropertiesManager.getProperties(file);
        final Map<String, String> parameters = new HashMap<String, String>(properties.size());
        for (final Entry<Object, Object> property : properties.entrySet()) {
            parameters.put((String) property.getKey(), (String) (property.getValue() == NULL ? null : property.getValue()));
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
            PropertiesManager.saveProperties(properties, file);
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

}
