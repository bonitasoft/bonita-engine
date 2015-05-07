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

package org.bonitasoft.engine.page.impl;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SDeletionException;
import org.bonitasoft.engine.commons.exceptions.SExecutionException;
import org.bonitasoft.engine.commons.exceptions.SObjectCreationException;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.events.model.SUpdateEvent;
import org.bonitasoft.engine.events.model.builders.SEventBuilderFactory;
import org.bonitasoft.engine.page.AuthorizationRule;
import org.bonitasoft.engine.page.PageMappingService;
import org.bonitasoft.engine.page.SAuthorizationException;
import org.bonitasoft.engine.page.SPageMapping;
import org.bonitasoft.engine.page.SPageURL;
import org.bonitasoft.engine.page.URLAdapter;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
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
public class PageMappingServiceImpl implements PageMappingService {

    public static final String PAGE_MAPPING = "PAGE_MAPPING";
    private final Recorder recorder;
    private final ReadPersistenceService persistenceService;
    private final SessionService sessionService;
    private final ReadSessionAccessor sessionAccessor;
    private final Map<String, URLAdapter> urlAdapterMap;
    private final Map<String, AuthorizationRule> authorizationRuleMap;

    public PageMappingServiceImpl(Recorder recorder, ReadPersistenceService persistenceService, SessionService sessionService,
            ReadSessionAccessor sessionAccessor) {
        this.recorder = recorder;
        this.persistenceService = persistenceService;
        this.sessionService = sessionService;
        this.sessionAccessor = sessionAccessor;
        urlAdapterMap = new HashMap<>();
        authorizationRuleMap = new HashMap<>();
    }

    public void setURLAdapters(List<URLAdapter> urlAdapters) {
        for (URLAdapter urlAdapter : urlAdapters) {
            urlAdapterMap.put(urlAdapter.getId(), urlAdapter);
        }
    }

    public void setAuthorizationRules(List<AuthorizationRule> authorizationRules) {
        for (AuthorizationRule authorizationRule : authorizationRules) {
            authorizationRuleMap.put(authorizationRule.getId(), authorizationRule);
        }
    }

    @Override
    public SPageMapping create(String key, Long pageId, List<String> authorizationRules) throws SObjectCreationException {
        SPageMappingImpl entity = new SPageMappingImpl();
        entity.setPageId(pageId);
        entity.setPageAuthorizationRules(authorizationRules);
        entity.setKey(key);
        return insert(entity);
    }

    SPageMapping insert(SPageMappingImpl entity) throws SObjectCreationException {
        InsertRecord record = new InsertRecord(entity);
        try {
            recorder.recordInsert(record, getInsertEvent(entity));
        } catch (SRecorderException e) {
            throw new SObjectCreationException(e);
        }
        return entity;
    }

    @Override
    public SPageMapping create(String key, String url, String urlAdapter, List<String> authorizationRules) throws SObjectCreationException {
        SPageMappingImpl entity = new SPageMappingImpl();
        entity.setUrl(url);
        entity.setUrlAdapter(urlAdapter);
        entity.setPageAuthorizationRules(authorizationRules);
        entity.setKey(key);
        return insert(entity);

    }

    SInsertEvent getInsertEvent(SPageMappingImpl entity) {
        return (SInsertEvent) BuilderFactory.get(SEventBuilderFactory.class).createInsertEvent(PAGE_MAPPING).setObject(entity).done();
    }

    @Override
    public SPageMapping get(String key) throws SObjectNotFoundException, SBonitaReadException {
        SPageMapping sPageMapping = persistenceService.selectOne(new SelectOneDescriptor<SPageMapping>("getPageMappingByKey", Collections
                .<String, Object> singletonMap("key", key), SPageMapping.class));
        if (sPageMapping == null) {
            throw new SObjectNotFoundException("No page mapping found with key " + key);
        }
        return sPageMapping;
    }

