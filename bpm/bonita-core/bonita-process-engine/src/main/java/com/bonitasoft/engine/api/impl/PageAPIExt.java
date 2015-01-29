package com.bonitasoft.engine.api.impl;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.bonitasoft.engine.api.impl.SessionInfos;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SObjectAlreadyExistsException;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
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
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.exception.UpdatingWithInvalidPageTokenException;
import org.bonitasoft.engine.exception.UpdatingWithInvalidPageZipContentException;
import org.bonitasoft.engine.page.PageService;
import org.bonitasoft.engine.page.SInvalidPageTokenException;
import org.bonitasoft.engine.page.SInvalidPageZipException;
import org.bonitasoft.engine.page.SInvalidPageZipInconsistentException;
import org.bonitasoft.engine.page.SInvalidPageZipMissingAPropertyException;
import org.bonitasoft.engine.page.SInvalidPageZipMissingIndexException;
import org.bonitasoft.engine.page.SInvalidPageZipMissingPropertiesException;
import org.bonitasoft.engine.page.SPage;
import org.bonitasoft.engine.page.SPageUpdateBuilder;
import org.bonitasoft.engine.page.SPageUpdateBuilderFactory;
import org.bonitasoft.engine.page.SPageUpdateContentBuilder;
import org.bonitasoft.engine.page.SPageUpdateContentBuilderFactory;
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
import com.bonitasoft.engine.page.PageUpdater;
import com.bonitasoft.engine.page.PageUpdater.PageUpdateField;
import com.bonitasoft.engine.search.descriptor.SearchEntitiesDescriptor;
import com.bonitasoft.engine.service.SPModelConvertor;
import com.bonitasoft.engine.service.TenantServiceAccessor;
import com.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import com.bonitasoft.engine.service.impl.TenantServiceSingleton;

@AvailableWhenTenantIsPaused
public class PageAPIExt implements PageAPI {

    @Override
    public Page getPage(final long pageId) throws PageNotFoundException {
        final PageService pageService = getTenantAccessor().getPageService();

        try {
            return convertToPage(pageService.getPage(pageId));
        } catch (final SBonitaReadException e) {
            throw new PageNotFoundException(e);

        } catch (final SObjectNotFoundException e) {
            throw new PageNotFoundException(e);
        }
    }

