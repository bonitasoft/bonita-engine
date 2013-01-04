/*
 * Copyright (C) 2012-2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package com.bonitasoft.engine.bpm.bar;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveContribution;
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
