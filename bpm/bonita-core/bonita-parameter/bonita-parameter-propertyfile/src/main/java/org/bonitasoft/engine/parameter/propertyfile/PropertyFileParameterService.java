/*
 * Copyright (C) 2011-2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package org.bonitasoft.engine.parameter.propertyfile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.home.BonitaHomeServer;
import org.bonitasoft.engine.io.PropertiesManager;
import org.bonitasoft.engine.parameter.OrderBy;
import org.bonitasoft.engine.parameter.ParameterService;
import org.bonitasoft.engine.parameter.SOutOfBoundException;
import org.bonitasoft.engine.parameter.SParameter;
import org.bonitasoft.engine.parameter.SParameterNameNotFoundException;
import org.bonitasoft.engine.parameter.SParameterProcessNotFoundException;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.bonitasoft.engine.sessionaccessor.TenantIdNotSetException;

import com.bonitasoft.manager.Features;
import com.bonitasoft.manager.Manager;

/**
 * @author Matthieu Chaffotte
 * @author Zhao Na
 */
public class PropertyFileParameterService implements ParameterService {

    private static final String PARAMETERS_PROPERTIES = "current-parameters.properties";

    private static final String NULL = "-==NULLL==-";

    private final ReadSessionAccessor sessionAccessor;

    public PropertyFileParameterService(final ReadSessionAccessor sessionAccessor) {
        this.sessionAccessor = sessionAccessor;
    }

    @Override
    public void update(final long processDefinitionId, final String parameterName, final String parameterValue) throws SParameterProcessNotFoundException,
            SParameterNameNotFoundException {
        try {
            final String filePath = getFilePath(processDefinitionId);
            final Properties properties = PropertiesManager.getPropertiesFromFile(filePath);
            if (!properties.containsKey(parameterName)) {
                throw new SParameterNameNotFoundException("The paramter name " + parameterName + " does not exist");
            }
            final String newValue = parameterValue == null ? NULL : parameterValue;
            putProperty(filePath, parameterName, newValue);
        } catch (final BonitaHomeNotSetException e) {
            throw new SParameterProcessNotFoundException(e);
        } catch (final IOException e) {
            throw new SParameterProcessNotFoundException(e);
        } catch (final TenantIdNotSetException e) {
            throw new SParameterProcessNotFoundException(e);
        }
    }

    @Override
    public void addAll(final long processDefinitionId, final Map<String, String> parameters) throws SParameterProcessNotFoundException {
        if (!Manager.isFeatureActive(Features.CREATE_PARAMETER)) {
            throw new IllegalStateException("The parameter creation is not an active feature");
        }
        try {
            final String filePath = getFilePathWithoutChecking(processDefinitionId);
            final Properties properties = new Properties();
            if (parameters != null) {
                for (final Entry<String, String> parameter : parameters.entrySet()) {
                    final String value = parameter.getValue() == null ? NULL : parameter.getValue();
                    properties.put(parameter.getKey(), value);
                }
            }
            final File file = new File(filePath);
            file.createNewFile();
            PropertiesManager.saveProperties(properties, file);
        } catch (final BonitaHomeNotSetException e) {
            throw new SParameterProcessNotFoundException(e);
        } catch (final IOException e) {
            throw new SParameterProcessNotFoundException(e);
        } catch (final TenantIdNotSetException e) {
            throw new SParameterProcessNotFoundException(e);
        }
    }

    @Override
    public void deleteAll(final long processDefinitionId) throws SParameterProcessNotFoundException {
        try {
            final String filePath = getFilePath(processDefinitionId);
            final File file = new File(filePath);
            if (!file.exists()) {
                final StringBuilder errorBuilder = new StringBuilder();
                errorBuilder.append("The process definition ").append(processDefinitionId).append(" does not exist");
                throw new SParameterProcessNotFoundException(errorBuilder.toString());
            }
            final boolean isDeleted = file.delete();
            if (!isDeleted) {
                throw new SParameterProcessNotFoundException("The property file was not deleted propertly");
            }
        } catch (final BonitaHomeNotSetException e) {
            throw new SParameterProcessNotFoundException(e);
        } catch (final TenantIdNotSetException e) {
            throw new SParameterProcessNotFoundException(e);
        }
    }

    private String getFilePathWithoutChecking(final long processDefinitionId) throws BonitaHomeNotSetException, TenantIdNotSetException {
        final long tenantId = sessionAccessor.getTenantId();
        final String processesFolder = BonitaHomeServer.getInstance().getProcessesFolder(tenantId);
        final StringBuilder pathBuilder = new StringBuilder(processesFolder);
        pathBuilder.append(File.separatorChar).append(processDefinitionId);
        pathBuilder.append(File.separatorChar).append(PARAMETERS_PROPERTIES);
        return pathBuilder.toString();
    }

