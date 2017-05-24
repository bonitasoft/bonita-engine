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

import org.bonitasoft.engine.commons.exceptions.SDeletionException;
import org.bonitasoft.engine.commons.exceptions.SExecutionException;
import org.bonitasoft.engine.commons.exceptions.SObjectCreationException;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.page.AuthorizationRule;
import org.bonitasoft.engine.page.PageMappingService;
import org.bonitasoft.engine.page.SAuthorizationException;
import org.bonitasoft.engine.page.SPageMapping;
import org.bonitasoft.engine.page.SPageURL;
import org.bonitasoft.engine.page.URLAdapter;
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

    public PageMappingServiceImpl(final Recorder recorder, final ReadPersistenceService persistenceService, final SessionService sessionService,
            final ReadSessionAccessor sessionAccessor) {
        this.recorder = recorder;
        this.persistenceService = persistenceService;
        this.sessionService = sessionService;
        this.sessionAccessor = sessionAccessor;
        urlAdapterMap = new HashMap<>();
        authorizationRuleMap = new HashMap<>();
    }

    public void setURLAdapters(final List<URLAdapter> urlAdapters) {
        for (final URLAdapter urlAdapter : urlAdapters) {
            urlAdapterMap.put(urlAdapter.getId(), urlAdapter);
        }
    }

    public void setAuthorizationRules(final List<AuthorizationRule> authorizationRules) {
        for (final AuthorizationRule authorizationRule : authorizationRules) {
            authorizationRuleMap.put(authorizationRule.getId(), authorizationRule);
        }
    }

    @Override
    public SPageMapping create(final String key, final Long pageId, final List<String> authorizationRules) throws SObjectCreationException {
        SPageMapping pageMapping = null;
        try {
            pageMapping = findMapping(key);
        } catch (final SBonitaReadException e) {
            throw new SObjectCreationException(String.format("Failed to get page mapping %s", key), e);
        }
        if (pageMapping == null) {
            final SPageMappingImpl entity = new SPageMappingImpl();
            entity.setPageId(pageId);
            entity.setPageAuthorizationRules(authorizationRules);
            entity.setKey(key);
            return insert(entity);
        }
        throw new SObjectCreationException(String.format("Mapping key %s already exists for page with id %s", key, pageMapping.getPageId()));
    }

    SPageMapping insert(final SPageMappingImpl entity) throws SObjectCreationException {
        try {
            recorder.recordInsert(new InsertRecord(entity), PAGE_MAPPING);
        } catch (final SRecorderException e) {
            throw new SObjectCreationException(e);
        }
        return entity;
    }

    @Override
    public SPageMapping create(final String key, final String url, final String urlAdapter, final List<String> authorizationRules)
            throws SObjectCreationException {
        SPageMapping pageMapping = null;
        try {
            pageMapping = findMapping(key);
        } catch (final SBonitaReadException e) {
            throw new SObjectCreationException(String.format("Failed to get page mapping %s", key), e);
        }
        if (pageMapping == null) {
            final SPageMappingImpl entity = new SPageMappingImpl();
            entity.setUrl(url);
            entity.setUrlAdapter(urlAdapter);
            entity.setPageAuthorizationRules(authorizationRules);
            entity.setKey(key);
            return insert(entity);
        }
        throw new SObjectCreationException(String.format("Mapping key %s already exists for page with id %s", key, pageMapping.getPageId()));
    }

    @Override
    public SPageMapping get(final String key) throws SObjectNotFoundException, SBonitaReadException {
        final SPageMapping sPageMapping = findMapping(key);
        if (sPageMapping == null) {
            throw new SObjectNotFoundException("No page mapping found with key " + key);
        }
        return sPageMapping;
    }

    private SPageMapping findMapping(final String key) throws SBonitaReadException {
        return persistenceService.selectOne(new SelectOneDescriptor<SPageMapping>("getPageMappingByKey", Collections
                .<String, Object> singletonMap("key", key), SPageMapping.class));
    }

    @Override
    public SPageURL resolvePageURL(final SPageMapping pageMapping, final Map<String, Serializable> context, final boolean executeAuthorizationRules)
            throws SExecutionException, SAuthorizationException {
        if (executeAuthorizationRules) {
            final List<String> pageAuthorizationRules = pageMapping.getPageAuthorizationRules();
            if (!isAllowedToAccess(pageMapping, context, pageAuthorizationRules)) {
                throw new SAuthorizationException("Access to Page or URL with key " + pageMapping.getKey() + " is not allowed");
            }
        }
        String url = pageMapping.getUrl();
        final String urlAdapter = pageMapping.getUrlAdapter();
        if (urlAdapter != null) {
            url = getUrlAdapter(urlAdapter).adapt(url, pageMapping.getKey(), context);
        }
        return new SPageURL(url, pageMapping.getPageId());
    }

    protected boolean isAllowedToAccess(final SPageMapping pageMapping, final Map<String, Serializable> context, final List<String> pageAuthorizationRules)
            throws SExecutionException {
        boolean authorized = true;
        for (final String rule : pageAuthorizationRules) {
            final AuthorizationRule authorizationRule = authorizationRuleMap.get(rule);
            if (authorizationRule == null) {
                throw new SExecutionException("Authorization rule " + rule + " is not known. Cannot check if authorized or not.");
            }
            if (authorizationRule.isAllowed(pageMapping.getKey(), context)) {
                return true;
            } else {
                authorized = false;
            }
        }
        return authorized;
    }

    private URLAdapter getUrlAdapter(final String urlAdapterName) throws SExecutionException {
        final URLAdapter urlAdapter = urlAdapterMap.get(urlAdapterName);
        if (urlAdapter == null) {
            throw new SExecutionException("unable to execute the url adapter " + urlAdapterName + " because it does not exists");
        }
        return urlAdapter;
    }

    @Override
    public void delete(final SPageMapping sPageMapping) throws SDeletionException {
        try {
            recorder.recordDelete(new DeleteRecord(sPageMapping), PAGE_MAPPING);
        } catch (final SRecorderException e) {
            throw new SDeletionException("Unable to delete the page mapping with key " + sPageMapping.getKey(), e);
        }
    }

    @Override
    public void update(final SPageMapping pageMapping, final Long pageId) throws SObjectModificationException, SObjectNotFoundException, SBonitaReadException {
        update(pageMapping, pageId, null, null);

    }

    void update(final SPageMapping pageMapping, final Long pageId, final String url, final String urlAdapter)
            throws SObjectNotFoundException, SBonitaReadException,
            SObjectModificationException {
        try {
            update(pageMapping, getEntityUpdateDescriptor(pageId, url, urlAdapter));
        } catch (SSessionNotFoundException | SessionIdNotSetException | SRecorderException e) {
            throw new SObjectModificationException(e);
        }
    }

    @Override
    public void update(final SPageMapping pageMapping, final String url, final String urlAdapter) throws SObjectModificationException, SObjectNotFoundException,
            SBonitaReadException {
        update(pageMapping, null, url, urlAdapter);

    }

    EntityUpdateDescriptor getEntityUpdateDescriptor(final Long pageId, final String url, final String urlAdapter)
            throws SSessionNotFoundException, SessionIdNotSetException {
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

    void update(final SPageMapping pageMapping, final EntityUpdateDescriptor descriptor)
            throws SObjectNotFoundException, SBonitaReadException, SRecorderException {
        recorder.recordUpdate(UpdateRecord.buildSetFields(pageMapping, descriptor), PAGE_MAPPING);
    }

    @Override
    public List<SPageMapping> get(final long pageId, final int startIndex, final int maxResults) throws SBonitaReadException {
        final QueryOptions options = new QueryOptions(startIndex, maxResults);
        final SelectListDescriptor<SPageMapping> listDescriptor = new SelectListDescriptor<SPageMapping>("getPageMappingByPageId",
                Collections.<String, Object> singletonMap("pageId", pageId), SPageMapping.class, options);
        return persistenceService.selectList(listDescriptor);
    }

}
