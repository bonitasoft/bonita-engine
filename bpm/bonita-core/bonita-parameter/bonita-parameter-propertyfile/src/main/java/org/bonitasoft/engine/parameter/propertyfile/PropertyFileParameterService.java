/*******************************************************************************
 * Copyright (C) 2009, 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package org.bonitasoft.engine.parameter.propertyfile;

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
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.bonitasoft.engine.sessionaccessor.STenantIdNotSetException;

import org.bonitasoft.engine.parameter.OrderBy;
import org.bonitasoft.engine.parameter.ParameterService;
import org.bonitasoft.engine.parameter.SParameter;
import org.bonitasoft.engine.parameter.SParameterNameNotFoundException;
import org.bonitasoft.engine.parameter.SParameterProcessNotFoundException;

/**
 * @author Matthieu Chaffotte
 * @author Zhao Na
 * @author Celine Souchet
 */
public class PropertyFileParameterService implements ParameterService {

    static final String NULL = "-==NULLL==-";

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
            final long tenantId = sessionAccessor.getTenantId();
            final Properties properties = getProperties(tenantId, processDefinitionId);
            if (!properties.containsKey(parameterName)) {
                throw new SParameterNameNotFoundException("The parameter name " + parameterName + " does not exist");
            }
            final String newValue = parameterValue == null ? NULL : parameterValue;
            putProperty(tenantId, processDefinitionId, parameterName, newValue);
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
            final Properties properties = new Properties();
            if (parameters != null) {
                for (final Entry<String, String> parameter : parameters.entrySet()) {
                    final String value = parameter.getValue() == null ? NULL : parameter.getValue();
                    properties.put(parameter.getKey(), value);
                }
            }
            saveProperties(properties, sessionAccessor.getTenantId(), processDefinitionId);
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

    private void saveProperties(final Properties properties, final long tenantId, final long processId) throws IOException, SCacheException, BonitaHomeNotSetException {
        cacheService.store(CACHE_NAME, getCacheKey(tenantId, processId), properties);
        BonitaHomeServer.getInstance().storeParameters(tenantId, processId, properties);
    }

    @Override
    public void deleteAll(final long processDefinitionId) throws SParameterProcessNotFoundException {
        try {
            final long tenantId = sessionAccessor.getTenantId();
            if (!BonitaHomeServer.getInstance().hasParameters(tenantId, processDefinitionId)) {
                final StringBuilder errorBuilder = new StringBuilder();
                errorBuilder.append("The process definition ").append(processDefinitionId).append(" does not exist");
                throw new SParameterProcessNotFoundException(errorBuilder.toString());
            }
            final boolean isDeleted = BonitaHomeServer.getInstance().deleteParameters(tenantId, processDefinitionId);
            if (!isDeleted) {
                throw new SParameterProcessNotFoundException("The property file was not deleted propertly");
            }
        } catch (final BonitaHomeNotSetException e) {
            throw new SParameterProcessNotFoundException(e);
        } catch (final STenantIdNotSetException e) {
            throw new SParameterProcessNotFoundException(e);
        } catch (IOException e) {
            throw new SParameterProcessNotFoundException(e);
        }
    }

    private List<SParameter> getListProperties(final Properties properties, final boolean onlyNulls) throws IOException, SCacheException {
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

    private List<SParameter> getOrderedParameters(final Properties properties, final OrderBy order, final boolean onlyNulls) throws IOException, SCacheException {
        final Comparator<SParameter> sorting;
        switch (order) {
            case NAME_DESC:
                sorting = new NameDescComparator(NULL);
                break;
            default:
                sorting = new NameAscComparator(NULL);
                break;
        }
        final List<SParameter> parameters = getListProperties(properties, onlyNulls);
        final List<SParameter> sortedList = new ArrayList<SParameter>(parameters);
        Collections.sort(sortedList, sorting);
        return sortedList;
    }

    private synchronized void putProperty(final long tenantId, final long processId, final String key, final String value) throws IOException, SCacheException, BonitaHomeNotSetException {
        final Properties properties = getProperties(tenantId, processId);
        properties.put(key, value);
        saveProperties(properties, tenantId, processId);
    }

    @Override
    public boolean containsNullValues(final long processDefinitionId) throws SParameterProcessNotFoundException {
        try {
            final Properties properties = getProperties(sessionAccessor.getTenantId(), processDefinitionId);
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
            final long tenantId = sessionAccessor.getTenantId();
            final Properties properties = getProperties(tenantId, processDefinitionId);
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
        } catch (final SCacheException e) {
            throw new SParameterProcessNotFoundException(e);
        } catch (IOException e) {
            throw new SParameterProcessNotFoundException(e);
        }
    }

    private String getCacheKey(final long tenantId, final long processId) {
        return Long.toString(tenantId) + "$$" + Long.toString(processId);
    }

    private Properties getProperties(final long tenantId, final long processId) throws BonitaHomeNotSetException, SCacheException, IOException {
        final String key = getCacheKey(tenantId, processId);
        final Object object = cacheService.get(CACHE_NAME, key);
        Properties properties;
        if (object != null) {
            properties = (Properties) object;
        } else {
            properties = BonitaHomeServer.getInstance().getParameters(tenantId, processId);
            cacheService.store(CACHE_NAME, key, properties);
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
            final Properties properties = getProperties(sessionAccessor.getTenantId(), processDefinitionId);
            final List<SParameter> orderedParameters = getOrderedParameters(properties, order, onlyNulls);

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
