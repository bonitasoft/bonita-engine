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
package org.bonitasoft.engine.core.form.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SObjectCreationException;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.core.form.FormMappingService;
import org.bonitasoft.engine.core.form.SFormMapping;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.events.model.SUpdateEvent;
import org.bonitasoft.engine.events.model.builders.SEventBuilderFactory;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.recorder.model.UpdateRecord;
import org.bonitasoft.engine.session.SSessionNotFoundException;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.bonitasoft.engine.sessionaccessor.SessionIdNotSetException;

/**
 * @author Baptiste Mesta
 */
public class FormMappingServiceImpl implements FormMappingService {

    public static final String FORM_MAPPING = "FORM_MAPPING";
    private Recorder recorder;
    private ReadPersistenceService persistenceService;
    private SessionService sessionService;
    private ReadSessionAccessor sessionAccessor;

    public FormMappingServiceImpl(Recorder recorder, ReadPersistenceService persistenceService, SessionService sessionService,
            ReadSessionAccessor sessionAccessor) {
        this.recorder = recorder;
        this.persistenceService = persistenceService;
        this.sessionService = sessionService;
        this.sessionAccessor = sessionAccessor;
    }

    @Override
    public SFormMapping create(long processDefinitionId, String task, String form, String target, String type) throws SObjectCreationException {
        SFormMappingImpl sFormMapping = new SFormMappingImpl(processDefinitionId, task, form, target, type);
        InsertRecord record = new InsertRecord(sFormMapping);

        final SInsertEvent insertEvent = (SInsertEvent) BuilderFactory.get(SEventBuilderFactory.class)
                .createInsertEvent(FORM_MAPPING)
                .setObject(sFormMapping).done();
        try {
            recorder.recordInsert(record, insertEvent);
        } catch (SRecorderException e) {
            throw new SObjectCreationException(e);
        }
        return sFormMapping;
    }

    @Override
    public void update(SFormMapping formMapping, String form, String target) throws SObjectModificationException {
        final SUpdateEvent updateEvent = (SUpdateEvent) BuilderFactory.get(SEventBuilderFactory.class)
                .createUpdateEvent(FORM_MAPPING)
                .setObject(formMapping).done();
        try {
            EntityUpdateDescriptor entityUpdateDescriptor = new EntityUpdateDescriptor();
            entityUpdateDescriptor.addField("form", form);
            entityUpdateDescriptor.addField("target", target);
            entityUpdateDescriptor.addField("lastUpdatedBy", getSessionUserId());
            entityUpdateDescriptor.addField("lastUpdateDate", System.currentTimeMillis());
            final UpdateRecord updateRecord = UpdateRecord.buildSetFields(formMapping,
                    entityUpdateDescriptor);
            recorder.recordUpdate(updateRecord, updateEvent);
        } catch (SBonitaException e) {
            throw new SObjectModificationException(e);
        }
    }

    private long getSessionUserId() throws SSessionNotFoundException, SessionIdNotSetException {
        return sessionService.getLoggedUserFromSession(sessionAccessor);
    }

    @Override
    public void delete(SFormMapping formMapping) throws SObjectModificationException {
        final SDeleteEvent deleteEvent = (SDeleteEvent) BuilderFactory.get(SEventBuilderFactory.class)
                .createDeleteEvent(FORM_MAPPING)
                .setObject(formMapping).done();
        try {
            recorder.recordDelete(new DeleteRecord(formMapping), deleteEvent);
        } catch (SRecorderException e) {
            throw new SObjectModificationException(e);
        }
    }

    @Override
    public SFormMapping get(long formMappingId) throws SBonitaReadException, SObjectNotFoundException {
        SFormMapping getFormMappingById = persistenceService.selectById(new SelectByIdDescriptor<SFormMapping>("getFormMappingById", SFormMapping.class,
                formMappingId));
        if (getFormMappingById == null) {
            throw new SObjectNotFoundException(formMappingId);
        }
        return getFormMappingById;
    }

    @Override
    public SFormMapping get(long processDefinitionId, String type, String task) throws SBonitaReadException {
        Map<String, Object> parameters = new HashMap<String, Object>(3);
        parameters.put("processDefinitionId", processDefinitionId);
        parameters.put("type", type);
        parameters.put("task", task);
        return persistenceService.selectOne(new SelectOneDescriptor<SFormMapping>("getFormMappingOfProcessDefinitionOnTask", parameters, SFormMapping.class));
    }

    @Override
    public SFormMapping get(long processDefinitionId, String type) throws SBonitaReadException {
        Map<String, Object> parameters = new HashMap<String, Object>(3);
        parameters.put("processDefinitionId", processDefinitionId);
        parameters.put("type", type);
        return persistenceService.selectOne(new SelectOneDescriptor<SFormMapping>("getFormMappingOfProcessDefinition", parameters, SFormMapping.class));
    }

    @Override
    public List<SFormMapping> list(long processDefinitionId, int fromIndex, int numberOfResults) throws SBonitaReadException {
        Map<String, Object> parameters = new HashMap<String, Object>(3);
        parameters.put("processDefinitionId", processDefinitionId);
        return persistenceService.selectList(new SelectListDescriptor<SFormMapping>("getFormMappingsOfProcessDefinition", parameters, SFormMapping.class,
                new QueryOptions(
                        fromIndex, numberOfResults)));
    }

    @Override
    public List<SFormMapping> list(int fromIndex, int numberOfResults) throws SBonitaReadException {
        Map<String, Object> parameters = new HashMap<String, Object>(3);
        return persistenceService.selectList(new SelectListDescriptor<SFormMapping>("getFormMappings", parameters, SFormMapping.class, new QueryOptions(
                fromIndex, numberOfResults)));
    }

    @Override
    public long getNumberOfFormMappings(QueryOptions queryOptions) throws SBonitaReadException {
        return persistenceService.getNumberOfEntities(SFormMapping.class, queryOptions, Collections.<String, Object>emptyMap());
    }

    @Override
    public List<SFormMapping> searchFormMappings(QueryOptions queryOptions) throws SBonitaReadException {
        return persistenceService.searchEntity(SFormMapping.class, queryOptions, Collections.<String, Object>emptyMap());
    }

}
