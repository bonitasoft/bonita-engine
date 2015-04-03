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
 */
package org.bonitasoft.engine.core.form.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SDeletionException;
import org.bonitasoft.engine.commons.exceptions.SObjectCreationException;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.core.form.FormMappingKeyGenerator;
import org.bonitasoft.engine.core.form.FormMappingService;
import org.bonitasoft.engine.core.form.SFormMapping;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.events.model.SUpdateEvent;
import org.bonitasoft.engine.events.model.builders.SEventBuilderFactory;
import org.bonitasoft.engine.page.PageMappingService;
import org.bonitasoft.engine.page.PageService;
import org.bonitasoft.engine.page.SPage;
import org.bonitasoft.engine.page.SPageMapping;
import org.bonitasoft.engine.page.URLAdapter;
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
    public static final String LEGACY_URL_ADAPTER = "legacy";
    public static final String EXTERNAL_URL_ADAPTER = "external";

    private final Recorder recorder;
    private final ReadPersistenceService persistenceService;
    private final SessionService sessionService;
    private final ReadSessionAccessor sessionAccessor;
    private final PageMappingService pageMappingService;
    private final PageService pageService;
    private final FormMappingKeyGenerator formMappingKeyGenerator;

    public FormMappingServiceImpl(Recorder recorder, ReadPersistenceService persistenceService, SessionService sessionService,
                                  ReadSessionAccessor sessionAccessor, PageMappingService pageMappingService, PageService pageService, FormMappingKeyGenerator formMappingKeyGenerator, List<URLAdapter> urlAdapters) {
        this.recorder = recorder;
        this.persistenceService = persistenceService;
        this.sessionService = sessionService;
        this.sessionAccessor = sessionAccessor;
        this.pageMappingService = pageMappingService;
        this.pageService = pageService;
        this.formMappingKeyGenerator = formMappingKeyGenerator;
        for (URLAdapter urlAdapter : urlAdapters) {
            this.pageMappingService.addUrlAdapter(urlAdapter);
        }
    }

    @Override
    public SFormMapping create(long processDefinitionId, String task, Integer type, String target, String form) throws SBonitaReadException,
            SObjectCreationException {
        SPageMapping sPageMapping;
        String key = formMappingKeyGenerator.generateKey(processDefinitionId, task, type);
        if (target == null) {
            sPageMapping = pageMappingService.create(key, null);
        } else {
            switch (target) {
                case SFormMapping.TARGET_INTERNAL:
                    sPageMapping = pageMappingService.create(key, getPageIdOrNull(form));
                    break;
                case SFormMapping.TARGET_URL:
                    sPageMapping = pageMappingService.create(key, form, EXTERNAL_URL_ADAPTER);
                    break;
                case SFormMapping.TARGET_LEGACY:
                    sPageMapping = pageMappingService.create(key, null, LEGACY_URL_ADAPTER);
                    break;
                default:
                    throw new IllegalArgumentException("Illegal form target " + target);

            }
        }
        SFormMappingImpl sFormMapping = new SFormMappingImpl(processDefinitionId, type, task);
        insertFormMapping(sFormMapping, sPageMapping);
        return sFormMapping;
    }

    Long getPageIdOrNull(String form) throws SBonitaReadException {
        SPage pageByName = pageService.getPageByName(form);
        return pageByName == null ? null : pageByName.getId();
    }

    private void insertFormMapping(SFormMappingImpl sFormMapping, SPageMapping sPageMapping) throws SObjectCreationException {
        InsertRecord record = new InsertRecord(sFormMapping);
        sFormMapping.setPageMapping(sPageMapping);
        final SInsertEvent insertEvent = (SInsertEvent) BuilderFactory.get(SEventBuilderFactory.class).createInsertEvent(FORM_MAPPING)
                .setObject(sFormMapping)
                .done();
        try {
            recorder.recordInsert(record, insertEvent);
        } catch (SRecorderException e) {
            throw new SObjectCreationException(e);
        }
    }

    @Override
    public void update(SFormMapping formMapping, String url, Long pageId) throws SObjectModificationException {
        String urlAdapter = null;
        final SUpdateEvent updateEvent = (SUpdateEvent) BuilderFactory.get(SEventBuilderFactory.class).createUpdateEvent(FORM_MAPPING).setObject(formMapping)
                .done();
        if (!((url != null) ^ (pageId != null))) {
            throw new SObjectModificationException("Can't update the form mapping with both url and pageId");
        }
        if (url != null) {
            if (url.isEmpty()) {
                throw new SObjectModificationException("Can't have an empty url");
            }
            urlAdapter = EXTERNAL_URL_ADAPTER;
        }
        if (pageId != null) {
            try {
                pageService.getPage(pageId);
            } catch (SBonitaReadException | SObjectNotFoundException e) {
                throw new SObjectModificationException("the page with id " + pageId + " does not exists");
            }
        }
        try {
            EntityUpdateDescriptor entityUpdateDescriptor = new EntityUpdateDescriptor();
            entityUpdateDescriptor.addField("pageMapping.url", url);
            entityUpdateDescriptor.addField("pageMapping.urlAdapter", urlAdapter);
            entityUpdateDescriptor.addField("pageMapping.pageId", pageId);
            entityUpdateDescriptor.addField("lastUpdatedBy", getSessionUserId());
            entityUpdateDescriptor.addField("lastUpdateDate", System.currentTimeMillis());
            final UpdateRecord updateRecord = UpdateRecord.buildSetFields(formMapping, entityUpdateDescriptor);
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
        final SDeleteEvent deleteEvent = (SDeleteEvent) BuilderFactory.get(SEventBuilderFactory.class).createDeleteEvent(FORM_MAPPING).setObject(formMapping)
                .done();
        try {
            recorder.recordDelete(new DeleteRecord(formMapping), deleteEvent);
            pageMappingService.delete(formMapping.getPageMapping());
        } catch (SRecorderException | SDeletionException e) {
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
    public SFormMapping get(long processDefinitionId, Integer type, String task) throws SBonitaReadException {
        Map<String, Object> parameters = new HashMap<String, Object>(3);
        parameters.put("processDefinitionId", processDefinitionId);
        parameters.put("type", type);
        parameters.put("task", task);
        return persistenceService.selectOne(new SelectOneDescriptor<SFormMapping>("getFormMappingOfProcessDefinitionOnTask", parameters, SFormMapping.class));
    }

    @Override
    public SFormMapping get(long processDefinitionId, Integer type) throws SBonitaReadException {
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
