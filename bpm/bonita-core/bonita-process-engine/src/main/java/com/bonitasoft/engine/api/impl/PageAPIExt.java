package com.bonitasoft.engine.api.impl;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bonitasoft.engine.api.impl.SessionInfos;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;

import com.bonitasoft.engine.api.PageAPI;
import com.bonitasoft.engine.api.impl.transaction.page.SearchPages;
import com.bonitasoft.engine.page.Page;
import com.bonitasoft.engine.page.PageCreator;
import com.bonitasoft.engine.page.PageNotFoundException;
import com.bonitasoft.engine.page.PageService;
import com.bonitasoft.engine.page.PageUpdater;
import com.bonitasoft.engine.page.PageUpdater.PageUpdateField;
import com.bonitasoft.engine.page.SPage;
import com.bonitasoft.engine.page.SPageBuilderFactory;
import com.bonitasoft.engine.search.descriptor.SearchEntitiesDescriptor;
import com.bonitasoft.engine.service.SPModelConvertor;
import com.bonitasoft.engine.service.TenantServiceAccessor;
import com.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import com.bonitasoft.engine.service.impl.TenantServiceSingleton;

public class PageAPIExt implements PageAPI {

    @Override
    public Page getPage(final long pageId) throws PageNotFoundException {
        final PageService pageService = getTenantAccessor().getPageService();

        try {
            SPage sPage = pageService.getPage(pageId);
            return convertToPage(sPage);
        } catch (SBonitaReadException e) {
            throw new PageNotFoundException(e);
        } catch (SObjectNotFoundException e) {
            throw new PageNotFoundException(e);
        }
    }

    @Override
    public byte[] getPageContent(final long pageId) throws PageNotFoundException {
        final PageService pageService = getTenantAccessor().getPageService();

        try {
            byte[] content = pageService.getPageContent(pageId);
            return content;
        } catch (SBonitaReadException e) {
            throw new PageNotFoundException(e);
        } catch (SObjectNotFoundException e) {
            throw new PageNotFoundException(e);
        }
    }

    @Override
    public SearchResult<Page> searchPages(final SearchOptions searchOptions) throws SearchException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
        final PageService pageService = tenantAccessor.getPageService();
        final SearchPages searchPages = getSearchPages(searchOptions, searchEntitiesDescriptor, pageService);
        try {
            searchPages.execute();
            return searchPages.getResult();
        } catch (final SBonitaException sbe) {
            throw new SearchException(sbe);
        }

    }

    protected SearchPages getSearchPages(final SearchOptions searchOptions, final SearchEntitiesDescriptor searchEntitiesDescriptor,
            final PageService pageService) {
        return new SearchPages(pageService, searchEntitiesDescriptor.getSearchPageDescriptor(), searchOptions);
    }

    @Override
    public Page createPage(final PageCreator pageCreator, final byte[] content) throws AlreadyExistsException, CreationException {
        final PageService pageService = getTenantAccessor().getPageService();
        final long userId = getUserIdFromSessionInfos();
        final SPage sPage = constructPage(pageCreator, userId);

        checkPageAlreadyExists((String) pageCreator.getFields().get(PageCreator.PageField.NAME), getTenantAccessor());
        try {
            SPage addPage = pageService.addPage(sPage, content);
            return convertToPage(addPage);
        } catch (final SBonitaException sBonitaException) {
            throw new CreationException(sBonitaException);
        }
    }

    protected Page convertToPage(SPage addPage) {
        return SPModelConvertor.toPage(addPage);
    }

    protected SPage constructPage(final PageCreator pageCreator, final long userId) {
        return SPModelConvertor.constructSPage(pageCreator, userId);
    }

    protected long getUserIdFromSessionInfos() {
        final long userId = SessionInfos.getUserIdFromSession();
        return userId;
    }

    @Override
    public void deletePage(final long pageId) throws DeletionException {
        final PageService pageService = getTenantAccessor().getPageService();
        try {
            pageService.deletePage(pageId);

        } catch (final SBonitaException sBonitaException) {
            throw new DeletionException(sBonitaException);
        }
    }

    @Override
    public void deletePages(final List<Long> pageIds) throws DeletionException {
        final PageService pageService = getTenantAccessor().getPageService();
        try {
            for (Long pageId : pageIds) {
                pageService.deletePage(pageId);
            }
        } catch (final SBonitaException sBonitaException) {
            throw new DeletionException(sBonitaException);
        }

    }

    protected TenantServiceAccessor getTenantAccessor() {
        try {
            final SessionAccessor sessionAccessor = ServiceAccessorFactory.getInstance().createSessionAccessor();
            final long tenantId = sessionAccessor.getTenantId();
            return TenantServiceSingleton.getInstance(tenantId);
        } catch (final Exception e) {
            throw new BonitaRuntimeException(e);
        }
    }

    protected void checkPageAlreadyExists(final String name, final TenantServiceAccessor tenantAccessor) throws AlreadyExistsException {
        final PageService pageService = getTenantAccessor().getPageService();
        // Check if the problem is primary key duplication:
        try {
            final SPage sPage = pageService.getPageByName(name);
            if (sPage != null) {
                throw new AlreadyExistsException("A report already exists with the name " + name);
            }
        } catch (SBonitaException e) {
            // ignore it
        }
    }

    private EntityUpdateDescriptor getPageUpdateDescriptor(final PageUpdater udpateDescriptor) {
        final SPageBuilderFactory pageBuilderFactory = BuilderFactory.get(SPageBuilderFactory.class);
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        final Map<PageUpdateField, Serializable> fields = udpateDescriptor.getFields();
        for (final Entry<PageUpdateField, Serializable> field : fields.entrySet()) {
            switch (field.getKey()) {
                case NAME:
                    descriptor.addField(pageBuilderFactory.getNameKey(), field.getValue());
                    break;
                case DESCRIPTION:
                    descriptor.addField(pageBuilderFactory.getDescriptionKey(), field.getValue());
                    break;
                case DISPLAY_NAME:
                    descriptor.addField(pageBuilderFactory.getDisplayNameKey(), field.getValue());
                    break;
                default:
                    break;
            }
        }
        return descriptor;
    }

    @Override
    public Page getPageByName(String name) throws PageNotFoundException {
        final PageService pageService = getTenantAccessor().getPageService();

        try {
            SPage sPage = pageService.getPageByName(name);
            return convertToPage(sPage);
        } catch (SBonitaReadException e) {
            throw new PageNotFoundException(e);
        }
    }

    @Override
    public Page updatePage(long pageId, final PageUpdater pageUpdater) throws UpdateException {
        final PageService pageService = getTenantAccessor().getPageService();
        final long userId = getUserIdFromSessionInfos();
        final SPage sPage = SPModelConvertor.constructSPage(pageUpdater, userId);

        try {
            SPage updatedPage = pageService.updatePage(pageId, sPage);
            return convertToPage(updatedPage);
        } catch (final SBonitaException sBonitaException) {
            throw new UpdateException(sBonitaException);
        }
    }

    @Override
    public void updatePageContent(long pageId, byte[] content) throws UpdateException {
        final PageService pageService = getTenantAccessor().getPageService();

        try {
            pageService.updatePageContent(pageId, content);

        } catch (final SBonitaException sBonitaException) {
            throw new UpdateException(sBonitaException);
        }
    }

    protected SPage constructPage(PageUpdater pageUpdater, long userId) {
        return SPModelConvertor.constructSPage(pageUpdater, userId);

    }

}