    @Override
    public SPageURL resolvePageURL(SPageMapping pageMapping, Map<String, Serializable> context, boolean executeAuthorizationRules) throws SExecutionException, SAuthorizationException {
        if (executeAuthorizationRules) {
            final List<String> pageAuthorizationRules = pageMapping.getPageAuthorizationRules();
            if (!isAllowedToAccess(pageMapping, context, pageAuthorizationRules)) {
                throw new SAuthorizationException("Access to Page or URL with key " + pageMapping.getKey() + " is not allowed");
            }
        }

        String url = pageMapping.getUrl();
        String urlAdapter = pageMapping.getUrlAdapter();
        if (urlAdapter != null) {
            url = getUrlAdapter(urlAdapter).adapt(url, pageMapping.getKey(), context);
        }
        return new SPageURL(url, pageMapping.getPageId());
    }

    protected boolean isAllowedToAccess(SPageMapping pageMapping, Map<String, Serializable> context, List<String> pageAuthorizationRules)
            throws SExecutionException {
        boolean authorized = true;
        for (String rule : pageAuthorizationRules) {
            final AuthorizationRule authorizationRule = authorizationRuleMap.get(rule);
            if (authorizationRule == null) {
                throw new SExecutionException("Authorization rule " + rule + " is not known. Cannot check if authorized or not.");
            }
            if (authorizationRule.isAllowed(pageMapping.getKey(), context)) {
                return true;
            }
            else {
                authorized = false;
            }
        }
        return authorized;
    }

    private URLAdapter getUrlAdapter(String urlAdapterName) throws SExecutionException {
        URLAdapter urlAdapter = urlAdapterMap.get(urlAdapterName);
        if (urlAdapter == null) {
            throw new SExecutionException("unable to execute the url adapter " + urlAdapterName + " because it does not exists");
        }
        return urlAdapter;
    }

    @Override
    public void delete(SPageMapping sPageMapping) throws SDeletionException {
        try {
            recorder.recordDelete(new DeleteRecord(sPageMapping), getDeleteRecord(sPageMapping));
        } catch (SRecorderException e) {
            throw new SDeletionException("Unable to delete the page mapping with key " + sPageMapping.getKey(), e);
        }
    }

    SDeleteEvent getDeleteRecord(SPageMapping sPageMapping) {
        return (SDeleteEvent) BuilderFactory.get(SEventBuilderFactory.class).createDeleteEvent(PAGE_MAPPING).setObject(sPageMapping).done();
    }

    @Override
    public void update(SPageMapping pageMapping, Long pageId) throws SObjectModificationException, SObjectNotFoundException, SBonitaReadException {
        update(pageMapping, pageId, null, null);

    }

    void update(SPageMapping pageMapping, Long pageId, String url, String urlAdapter) throws SObjectNotFoundException, SBonitaReadException,
            SObjectModificationException {
        try {
            update(pageMapping, getEntityUpdateDescriptor(pageId, url, urlAdapter));
        } catch (SSessionNotFoundException | SessionIdNotSetException | SRecorderException e) {
            throw new SObjectModificationException(e);
        }
    }

    @Override
    public void update(SPageMapping pageMapping, String url, String urlAdapter) throws SObjectModificationException, SObjectNotFoundException,
            SBonitaReadException {
        update(pageMapping, null, url, urlAdapter);

    }

    EntityUpdateDescriptor getEntityUpdateDescriptor(Long pageId, String url, String urlAdapter) throws SSessionNotFoundException, SessionIdNotSetException {
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        descriptor.addField("pageId", pageId);
        descriptor.addField("url", url);
        descriptor.addField("urlAdapter", urlAdapter);
        descriptor.addField("lastUpdatedBy", getSessionUserId());
        descriptor.addField("lastUpdateDate", System.currentTimeMillis());
        return descriptor;
    }

    private long getSessionUserId() throws SSessionNotFoundException, SessionIdNotSetException {
        return sessionService.getLoggedUserFromSession(sessionAccessor);
    }

    void update(SPageMapping pageMapping, EntityUpdateDescriptor descriptor) throws SObjectNotFoundException, SBonitaReadException, SRecorderException {
        final UpdateRecord updateRecord = UpdateRecord.buildSetFields(pageMapping, descriptor);
        recorder.recordUpdate(updateRecord, getUpdateEvent(pageMapping));
    }

    SUpdateEvent getUpdateEvent(SPageMapping sPageMapping) {
        return (SUpdateEvent) BuilderFactory.get(SEventBuilderFactory.class).createUpdateEvent(PAGE_MAPPING).setObject(sPageMapping).done();
    }
}
