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
package org.bonitasoft.engine.api.impl.page;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.bonitasoft.engine.api.impl.converter.PageModelConverter;
import org.bonitasoft.engine.api.impl.resolver.DependencyResolver;
import org.bonitasoft.engine.api.impl.transaction.page.SearchPages;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SObjectAlreadyExistsException;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.core.form.FormMappingService;
import org.bonitasoft.engine.core.form.SFormMapping;
import org.bonitasoft.engine.exception.AlreadyExistsException;
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
import org.bonitasoft.engine.form.FormMappingSearchDescriptor;
import org.bonitasoft.engine.page.Page;
import org.bonitasoft.engine.page.PageCreator;
import org.bonitasoft.engine.page.PageMappingService;
import org.bonitasoft.engine.page.PageNotFoundException;
import org.bonitasoft.engine.page.PageService;
import org.bonitasoft.engine.page.PageUpdater;
import org.bonitasoft.engine.page.SInvalidPageTokenException;
import org.bonitasoft.engine.page.SInvalidPageZipException;
import org.bonitasoft.engine.page.SInvalidPageZipInconsistentException;
import org.bonitasoft.engine.page.SInvalidPageZipMissingAPropertyException;
import org.bonitasoft.engine.page.SInvalidPageZipMissingIndexException;
import org.bonitasoft.engine.page.SInvalidPageZipMissingPropertiesException;
import org.bonitasoft.engine.page.SPage;
import org.bonitasoft.engine.page.SPageMapping;
import org.bonitasoft.engine.page.SPageUpdateBuilder;
import org.bonitasoft.engine.page.SPageUpdateBuilderFactory;
import org.bonitasoft.engine.page.SPageUpdateContentBuilder;
import org.bonitasoft.engine.page.SPageUpdateContentBuilderFactory;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.search.descriptor.SearchEntitiesDescriptor;
import org.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * @author Emmanuel Duchastenier
 */
public class PageAPIDelegate {

    private final TenantServiceAccessor tenantAccessor;
    private final long userIdFromSession;
    private final PageService pageService;
    private final SearchEntitiesDescriptor searchEntitiesDescriptor;
    private final PageMappingService pageMappingService;
    private final FormMappingService formMappingService;
    private final DependencyResolver dependencyResolver;

    public PageAPIDelegate(final TenantServiceAccessor tenantAccessor, final long userIdFromSession) {
        this.tenantAccessor = tenantAccessor;
        dependencyResolver = tenantAccessor.getDependencyResolver();
        this.userIdFromSession = userIdFromSession;
        pageService = tenantAccessor.getPageService();
        pageMappingService = tenantAccessor.getPageMappingService();
        formMappingService = tenantAccessor.getFormMappingService();
        searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
    }

    public Page getPage(final long pageId) throws PageNotFoundException {
        try {
            return convertToPage(pageService.getPage(pageId));
        } catch (final SBonitaReadException e) {
            throw new PageNotFoundException(e);
        } catch (final SObjectNotFoundException e) {
            throw new PageNotFoundException(e);
        }
    }

    public byte[] getPageContent(final long pageId) throws PageNotFoundException {
        try {
            return pageService.getPageContent(pageId);
        } catch (final SBonitaReadException e) {
            throw new PageNotFoundException(e);

        } catch (final SObjectNotFoundException e) {
            throw new PageNotFoundException(e);
        }
    }

    public SearchResult<Page> searchPages(final SearchOptions searchOptions) throws SearchException {
        final SearchPages searchPages = getSearchPages(searchOptions);
        try {
            searchPages.execute();
            return searchPages.getResult();
        } catch (final SBonitaException sbe) {
            throw new SearchException(sbe);
        }
    }

    protected SearchPages getSearchPages(final SearchOptions searchOptions) {
        return new SearchPages(pageService, searchEntitiesDescriptor.getSearchPageDescriptor(), searchOptions);
    }