    private String getFilePath(final long processDefinitionId) throws BonitaHomeNotSetException, TenantIdNotSetException, SParameterProcessNotFoundException {
        final long tenantId = sessionAccessor.getTenantId();
        final String processesFolder = BonitaHomeServer.getInstance().getProcessesFolder(tenantId);
        final StringBuilder pathBuilder = new StringBuilder(processesFolder);
        pathBuilder.append(File.separatorChar).append(processDefinitionId);
        final File file = new File(pathBuilder.toString());
        if (!file.exists()) {
            final StringBuilder errorBuilder = new StringBuilder();
            errorBuilder.append("The process definition ").append(processDefinitionId).append(" does not exist");
            throw new SParameterProcessNotFoundException(errorBuilder.toString());
        }
        pathBuilder.append(File.separatorChar).append(PARAMETERS_PROPERTIES);
        return pathBuilder.toString();
    }

    private List<SParameter> getListProperties(final String fileName) throws IOException {
        final Properties properties = PropertiesManager.getPropertiesFromFile(fileName);
        final List<SParameter> paramters = new ArrayList<SParameter>();
        for (final Entry<Object, Object> property : properties.entrySet()) {
            String value = (String) property.getValue();
            if (NULL.equals(value)) {
                value = null;
            }
            paramters.add(new SParameterImpl(property.getKey().toString(), value));
        }
        return paramters;
    }

    private List<SParameter> getOrderedParameters(final String filePath, final OrderBy order) throws IOException {
        final Comparator<SParameter> sorting;
        switch (order) {
            case NAME_DESC:
                sorting = new NameDescComparator(NULL);
                break;
            default:
                sorting = new NameAscComparator(NULL);
                break;
        }
        final List<SParameter> parameters = getListProperties(filePath);
        final List<SParameter> sortedList = new ArrayList<SParameter>(parameters);
        Collections.sort(sortedList, sorting);
        return sortedList;
    }

    private synchronized void putProperty(final String fileName, final String key, final String value) throws IOException {
        final Properties properties = PropertiesManager.getPropertiesFromFile(fileName);
        properties.put(key, value);
        PropertiesManager.saveProperties(properties, fileName);
    }

    @Override
    public boolean containsNullValues(final long processDefinitionId) throws SParameterProcessNotFoundException {
        String filePath;
        try {
            filePath = getFilePath(processDefinitionId);
            final Properties properties = PropertiesManager.getPropertiesFromFile(filePath);
            final Collection<Object> values = properties.values();
            final Iterator<Object> iterator = values.iterator();
            boolean contains = false;
            while (!contains && iterator.hasNext()) {
                final String value = iterator.next().toString();
                if (NULL.equals(value)) {
                    contains = true;
                }
            }
            return contains;
        } catch (final BonitaHomeNotSetException e) {
            throw new SParameterProcessNotFoundException(e);
        } catch (final TenantIdNotSetException e) {
            throw new SParameterProcessNotFoundException(e);
        } catch (final IOException e) {
            throw new SParameterProcessNotFoundException(e);
        }
    }

    @Override
    public SParameter get(final long processDefinitionId, final String parameterName) throws SParameterProcessNotFoundException,
            SParameterProcessNotFoundException {
        try {
            final String filePath = getFilePath(processDefinitionId);
            final Properties properties = PropertiesManager.getPropertiesFromFile(filePath);
            final String property = properties.getProperty(parameterName);
            if (property == null) {
                throw new SParameterProcessNotFoundException(parameterName);
            } else if (NULL.equals(property)) {
                return new SParameterImpl(parameterName, null);
            } else {
                return new SParameterImpl(parameterName, property);
            }
        } catch (final BonitaHomeNotSetException e) {
            throw new SParameterProcessNotFoundException(e);
        } catch (final TenantIdNotSetException e) {
            throw new SParameterProcessNotFoundException(e);
        } catch (final IOException e) {
            throw new SParameterProcessNotFoundException(e);
        }
    }

    @Override
    public List<SParameter> get(final long processDefinitionId, final int fromIndex, final int numberOfResult, final OrderBy order)
            throws SParameterProcessNotFoundException, SOutOfBoundException {
        try {
            final String filePath = getFilePath(processDefinitionId);
            final List<SParameter> orderedParameters = getOrderedParameters(filePath, order);

            final int numberOfParameters = orderedParameters.size();
            if (fromIndex != 0 && numberOfParameters <= fromIndex) {
                throw new SOutOfBoundException("Index " + fromIndex + " >= " + numberOfParameters);
            }
            final int maxIndex = fromIndex + numberOfResult > numberOfParameters ? numberOfParameters : fromIndex + numberOfResult;
            final List<SParameter> parameters = new ArrayList<SParameter>();
            for (int i = fromIndex; i < maxIndex; i++) {
                final SParameter parameterDef = orderedParameters.get(i);
                parameters.add(parameterDef);
            }
            return parameters;
        } catch (final BonitaHomeNotSetException e) {
            throw new SParameterProcessNotFoundException(e);
        } catch (final TenantIdNotSetException e) {
            throw new SParameterProcessNotFoundException(e);
        } catch (final IOException e) {
            throw new SParameterProcessNotFoundException(e);
        }
    }

    @Override
    public List<SParameter> getNullValues(final long processDefinitionId, final int fromIndex, final int numberOfResult, final OrderBy order)
            throws SParameterProcessNotFoundException, SOutOfBoundException {
        // TODO Auto-generated method stub
        return null;
    }

}
