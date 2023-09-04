/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.api.impl;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.bonitasoft.engine.api.PageAPI;
import org.bonitasoft.engine.api.impl.page.PageAPIDelegate;
import org.bonitasoft.engine.commons.exceptions.SExecutionException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.exception.*;
import org.bonitasoft.engine.page.*;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.service.ModelConvertor;
import org.bonitasoft.engine.service.ServiceAccessor;
import org.bonitasoft.engine.service.ServiceAccessorSingleton;

@AvailableWhenTenantIsPaused
public class PageAPIImpl implements PageAPI {

    protected ServiceAccessor getServiceAccessor() {
        try {
            return ServiceAccessorSingleton.getInstance();
        } catch (final Exception e) {
            throw new BonitaRuntimeException(e);
        }
    }

    protected PageAPIDelegate getPageAPIDelegate() {
        return PageAPIDelegate.getInstance();
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
    public Page createPage(final PageCreator pageCreator, final byte[] content) throws CreationException {
        return getPageAPIDelegate().createPage(pageCreator, content, SessionInfos.getUserIdFromSession());
    }

    @Override
    public Page createPage(final String contentName, final byte[] content) throws CreationException {
        return getPageAPIDelegate().createPage(contentName, content, SessionInfos.getUserIdFromSession());
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
    public Page getPageByNameAndProcessDefinitionId(String name, long processDefinitionId)
            throws PageNotFoundException {
        return getPageAPIDelegate().getPageByNameAndProcessDefinition(name, processDefinitionId);
    }

    @Override
    public Page updatePage(final long pageId, final PageUpdater pageUpdater)
            throws UpdateException, AlreadyExistsException {
        return getPageAPIDelegate().updatePage(pageId, pageUpdater, SessionInfos.getUserIdFromSession());
    }

    @Override
    public void updatePageContent(final long pageId, final byte[] content) throws UpdateException {
        getPageAPIDelegate().updatePageContent(pageId, content, SessionInfos.getUserIdFromSession());
    }

    @Override
    public Properties getPageProperties(final byte[] content, final boolean checkIfItAlreadyExists)
            throws InvalidPageTokenException, AlreadyExistsException,
            InvalidPageZipMissingPropertiesException, InvalidPageZipMissingIndexException,
            InvalidPageZipInconsistentException,
            InvalidPageZipMissingAPropertyException {
        return getPageAPIDelegate().getPageProperties(content, checkIfItAlreadyExists);
    }

    @Override
    public PageURL resolvePageOrURL(String key, Map<String, Serializable> context, boolean executeAuthorizationRules)
            throws NotFoundException, ExecutionException, UnauthorizedAccessException {
        final PageMappingService pageMappingService = retrievePageMappingService();
        try {
            return ModelConvertor.toPageURL(
                    pageMappingService.resolvePageURL(pageMappingService.get(key), context, executeAuthorizationRules));
        } catch (final SObjectNotFoundException e) {
            throw new NotFoundException(e);
        } catch (final SBonitaReadException e) {
            throw new RetrieveException(e);
        } catch (final SExecutionException e) {
            throw new ExecutionException(e);
        } catch (final SAuthorizationException e) {
            throw new UnauthorizedAccessException(e);
        }
    }

    PageMappingService retrievePageMappingService() {
        return getServiceAccessor().getPageMappingService();
    }

}
