/*******************************************************************************
 * Copyright (C) 2009, 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.parameter.propertyfile;

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

import org.bonitasoft.engine.cache.PlatformCacheService;
import org.bonitasoft.engine.cache.SCacheException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.home.BonitaHomeServer;
import org.bonitasoft.engine.io.PropertiesManager;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.bonitasoft.engine.sessionaccessor.STenantIdNotSetException;

import com.bonitasoft.engine.parameter.OrderBy;
import com.bonitasoft.engine.parameter.ParameterService;
import com.bonitasoft.engine.parameter.SParameter;
import com.bonitasoft.engine.parameter.SParameterNameNotFoundException;
import com.bonitasoft.engine.parameter.SParameterProcessNotFoundException;

/**
 * @author Matthieu Chaffotte
 * @author Zhao Na
 * @author Celine Souchet
 */
public class PropertyFileParameterService implements ParameterService {

    private static final String PARAMETERS_PROPERTIES = "current-parameters.properties";

    private static final String NULL = "-==NULLL==-";

    private static final String CACHE_NAME = "parameters";

    private final ReadSessionAccessor sessionAccessor;

    private final PlatformCacheService cacheService;

    public PropertyFileParameterService(final ReadSessionAccessor sessionAccessor, final PlatformCacheService cacheService) {
        this.sessionAccessor = sessionAccessor;
        this.cacheService = cacheService;
    }

    @Override
    public void update(final long processDefinitionId, final String parameterName, final String parameterValue) throws SParameterProcessNotFoundException,
            SParameterNameNotFoundException {
        try {
            final String filePath = getFilePath(processDefinitionId);
            final Properties properties = getProperties(filePath);
            if (!properties.containsKey(parameterName)) {
                throw new SParameterNameNotFoundException("The parameter name " + parameterName + " does not exist");
            }
            final String newValue = parameterValue == null ? NULL : parameterValue;
            putProperty(filePath, parameterName, newValue);
        } catch (final BonitaHomeNotSetException e) {
            throw new SParameterProcessNotFoundException(e);
        } catch (final IOException e) {
            throw new SParameterProcessNotFoundException(e);
        } catch (final STenantIdNotSetException e) {
            throw new SParameterProcessNotFoundException(e);
        } catch (final SCacheException e) {
            throw new SParameterProcessNotFoundException(e);
        }
    }

