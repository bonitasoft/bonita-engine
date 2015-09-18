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

import org.bonitasoft.engine.cache.CacheService;
import org.bonitasoft.engine.cache.SCacheException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.home.BonitaHomeServer;
import org.bonitasoft.engine.parameter.OrderBy;
import org.bonitasoft.engine.parameter.ParameterService;
import org.bonitasoft.engine.parameter.SParameter;
import org.bonitasoft.engine.parameter.SParameterNameNotFoundException;
import org.bonitasoft.engine.parameter.SParameterProcessNotFoundException;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.bonitasoft.engine.sessionaccessor.STenantIdNotSetException;

/**
 * @author Matthieu Chaffotte
 * @author Zhao Na
 * @author Celine Souchet
 */
public class PropertyFileParameterService implements ParameterService {

    static final String NULL = "-==NULLL==-";

    private static final String CACHE_NAME = "parameters";

    private final ReadSessionAccessor sessionAccessor;

    private final CacheService cacheService;

    public PropertyFileParameterService(final ReadSessionAccessor sessionAccessor, final CacheService cacheService) {
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
        } catch (final BonitaHomeNotSetException | IOException | SCacheException | STenantIdNotSetException e) {
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
        } catch (final BonitaHomeNotSetException | IOException | SCacheException | STenantIdNotSetException e) {
            throw new SParameterProcessNotFoundException(e);
        }
    }

    private void saveProperties(final Properties properties, final long tenantId, final long processId) throws IOException, SCacheException, BonitaHomeNotSetException {
        cacheService.store(CACHE_NAME, getCacheKey(tenantId, processId), properties);
        BonitaHomeServer.getInstance().getProcessManager().storeParameters(tenantId, processId, properties);
    }

    @Override
    public void deleteAll(final long processDefinitionId) throws SParameterProcessNotFoundException {
        try {
            final long tenantId = sessionAccessor.getTenantId();
            if (!BonitaHomeServer.getInstance().getProcessManager().hasParameters(tenantId, processDefinitionId)) {
                throw new SParameterProcessNotFoundException("The process definition " + processDefinitionId + " does not exist");
            }
            final boolean isDeleted = BonitaHomeServer.getInstance().getProcessManager().deleteParameters(tenantId, processDefinitionId);
            if (!isDeleted) {
                throw new SParameterProcessNotFoundException("The property file was not deleted properly");
            }
        } catch (final BonitaHomeNotSetException | STenantIdNotSetException | IOException e) {
            throw new SParameterProcessNotFoundException(e);
        }
    }

    private List<SParameter> getListProperties(final Properties properties, final boolean onlyNulls) throws IOException, SCacheException {
        final List<SParameter> parameters = new ArrayList<SParameter>();
        for (final Entry<Object, Object> property : properties.entrySet()) {
            String value = (String) property.getValue();
            if (NULL.equals(value)) {
                value = null;
            }
            if (!onlyNulls || value == null) {
                parameters.add(new SParameterImpl(property.getKey().toString(), value));
            }
        }
        return parameters;
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
        } catch (final BonitaHomeNotSetException | STenantIdNotSetException | SCacheException | IOException e) {
            throw new SParameterProcessNotFoundException(e);
        }
    }

    @Override
    public SParameter get(final long processDefinitionId, final String parameterName) throws
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
        } catch (final BonitaHomeNotSetException | STenantIdNotSetException | IOException | SCacheException e) {
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
            properties = BonitaHomeServer.getInstance().getProcessManager().getParameters(tenantId, processId);
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
        } catch (final BonitaHomeNotSetException | STenantIdNotSetException | SCacheException | IOException e) {
            throw new SParameterProcessNotFoundException(e);
        }
    }

}
