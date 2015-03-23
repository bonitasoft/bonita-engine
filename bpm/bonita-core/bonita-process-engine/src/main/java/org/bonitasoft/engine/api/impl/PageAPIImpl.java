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
package org.bonitasoft.engine.api.impl;

import java.util.List;
import java.util.Properties;

import org.bonitasoft.engine.api.PageAPI;
import org.bonitasoft.engine.api.impl.page.PageAPIDelegate;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.InvalidPageTokenException;
import org.bonitasoft.engine.exception.InvalidPageZipContentException;
import org.bonitasoft.engine.exception.InvalidPageZipInconsistentException;
import org.bonitasoft.engine.exception.InvalidPageZipMissingAPropertyException;
import org.bonitasoft.engine.exception.InvalidPageZipMissingIndexException;
import org.bonitasoft.engine.exception.InvalidPageZipMissingPropertiesException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.exception.UpdatingWithInvalidPageTokenException;
import org.bonitasoft.engine.exception.UpdatingWithInvalidPageZipContentException;
import org.bonitasoft.engine.page.Page;
import org.bonitasoft.engine.page.PageCreator;
import org.bonitasoft.engine.page.PageNotFoundException;
import org.bonitasoft.engine.page.PageUpdater;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceSingleton;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;

@AvailableWhenTenantIsPaused
public class PageAPIImpl implements PageAPI {

    protected TenantServiceAccessor getTenantAccessor() {
        try {
            final SessionAccessor sessionAccessor = ServiceAccessorFactory.getInstance().createSessionAccessor();
            final long tenantId = sessionAccessor.getTenantId();
            return TenantServiceSingleton.getInstance(tenantId);
        } catch (final Exception e) {
            throw new BonitaRuntimeException(e);
        }
    }

    private PageAPIDelegate getPageAPIDelegate() {
        return new PageAPIDelegate(getTenantAccessor(), SessionInfos.getUserIdFromSession());
    }

    @Override
    public Page getPage(final long pageId) throws PageNotFoundException {
        return getPageAPIDelegate().getPage(pageId);
    }

    @Override
    public byte[] getPageContent(final long pageId) throws PageNotFoundException {
        return getPageAPIDelegate().getPageContent(pageId);
    }

    @Override
    public SearchResult<Page> searchPages(final SearchOptions searchOptions) throws SearchException {
        return getPageAPIDelegate().searchPages(searchOptions);
    }

    @Override
    public Page createPage(final PageCreator pageCreator, final byte[] content) throws AlreadyExistsException, CreationException, InvalidPageTokenException,
            InvalidPageZipContentException {
        return getPageAPIDelegate().createPage(pageCreator, content);
    }

    @Override
    public Page createPage(final String contentName, final byte[] content) throws AlreadyExistsException, CreationException, InvalidPageTokenException,
            InvalidPageZipContentException {
        return getPageAPIDelegate().createPage(contentName, content);
    }

    protected long getUserIdFromSessionInfos() {
        return SessionInfos.getUserIdFromSession();
    }

    @Override
    public void deletePage(final long pageId) throws DeletionException {
        getPageAPIDelegate().deletePage(pageId);
    }

    @Override
    public void deletePages(final List<Long> pageIds) throws DeletionException {
        getPageAPIDelegate().deletePages(pageIds);
    }

    @Override
    public Page getPageByName(final String name) throws PageNotFoundException {
        return getPageAPIDelegate().getPageByName(name);
    }

    @Override
    public Page getPageByNameAndProcessDefinitionId(String name, long processDefinitionId) throws PageNotFoundException {
        return getPageAPIDelegate().getPageByNameAndProcessDefinition(name,processDefinitionId);
    }

    @Override
    public Page updatePage(final long pageId, final PageUpdater pageUpdater) throws UpdateException, AlreadyExistsException,
            UpdatingWithInvalidPageTokenException, UpdatingWithInvalidPageZipContentException {
        return getPageAPIDelegate().updatePage(pageId, pageUpdater);
    }

    @Override
    public void updatePageContent(final long pageId, final byte[] content) throws UpdateException, UpdatingWithInvalidPageTokenException,
            UpdatingWithInvalidPageZipContentException {
        getPageAPIDelegate().updatePageContent(pageId, content);
    }

    @Override
    public Properties getPageProperties(final byte[] content, final boolean checkIfItAlreadyExists) throws InvalidPageTokenException, AlreadyExistsException,
            InvalidPageZipMissingPropertiesException, InvalidPageZipMissingIndexException, InvalidPageZipInconsistentException,
            InvalidPageZipMissingAPropertyException {
        return getPageAPIDelegate().getPageProperties(content, checkIfItAlreadyExists);
    }

}
