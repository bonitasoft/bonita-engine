/**
 * Copyright (C) 2015 Bonitasoft S.A.
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

package org.bonitasoft.engine.parameter;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SObjectCreationException;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.events.EventActionType;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.events.model.SUpdateEvent;
import org.bonitasoft.engine.events.model.builders.SEventBuilderFactory;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.recorder.model.UpdateRecord;

/**
 * @author Baptiste Mesta
 */
public class ParameterServiceImpl implements ParameterService {

    public static final int PAGE_SIZE = 100;
    public final String PARAMETER = "PARAMETER";

    private final Recorder recorder;
    private final ReadPersistenceService persistenceService;
    private final EventService eventService;

    public ParameterServiceImpl(Recorder recorder, ReadPersistenceService persistenceService, EventService eventService) {
        this.recorder = recorder;
        this.persistenceService = persistenceService;
        this.eventService = eventService;
    }

    @Override
    public void update(long processDefinitionId, String parameterName, String parameterValue)
            throws SParameterNameNotFoundException, SBonitaReadException, SObjectModificationException {
        final SParameter sParameter = get(processDefinitionId, parameterName);
        if (sParameter == null) {
            throw new SParameterNameNotFoundException("no parameter <" + parameterName + "> found in the process <" + processDefinitionId + ">");
        }
        update(sParameter, parameterValue);
    }

    void update(SParameter sParameter, String parameterValue) throws SObjectModificationException {
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        descriptor.addField("value", parameterValue);
        try { //TODO Business log
            recorder.recordUpdate(UpdateRecord.buildSetFields(sParameter, descriptor), getUpdateEvent(sParameter, PARAMETER));
        } catch (SRecorderException e) {
            throw new SObjectModificationException(e);
        }
    }

    @Override
    public void addAll(long processDefinitionId, Map<String, String> parameters)
            throws SObjectCreationException, SBonitaReadException, SObjectModificationException {
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            addOrUpdate(processDefinitionId, entry.getKey(), entry.getValue());
        }
    }

    void addOrUpdate(long processDefinitionId, String name, String value) throws SObjectCreationException, SBonitaReadException, SObjectModificationException {
        final SParameter currentParameter = get(processDefinitionId, name);
        if (currentParameter != null) {
            update(currentParameter, value);
        } else {
            add(processDefinitionId, name, value);
        }
    }

    private void add(long processDefinitionId, String name, String value) throws SObjectCreationException {
        final SParameterImpl sParameter = new SParameterImpl(name, value, processDefinitionId);
        try {
            recorder.recordInsert(new InsertRecord(sParameter), getInsertEvent(sParameter, PARAMETER));
        } catch (SRecorderException e) {
            throw new SObjectCreationException(e);
        }
    }

    @Override
    public Map<String, String> getAll(long processDefinitionId) throws SParameterProcessNotFoundException, SBonitaReadException {
        Map<String, String> parameters = new HashMap<>();
        int fromIndex = 0;
        List<SParameter> sParameters;
        do {
            sParameters = get(processDefinitionId, fromIndex, PAGE_SIZE, null);
            for (SParameter sParameter : sParameters) {
                parameters.put(sParameter.getName(), sParameter.getValue());
            }
            fromIndex += PAGE_SIZE;

        } while (sParameters.size() == PAGE_SIZE);
        return parameters;
    }

    @Override
    public void deleteAll(long processDefinitionId) throws SParameterProcessNotFoundException, SBonitaReadException, SObjectModificationException {
        List<SParameter> toDelete;
        do {
            toDelete = get(processDefinitionId, 0, PAGE_SIZE, null);
            for (SParameter sParameter : toDelete) {
                try {
                    recorder.recordDelete(new DeleteRecord(sParameter), getDeleteEvent(sParameter, PARAMETER));
                } catch (SRecorderException e) {
                    throw new SObjectModificationException(e);
                }
            }
        } while (toDelete.size() == PAGE_SIZE);
    }

    @Override
    public List<SParameter> get(long processDefinitionId, int fromIndex, int numberOfResult, OrderBy order) throws SBonitaReadException {
        return persistenceService.selectList(
                new SelectListDescriptor<SParameter>("getParameters", Collections.<String, Object> singletonMap("processDefinitionId", processDefinitionId),
                        SParameter.class, new QueryOptions(fromIndex, numberOfResult, getOrderByOptions(order))));
    }

    @Override
    public SParameter get(long processDefinitionId, String parameterName) throws SBonitaReadException {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("processDefinitionId", processDefinitionId);
        parameters.put("name", parameterName);
        return persistenceService.selectOne(new SelectOneDescriptor<SParameter>("getParameterByName", parameters, SParameter.class));
    }

    @Override
    public List<SParameter> getNullValues(long processDefinitionId, int fromIndex, int numberOfResult, OrderBy order)
            throws SParameterProcessNotFoundException, SBonitaReadException {
        return persistenceService.selectList(new SelectListDescriptor<SParameter>("getParametersWithNullValues", Collections.<String, Object> singletonMap(
                "processDefinitionId", processDefinitionId), SParameter.class, new QueryOptions(fromIndex, numberOfResult, getOrderByOptions(order))));
    }

    List<OrderByOption> getOrderByOptions(OrderBy order) {
        OrderByType type = OrderByType.ASC;
        String fieldName = "name";
        if (order != null) {
            switch (order) {
                case NAME_DESC:
                    type = OrderByType.DESC;
                    break;
                case VALUE_ASC:
                    fieldName = "value";
                    break;
                case VALUE_DESC:
                    fieldName = "value";
                    type = OrderByType.DESC;
                    break;
            }
        }
        return Collections.singletonList(new OrderByOption(SParameter.class, fieldName, type));
    }

    @Override
    public boolean containsNullValues(long processDefinitionId) throws SBonitaReadException {
        return !persistenceService.selectList(new SelectListDescriptor<SParameter>("getParametersWithNullValues", Collections.<String, Object> singletonMap(
                "processDefinitionId", processDefinitionId), SParameter.class, new QueryOptions(0, 1000))).isEmpty();
    }

    private SInsertEvent getInsertEvent(final Object object, final String type) {
        if (eventService.hasHandlers(type, EventActionType.CREATED)) {
            return (SInsertEvent) BuilderFactory.get(SEventBuilderFactory.class).createInsertEvent(type).setObject(object).done();
        }
        return null;
    }

    private SDeleteEvent getDeleteEvent(final Object object, final String type) {
        if (eventService.hasHandlers(type, EventActionType.DELETED)) {
            return (SDeleteEvent) BuilderFactory.get(SEventBuilderFactory.class).createDeleteEvent(type).setObject(object).done();
        }
        return null;
    }

    private SUpdateEvent getUpdateEvent(final Object object, final String type) {
        if (eventService.hasHandlers(type, EventActionType.UPDATED)) {
            return (SUpdateEvent) BuilderFactory.get(SEventBuilderFactory.class).createUpdateEvent(type).setObject(object).done();
        }
        return null;
    }
}