    @Override
    public byte[] getPageContent(final long pageId) throws PageNotFoundException {
        final PageService pageService = getTenantAccessor().getPageService();

        try {
            return pageService.getPageContent(pageId);
        } catch (final SBonitaReadException e) {
            throw new PageNotFoundException(e);

        } catch (final SObjectNotFoundException e) {
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
    public Page createPage(final PageCreator pageCreator, final byte[] content) throws AlreadyExistsException, CreationException, InvalidPageTokenException,
            InvalidPageZipContentException {
        final PageService pageService = getTenantAccessor().getPageService();
        final long userId = getUserIdFromSessionInfos();
        final SPage sPage = constructPage(pageCreator, userId);

        try {
            final SPage addPage = pageService.addPage(sPage, content);
            return convertToPage(addPage);
        } catch (final SObjectAlreadyExistsException e) {
            throw new AlreadyExistsException("A page already exists with the name " + pageCreator.getName());
        } catch (final SInvalidPageTokenException e) {
            throw new InvalidPageTokenException(e.getMessage(), e);
        } catch (final SInvalidPageZipException e) {
            throw convertException(e);
        } catch (final SBonitaException e) {
            throw new CreationException(e);
        }
    }

    private InvalidPageZipContentException convertException(final SInvalidPageZipException e) {
        if (e instanceof SInvalidPageZipMissingPropertiesException) {
            return new InvalidPageZipMissingPropertiesException();
        }
        if (e instanceof SInvalidPageZipMissingAPropertyException) {
            return new InvalidPageZipMissingAPropertyException(((SInvalidPageZipMissingAPropertyException) e).getFields());
        }
        if (e instanceof SInvalidPageZipInconsistentException) {
            return new InvalidPageZipInconsistentException(e.getMessage(), e);
        }
        if (e instanceof SInvalidPageZipMissingIndexException) {
            return new InvalidPageZipMissingIndexException();
        }
        return new InvalidPageZipContentException(e.getMessage(), e);
    }

    @Override
    public Page createPage(final String contentName, final byte[] content) throws AlreadyExistsException, CreationException, InvalidPageTokenException,
            InvalidPageZipContentException {
        final PageService pageService = getTenantAccessor().getPageService();
        final long userId = getUserIdFromSessionInfos();

        try {
            final SPage addPage = pageService.addPage(content, contentName, userId);
            return convertToPage(addPage);
        } catch (final SObjectAlreadyExistsException e) {
            throw new AlreadyExistsException("A page already exists with the name defined in page zip content");
        } catch (final SInvalidPageTokenException e) {
            throw new InvalidPageTokenException(e.getMessage(), e);
        } catch (final SInvalidPageZipException e) {
            throw convertException(e);
        } catch (final SBonitaException e) {
            throw new CreationException(e);
        }
    }

    protected Page convertToPage(final SPage addPage) {
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
            for (final Long pageId : pageIds) {
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

    @Override
    public Page getPageByName(final String name) throws PageNotFoundException {
        final PageService pageService = getTenantAccessor().getPageService();

        try {
            final SPage sPage = pageService.getPageByName(name);
            if (sPage == null) {
                throw new PageNotFoundException(name);
            }
            return convertToPage(sPage);
        } catch (final SBonitaReadException e) {
            throw new PageNotFoundException(e);
        }
    }

    @Override
    public Page updatePage(final long pageId, final PageUpdater pageUpdater) throws UpdateException, AlreadyExistsException,
            UpdatingWithInvalidPageTokenException,
            UpdatingWithInvalidPageZipContentException {
        if (pageUpdater == null || pageUpdater.getFields().isEmpty()) {
            throw new UpdateException("The pageUpdater descriptor does not contain field updates");
        }
        final PageService pageService = getTenantAccessor().getPageService();

        final SPageUpdateBuilder pageUpdateBuilder = getPageUpdateBuilder();
        final Map<PageUpdateField, Serializable> fields = pageUpdater.getFields();
        for (final Entry<PageUpdateField, Serializable> field : fields.entrySet()) {
            switch (field.getKey()) {
                case NAME:
                    pageUpdateBuilder.updateName((String) field.getValue());
                    break;
                case DISPLAY_NAME:
                    pageUpdateBuilder.updateDisplayName((String) field.getValue());
                    break;
                case DESCRIPTION:
                    pageUpdateBuilder.updateDescription((String) field.getValue());
                    break;
                case CONTENT_NAME:
                    pageUpdateBuilder.updateContentName((String) field.getValue());
                    break;
                default:
                    break;
            }
        }
        pageUpdateBuilder.updateLastModificationDate(System.currentTimeMillis());
        pageUpdateBuilder.updateLastUpdatedBy(getUserIdFromSessionInfos());

        SPage updatedPage;
        try {
            updatedPage = pageService.updatePage(pageId, pageUpdateBuilder.done());
            return convertToPage(updatedPage);
        } catch (final SObjectModificationException e) {
            throw new UpdateException(e);
        } catch (final SObjectAlreadyExistsException e) {
            throw new AlreadyExistsException(e);
        } catch (final SInvalidPageTokenException e) {
            throw new UpdatingWithInvalidPageTokenException(e.getMessage(), e);
        }

    }

    protected SPageUpdateBuilder getPageUpdateBuilder() {
        return BuilderFactory.get(SPageUpdateBuilderFactory.class)
                .createNewInstance(new EntityUpdateDescriptor());
    }

    @Override
    public void updatePageContent(final long pageId, final byte[] content) throws UpdateException, UpdatingWithInvalidPageTokenException,
            UpdatingWithInvalidPageZipContentException {
        final PageService pageService = getTenantAccessor().getPageService();
        final SPageUpdateBuilder pageUpdateBuilder = getPageUpdateBuilder();
        pageUpdateBuilder.updateLastModificationDate(System.currentTimeMillis());
        pageUpdateBuilder.updateLastUpdatedBy(getUserIdFromSessionInfos());
        try {
            final SPage page = pageService.getPage(pageId);
            pageService.updatePageContent(pageId, content, page.getContentName());
            pageService.updatePage(pageId, pageUpdateBuilder.done());
        } catch (final SInvalidPageTokenException e) {
            throw new UpdatingWithInvalidPageTokenException(e.getMessage(), e);
        } catch (final SInvalidPageZipException e) {
            throw new UpdatingWithInvalidPageZipContentException(e.getMessage(), e);
        } catch (final SBonitaException sBonitaException) {
            throw new UpdateException(sBonitaException);
        }
    }

    protected SPageUpdateContentBuilder getPageUpdateContentBuilder() {
        return BuilderFactory.get(SPageUpdateContentBuilderFactory.class)
                .createNewInstance(new EntityUpdateDescriptor());
    }

    protected SPage constructPage(final PageUpdater pageUpdater, final long userId) {
        return SPModelConvertor.constructSPage(pageUpdater, userId);
    }

    @Override
    public Properties getPageProperties(final byte[] content, final boolean checkIfItAlreadyExists) throws InvalidPageTokenException,
            AlreadyExistsException, InvalidPageZipMissingPropertiesException, InvalidPageZipMissingIndexException, InvalidPageZipInconsistentException,
            InvalidPageZipMissingAPropertyException {
        final PageService pageService = getTenantAccessor().getPageService();
        try {
            return getProperties(content, checkIfItAlreadyExists, pageService);
        } catch (final SInvalidPageTokenException e) {
            throw new InvalidPageTokenException(e.getMessage());
        } catch (final SBonitaReadException e) {
            throw new RetrieveException(e);
        } catch (final SInvalidPageZipMissingAPropertyException e) {
            throw new InvalidPageZipMissingAPropertyException(e.getFields());
        } catch (final SInvalidPageZipInconsistentException e) {
            throw new InvalidPageZipInconsistentException(e.getMessage(), e);
        } catch (final SInvalidPageZipMissingIndexException e) {
            throw new InvalidPageZipMissingIndexException();
        } catch (final SInvalidPageZipMissingPropertiesException e) {
            throw new InvalidPageZipMissingPropertiesException();
        }
    }

    private Properties getProperties(final byte[] content, final boolean checkIfItAlreadyExists, final PageService pageService)
            throws SInvalidPageZipMissingIndexException, SInvalidPageZipMissingAPropertyException, SInvalidPageZipInconsistentException,
            SInvalidPageZipMissingPropertiesException, SInvalidPageTokenException, SBonitaReadException, AlreadyExistsException {
        final Properties properties = pageService.readPageZip(content);
        if (checkIfItAlreadyExists) {
            final String name = properties.getProperty(PageService.PROPERTIES_NAME);
            final SPage pageByName = pageService.getPageByName(name);
            if (pageByName != null) {
                throw new AlreadyExistsException("A page with name " + name + " already exists");
            }
        }
        return properties;
    }
}
