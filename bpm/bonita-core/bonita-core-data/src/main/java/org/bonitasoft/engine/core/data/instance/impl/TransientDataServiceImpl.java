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
package org.bonitasoft.engine.core.data.instance.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.apache.commons.lang3.text.WordUtils;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.cache.CacheService;
import org.bonitasoft.engine.cache.SCacheException;
import org.bonitasoft.engine.commons.ClassReflector;
import org.bonitasoft.engine.commons.exceptions.SReflectException;
import org.bonitasoft.engine.core.data.instance.TransientDataService;
import org.bonitasoft.engine.core.expression.control.api.ExpressionResolverService;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionNotFoundException;
import org.bonitasoft.engine.core.process.definition.model.SActivityDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.api.FlowNodeInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeReadException;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.data.definition.model.SDataDefinition;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.data.instance.exception.SCreateDataInstanceException;
import org.bonitasoft.engine.data.instance.exception.SDataInstanceException;
import org.bonitasoft.engine.data.instance.exception.SDataInstanceNotFoundException;
import org.bonitasoft.engine.data.instance.exception.SDataInstanceReadException;
import org.bonitasoft.engine.data.instance.exception.SUpdateDataInstanceException;
import org.bonitasoft.engine.data.instance.model.SDataInstance;
import org.bonitasoft.engine.data.instance.model.builder.SDataInstanceBuilderFactory;
import org.bonitasoft.engine.data.instance.model.exceptions.SDataInstanceNotWellFormedException;
import org.bonitasoft.engine.expression.exception.SExpressionException;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

/**
 * @author Baptiste Mesta
 */
public class TransientDataServiceImpl implements TransientDataService {

    static final String TRANSIENT_DATA_CACHE_NAME = "transient_data";

    private final CacheService cacheService;
    private final ExpressionResolverService expressionResolverService;
    private final TechnicalLoggerService logger;
    private final FlowNodeInstanceService flowNodeInstanceService;
    private final ProcessDefinitionService processDefinitionService;

    public TransientDataServiceImpl(final CacheService cacheService,
                                    ExpressionResolverService expressionResolverService,
                                    FlowNodeInstanceService flowNodeInstanceService,
                                    ProcessDefinitionService processDefinitionService,
                                    TechnicalLoggerService logger) {
        this.cacheService = cacheService;
        this.expressionResolverService = expressionResolverService;
        this.flowNodeInstanceService = flowNodeInstanceService;
        this.processDefinitionService = processDefinitionService;
        this.logger = logger;
    }

    @Override
    public List<SDataInstance> getDataInstances(final List<String> dataNames, final long containerId,
                                                final String containerType) throws SDataInstanceException {
        final ArrayList<SDataInstance> data = new ArrayList<SDataInstance>(dataNames.size());
        for (final String dataName : dataNames) {
            data.add(getDataInstance(dataName, containerId, containerType));
        }
        return data;
    }

    private static String getKey(final String dataInstanceName, final long containerId, final String containerType) {
        return dataInstanceName + ":" + containerId + ":" + containerType;
    }

    static String getKey(final SDataInstance dataInstance) {
        return getKey(dataInstance.getName(), dataInstance.getContainerId(), dataInstance.getContainerType());
    }

    @Override
    public void createDataInstance(final SDataInstance dataInstance) throws SDataInstanceException {
        try {
            final String dataInstanceKey = getKey(dataInstance);
            if (checkDataAlreadyExists(dataInstanceKey)) {
                throw new SCreateDataInstanceException(String.format(
                        "Data already exists: name,  container type = %s, containerId = %s", dataInstance.getName(),
                        dataInstance.getContainerType(), dataInstance.getContainerId()));
            }
            setId(dataInstance);
            cacheService.store(TRANSIENT_DATA_CACHE_NAME, dataInstanceKey, dataInstance);
        } catch (final Exception e) {
            throw new SDataInstanceException("Impossible to store transient data", e);
        }
    }