    public Page createPage(final PageCreator pageCreator, final byte[] content) throws AlreadyExistsException, CreationException, InvalidPageTokenException,
            InvalidPageZipContentException {
        final SPage sPage = constructPage(pageCreator, userIdFromSession);

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

    public Page createPage(final String contentName, final byte[] content) throws AlreadyExistsException, CreationException, InvalidPageTokenException,
            InvalidPageZipContentException {
        try {
            return convertToPage(pageService.addPage(content, contentName, userIdFromSession));
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

    public void deletePage(final long pageId) throws DeletionException {
        try {
            final SPage page = pageService.getPage(pageId);
            pageService.deletePage(pageId);
            updatePageMappings(pageId);
            final Long processDefinitionId = page.getProcessDefinitionId();
            if (processDefinitionId != null) {
                updateProcessResolution(processDefinitionId);
            }
        } catch (final SBonitaException e) {
            throw new DeletionException(e);
        }
    }

    protected void updatePageMappings(long pageId) throws SBonitaReadException, SObjectModificationException, SObjectNotFoundException {
        List<SFormMapping> formMappings;
        QueryOptions queryOptions = new QueryOptions(0, 20, Collections.singletonList(new OrderByOption(SFormMapping.class,
                FormMappingSearchDescriptor.ID, OrderByType.ASC)), Arrays.asList(new FilterOption(SPageMapping.class,
                FormMappingSearchDescriptor.PAGE_ID, pageId)), null);
        do {
            formMappings = formMappingService.searchFormMappings(queryOptions);
            for (SFormMapping formMapping : formMappings) {
                pageMappingService.update(formMapping.getPageMapping(), null);
            }
            queryOptions = QueryOptions.getNextPage(queryOptions);
        } while (!formMappings.isEmpty());

    }

    private void updateProcessResolution(Long processDefinitionId) {
        dependencyResolver.resolveDependencies(processDefinitionId, tenantAccessor);
    }

    public void deletePages(final List<Long> pageIds) throws DeletionException {
        for (final Long pageId : pageIds) {
            deletePage(pageId);
        }
    }

    public Page getPageByName(final String name) throws PageNotFoundException {
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

    public Page updatePage(final long pageId, final PageUpdater pageUpdater) throws UpdateException, AlreadyExistsException,
            UpdatingWithInvalidPageTokenException, UpdatingWithInvalidPageZipContentException {
        if (pageUpdater == null || pageUpdater.getFields().isEmpty()) {
            throw new UpdateException("The pageUpdater descriptor does not contain field updates");
        }
        final SPage sPage = constructPage(pageUpdater, userIdFromSession);
        final SPageUpdateBuilder pageUpdateBuilder = getPageUpdateBuilder();
        final Map<PageUpdater.PageUpdateField, Serializable> fields = pageUpdater.getFields();
        for (final Entry<PageUpdater.PageUpdateField, Serializable> field : fields.entrySet()) {
            switch (field.getKey()) {
                case NAME:
                    pageUpdateBuilder.updateName(sPage.getName());
                    break;
                case DISPLAY_NAME:
                    pageUpdateBuilder.updateDisplayName(sPage.getDisplayName());
                    break;
                case DESCRIPTION:
                    pageUpdateBuilder.updateDescription(sPage.getDescription());
                    break;
                case CONTENT_NAME:
                    pageUpdateBuilder.updateContentName(sPage.getContentName());
                    break;
                case CONTENT_TYPE:
                    pageUpdateBuilder.updateContentType(sPage.getContentType());
                    break;
                case PROCESS_DEFINITION_ID:
                    pageUpdateBuilder.updateProcessDefinitionId(sPage.getProcessDefinitionId());
                    break;
                default:
                    break;
            }
        }
        pageUpdateBuilder.updateLastModificationDate(System.currentTimeMillis());
        pageUpdateBuilder.updateLastUpdatedBy(userIdFromSession);

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

    public void updatePageContent(final long pageId, final byte[] content) throws UpdateException, UpdatingWithInvalidPageTokenException,
            UpdatingWithInvalidPageZipContentException {
        final SPageUpdateBuilder pageUpdateBuilder = getPageUpdateBuilder();
        pageUpdateBuilder.updateLastModificationDate(System.currentTimeMillis());
        pageUpdateBuilder.updateLastUpdatedBy(userIdFromSession);
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

    public Properties getPageProperties(final byte[] content, final boolean checkIfItAlreadyExists) throws InvalidPageTokenException, AlreadyExistsException,
            InvalidPageZipMissingPropertiesException, InvalidPageZipMissingIndexException, InvalidPageZipInconsistentException,
            InvalidPageZipMissingAPropertyException {
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

    protected SPageUpdateBuilder getPageUpdateBuilder() {
        return BuilderFactory.get(SPageUpdateBuilderFactory.class).createNewInstance(new EntityUpdateDescriptor());
    }

    protected Page convertToPage(final SPage addPage) {
        return new PageModelConverter().toPage(addPage);
    }

    protected SPage constructPage(final PageCreator pageCreator, final long userId) {
        return new PageModelConverter().constructSPage(pageCreator, userId);
    }

    protected SPageUpdateContentBuilder getPageUpdateContentBuilder() {
        return BuilderFactory.get(SPageUpdateContentBuilderFactory.class)
                .createNewInstance(new EntityUpdateDescriptor());
    }

    protected SPage constructPage(final PageUpdater pageUpdater, final long userId) {
        return new PageModelConverter().constructSPage(pageUpdater, userId);
    }

    private Properties getProperties(final byte[] content, final boolean checkIfItAlreadyExists, final PageService pageService)
            throws SInvalidPageZipMissingIndexException,
            SInvalidPageZipMissingAPropertyException, SInvalidPageZipInconsistentException, SInvalidPageZipMissingPropertiesException,
            SInvalidPageTokenException, SBonitaReadException, AlreadyExistsException {
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

    public Page getPageByNameAndProcessDefinition(String name, long processDefinitionId) throws PageNotFoundException {
        try {
            final SPage sPage = pageService.getPageByNameAndProcessDefinitionId(name, processDefinitionId);
            if (sPage == null) {
                throw new PageNotFoundException(name);
            }
            return convertToPage(sPage);
        } catch (final SBonitaReadException e) {
            throw new PageNotFoundException(e);
        }
    }
}