    @Override
    public void addAll(final long processDefinitionId, final Map<String, String> parameters) throws SParameterProcessNotFoundException {
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
            saveProperties(properties, file.getAbsolutePath());
        } catch (final BonitaHomeNotSetException e) {
            throw new SParameterProcessNotFoundException(e);
        } catch (final IOException e) {
            throw new SParameterProcessNotFoundException(e);
        } catch (final STenantIdNotSetException e) {
            throw new SParameterProcessNotFoundException(e);
        } catch (final SCacheException e) {
            throw new SParameterProcessNotFoundException(e);
        }
    }

    private void saveProperties(final Properties properties, final String filePath) throws IOException, SCacheException {
        cacheService.store(CACHE_NAME, filePath, properties);
        PropertiesManager.saveProperties(properties, filePath);
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
        } catch (final STenantIdNotSetException e) {
            throw new SParameterProcessNotFoundException(e);
        }
    }

    private String getFilePathWithoutChecking(final long processDefinitionId) throws BonitaHomeNotSetException, STenantIdNotSetException {
        final long tenantId = sessionAccessor.getTenantId();
        final String processesFolder = BonitaHomeServer.getInstance().getProcessesFolder(tenantId);
        final StringBuilder pathBuilder = new StringBuilder(processesFolder);
        pathBuilder.append(File.separatorChar).append(processDefinitionId);
        pathBuilder.append(File.separatorChar).append(PARAMETERS_PROPERTIES);
        return pathBuilder.toString();
    }

    private String getFilePath(final long processDefinitionId) throws BonitaHomeNotSetException, STenantIdNotSetException, SParameterProcessNotFoundException {
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

    private List<SParameter> getListProperties(final String fileName, final boolean onlyNulls) throws IOException, SCacheException {
        final Properties properties = getProperties(fileName);
        final List<SParameter> paramters = new ArrayList<SParameter>();
        for (final Entry<Object, Object> property : properties.entrySet()) {
            String value = (String) property.getValue();
            if (NULL.equals(value)) {
                value = null;
            }
            if (!onlyNulls) {
                paramters.add(new SParameterImpl(property.getKey().toString(), value));
            } else if (value == null) {
                paramters.add(new SParameterImpl(property.getKey().toString(), value));
            }
        }
        return paramters;
    }

    private List<SParameter> getOrderedParameters(final String filePath, final OrderBy order, final boolean onlyNulls) throws IOException, SCacheException {
        final Comparator<SParameter> sorting;
        switch (order) {
            case NAME_DESC:
                sorting = new NameDescComparator(NULL);
                break;
            default:
                sorting = new NameAscComparator(NULL);
                break;
        }
        final List<SParameter> parameters = getListProperties(filePath, onlyNulls);
        final List<SParameter> sortedList = new ArrayList<SParameter>(parameters);
        Collections.sort(sortedList, sorting);
        return sortedList;
    }

    private synchronized void putProperty(final String filePath, final String key, final String value) throws IOException, SCacheException {
        final Properties properties = getProperties(filePath);
        properties.put(key, value);
        saveProperties(properties, filePath);
    }

    @Override
    public boolean containsNullValues(final long processDefinitionId) throws SParameterProcessNotFoundException {
        String filePath;
        try {
            filePath = getFilePath(processDefinitionId);
            final Properties properties = getProperties(filePath);
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
        } catch (final STenantIdNotSetException e) {
            throw new SParameterProcessNotFoundException(e);
        } catch (final IOException e) {
            throw new SParameterProcessNotFoundException(e);
        } catch (final SCacheException e) {
            throw new SParameterProcessNotFoundException(e);
        }
    }

    @Override
    public SParameter get(final long processDefinitionId, final String parameterName) throws SParameterProcessNotFoundException,
            SParameterProcessNotFoundException {
        try {
            final String filePath = getFilePath(processDefinitionId);
            final Properties properties = getProperties(filePath);
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
        } catch (final STenantIdNotSetException e) {
            throw new SParameterProcessNotFoundException(e);
        } catch (final IOException e) {
            throw new SParameterProcessNotFoundException(e);
        } catch (final SCacheException e) {
            throw new SParameterProcessNotFoundException(e);
        }
    }

    private Properties getProperties(final String filePath) throws IOException, SCacheException {
        final Object object = cacheService.get(CACHE_NAME, filePath);
        Properties properties;
        if (object != null) {
            properties = (Properties) object;
        } else {
            properties = PropertiesManager.getProperties(filePath);
            cacheService.store(CACHE_NAME, filePath, properties);
        }
        return properties;

    }

    @Override
    public List<SParameter> get(final long processDefinitionId, final int fromIndex, final int numberOfResult, final OrderBy order)
            throws SParameterProcessNotFoundException {
        return getParameters(processDefinitionId, fromIndex, numberOfResult, order, false);
    }

    @Override
    public List<SParameter> getNullValues(final long processDefinitionId, final int fromIndex, final int numberOfResult, final OrderBy order)
            throws SParameterProcessNotFoundException {
        return getParameters(processDefinitionId, fromIndex, numberOfResult, order, true);
    }

    private List<SParameter> getParameters(final long processDefinitionId, final int fromIndex, final int numberOfResult, final OrderBy order,
            final boolean onlyNulls) throws SParameterProcessNotFoundException {
        try {
            final String filePath = getFilePath(processDefinitionId);
            final List<SParameter> orderedParameters = getOrderedParameters(filePath, order, onlyNulls);

            final int numberOfParameters = orderedParameters.size();
            if (fromIndex != 0 && numberOfParameters <= fromIndex) {
                return Collections.emptyList();
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
        } catch (final STenantIdNotSetException e) {
            throw new SParameterProcessNotFoundException(e);
        } catch (final IOException e) {
            throw new SParameterProcessNotFoundException(e);
        } catch (final SCacheException e) {
            throw new SParameterProcessNotFoundException(e);
        }
    }

}