    private void setId(final SDataInstance dataInstance)
            throws SecurityException, IllegalArgumentException, SReflectException {
        // FIXME: probably the id will be be used, so not necessary to be set
        final long id = Math.abs(UUID.randomUUID().getMostSignificantBits());
        ClassReflector.invokeSetter(dataInstance, "setId", long.class, id);
    }

    private boolean checkDataAlreadyExists(final String dataInstanceKey) throws SCacheException {
        final List<?> keys = getCacheKeys(TRANSIENT_DATA_CACHE_NAME);
        return keys.contains(dataInstanceKey);
    }

    @Override
    public void updateDataInstance(final SDataInstance dataInstance, final EntityUpdateDescriptor descriptor)
            throws SDataInstanceException {
        try {
            final String key = getKey(dataInstance);

            for (final Map.Entry<String, Object> field : descriptor.getFields().entrySet()) {
                try {
                    final String setterName = "set" + WordUtils.capitalize(field.getKey());
                    ClassReflector.invokeMethodByName(dataInstance, setterName, field.getValue());
                } catch (final Exception e) {
                    throw new SUpdateDataInstanceException("Problem while updating entity: " + dataInstance
                            + " with id: " + dataInstance.getId() + " in TransientDataInstanceDataSource.", e);
                }
            }
            cacheService.store(TRANSIENT_DATA_CACHE_NAME, key, dataInstance);
        } catch (final SCacheException e) {
            throw new SDataInstanceException("Impossible to update transient data", e);
        }
    }

    @Override
    public void deleteDataInstance(final SDataInstance dataInstance) throws SDataInstanceException {
        try {
            final String key = getKey(dataInstance);
            cacheService.remove(TRANSIENT_DATA_CACHE_NAME, key);
        } catch (final SCacheException e) {
            throw new SDataInstanceException("Impossible to delete transient data", e);
        }
    }

    @Override
    public SDataInstance getDataInstance(final long dataInstanceId) throws SDataInstanceException {
        try {
            final List<?> cacheKeys = getCacheKeys(TRANSIENT_DATA_CACHE_NAME);
            for (final Object key : cacheKeys) {
                final SDataInstance dataInstance = (SDataInstance) cacheService.get(TRANSIENT_DATA_CACHE_NAME, key);
                if (dataInstance != null && dataInstance.getId() == dataInstanceId) {
                    return dataInstance;
                }
            }
        } catch (final SCacheException e) {
            throw new SDataInstanceException("Impossible to get transient data: ", e);
        }
        throw new SDataInstanceNotFoundException("No data found. Id: " + dataInstanceId);
    }

    @Override
    public SDataInstance getDataInstance(final String dataName, final long containerId, final String containerType)
            throws SDataInstanceException {
        try {
            final List<?> cacheKeys = getCacheKeys(TRANSIENT_DATA_CACHE_NAME);
            final String key = getKey(dataName, containerId, containerType);

            if (!cacheKeys.contains(key)) {
                reevaluateTransientData(dataName, containerId, containerType);
            }

            return (SDataInstance) cacheService.get(TRANSIENT_DATA_CACHE_NAME, key);
        } catch (final SCacheException | SProcessDefinitionNotFoundException | SBonitaReadException
                | SFlowNodeNotFoundException | SFlowNodeReadException | SExpressionException e) {
            throw new SDataInstanceException("Impossible to get transient data: ", e);
        }
    }

    private List<?> getCacheKeys(final String cacheName) throws SCacheException {
        List<?> cacheKeys = Collections.emptyList();
        if (cacheService.getCachesNames().contains(cacheName)) {
            cacheKeys = cacheService.getKeys(cacheName);
        }
        return cacheKeys;
    }

