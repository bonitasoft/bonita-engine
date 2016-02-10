/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl;

import java.util.List;
import java.util.Properties;

import org.bonitasoft.engine.api.impl.AvailableWhenTenantIsPaused;
import org.bonitasoft.engine.api.impl.PageAPIImpl;
import org.bonitasoft.engine.api.impl.SessionInfos;
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
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.search.impl.SearchResultImpl;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;

import com.bonitasoft.engine.api.PageAPI;
import com.bonitasoft.engine.api.converter.CollectionConverter;
import com.bonitasoft.engine.page.Page;
import com.bonitasoft.engine.page.PageCreator;
import com.bonitasoft.engine.page.PageNotFoundException;
import com.bonitasoft.engine.page.PageUpdater;
import com.bonitasoft.engine.page.impl.PageConverter;
import com.bonitasoft.engine.service.TenantServiceAccessor;
import com.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import com.bonitasoft.engine.service.impl.TenantServiceSingleton;

/**
 * @author Emmanuel Duchastenier
 * @deprecated from version 7.0 on, use {@link PageAPIImpl} instead.
 */
@Deprecated
@AvailableWhenTenantIsPaused
public class PageAPIExt implements PageAPI {

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
        try {
            return new PageConverter().convert(getPageAPIDelegate().getPage(pageId));
        } catch (final org.bonitasoft.engine.page.PageNotFoundException e) {
            throw new PageNotFoundException(e.getCause());
        }
    }

    @Override
    public byte[] getPageContent(final long pageId) throws PageNotFoundException {
        try {
            return getPageAPIDelegate().getPageContent(pageId);
        } catch (final org.bonitasoft.engine.page.PageNotFoundException e) {
            throw new PageNotFoundException(e.getCause());
        }
    }

    @Override
    public SearchResult<Page> searchPages(final SearchOptions searchOptions) throws SearchException {
        final SearchResult<org.bonitasoft.engine.page.Page> searchedPages = getPageAPIDelegate().searchPages(searchOptions);
        return new SearchResultImpl<Page>(searchedPages.getCount(), new CollectionConverter().convert(searchedPages.getResult(), new PageConverter()));

    }

    @Override
    public Page createPage(final PageCreator pageCreator, final byte[] content) throws AlreadyExistsException, CreationException, InvalidPageTokenException,
            InvalidPageZipContentException {
        return new PageConverter().convert(getPageAPIDelegate().createPage(pageCreator.getDelegate(), content));
    }

    @Override
    public Page createPage(final String contentName, final byte[] content) throws AlreadyExistsException, CreationException, InvalidPageTokenException,
            InvalidPageZipContentException {
        return new PageConverter().convert(getPageAPIDelegate().createPage(contentName, content));
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
        try {
            return new PageConverter().convert(getPageAPIDelegate().getPageByName(name));
        } catch (final org.bonitasoft.engine.page.PageNotFoundException e) {
            throw new PageNotFoundException(e.getCause());
        }
    }

    @Override
    public Page updatePage(final long pageId, final PageUpdater pageUpdater) throws UpdateException, AlreadyExistsException,
            UpdatingWithInvalidPageTokenException, UpdatingWithInvalidPageZipContentException {
        return new PageConverter().convert(getPageAPIDelegate().updatePage(pageId, pageUpdater.getDelegate()));
    }

    @Override
    public void updatePageContent(final long pageId, final byte[] content) throws UpdateException, UpdatingWithInvalidPageTokenException,
            UpdatingWithInvalidPageZipContentException {
        getPageAPIDelegate().updatePageContent(pageId, content);
    }

    @Override
    public Properties getPageProperties(final byte[] content, final boolean checkIfItAlreadyExists) throws InvalidPageTokenException,
            AlreadyExistsException, InvalidPageZipMissingPropertiesException, InvalidPageZipMissingIndexException, InvalidPageZipInconsistentException,
            InvalidPageZipMissingAPropertyException {
        return getPageAPIDelegate().getPageProperties(content, checkIfItAlreadyExists);
    }
}