    private void reevaluateTransientData(final String name, final long containerId, final String containerType)
            throws SProcessDefinitionNotFoundException, SBonitaReadException, SFlowNodeNotFoundException,
            SFlowNodeReadException, SDataInstanceException, SExpressionException {

        SFlowNodeInstance flowNodeInstance = flowNodeInstanceService.getFlowNodeInstance(containerId);
        final long flowNodeDefinitionId = flowNodeInstance.getFlowNodeDefinitionId();
        final long processDefinitionId = flowNodeInstance.getProcessDefinitionId();
        final SProcessDefinition processDefinition = processDefinitionService.getProcessDefinition(processDefinitionId);
        final SActivityDefinition flowNode = (SActivityDefinition) processDefinition.getProcessContainer()
                .getFlowNode(flowNodeDefinitionId);
        final List<SDataDefinition> sDataDefinitions = flowNode.getSDataDefinitions();
        SDataDefinition dataDefinition = sDataDefinitions.stream().filter(data -> Objects.equals(name, data.getName()))
                .findFirst()
                .orElseThrow(() -> new SDataInstanceNotFoundException(
                        "Transient data was not found and we were unable to reevaluate it because it was not found in the definition, name=<"
                                + name + "> process definition=<" + processDefinition.getName() + ","
                                + processDefinition.getVersion() + "> flow node=<" + flowNode.getName() + ">"));
        createDataInstance(dataDefinition, containerId, DataInstanceContainer.ACTIVITY_INSTANCE,
                new SExpressionContext(containerId, containerType, processDefinitionId));
    }

    private void createDataInstance(SDataDefinition dataDefinition, final long containerId,
                                    final DataInstanceContainer containerType, final SExpressionContext expressionContext)
            throws SDataInstanceException, SExpressionException {
        Serializable dataValue = null;
        final SExpression defaultValueExpression = dataDefinition.getDefaultValueExpression();
        if (defaultValueExpression != null) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.WARNING)) {
                logger.log(getClass(), TechnicalLogSeverity.WARNING,
                        String.format("The value of the transient data %s of %s %s is reevaluated from its default value expression.", dataDefinition.getName(), containerId, containerType));
            }
            dataValue = (Serializable) expressionResolverService.evaluate(dataDefinition.getDefaultValueExpression(),
                    expressionContext);
        } else {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.WARNING)) {
                logger.log(this.getClass(), TechnicalLogSeverity.WARNING,
                        "Creating a transient data instance with a null expression is not a good practice.");
            }
        }

        try {
            createDataInstance(BuilderFactory.get(SDataInstanceBuilderFactory.class)
                    .createNewInstance(dataDefinition)
                    .setContainerId(containerId)
                    .setContainerType(containerType.name())
                    .setValue(dataValue)
                    .done());
        } catch (final SDataInstanceNotWellFormedException e) {
            throw new SDataInstanceReadException(e);
        }
    }

    @Override
    public List<SDataInstance> getDataInstances(final long containerId, final String containerType, final int fromIndex,
                                                final int numberOfResults) throws SDataInstanceException {
        final String matchingKey = containerId + ":" + containerType;
        final List<SDataInstance> dataInstances = new ArrayList<SDataInstance>();
        try {
            final List<?> cacheKeys = getCacheKeys(TRANSIENT_DATA_CACHE_NAME);
            for (int i = fromIndex; i < cacheKeys.size() && i < numberOfResults; i++) {
                final Object key = cacheKeys.get(i);
                if (((String) key).contains(matchingKey)) {
                    final SDataInstance dataInstance = getDataInstance(
                            ((String) key).substring(0, ((String) key).indexOf(":")), containerId, containerType);
                    if (dataInstance != null) {
                        dataInstances.add(dataInstance);
                    }
                }
            }

            if (!dataInstances.isEmpty()) {
                return dataInstances;
            }
            return Collections.emptyList();
            // throw new SDataInstanceException("No data instance found for container type "
            // + containerType + " and container id " + containerId);
        } catch (final SCacheException e) {
            throw new SDataInstanceException("Impossible to get transient data: ", e);
        }
    }

    @Override
    public List<SDataInstance> getDataInstances(final List<Long> dataInstanceIds) {
        final List<SDataInstance> results = new ArrayList<SDataInstance>(dataInstanceIds.size());
        for (final Long dataInstanceId : dataInstanceIds) {
            try {
                results.add(getDataInstance(dataInstanceId));
            } catch (final SDataInstanceException e) {
            }
        }
        return results;
    }

}
