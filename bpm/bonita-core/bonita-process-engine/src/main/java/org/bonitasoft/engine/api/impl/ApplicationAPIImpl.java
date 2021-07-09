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

import static org.bonitasoft.engine.business.application.ApplicationSearchDescriptor.USER_ID;
import static org.bonitasoft.engine.business.application.InternalProfiles.INTERNAL_PROFILE_SUPER_ADMIN;
import static org.bonitasoft.engine.search.descriptor.SearchApplicationDescriptor.APPLICATION_VISIBILITY;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bonitasoft.engine.api.ApplicationAPI;
import org.bonitasoft.engine.api.ImportStatus;
import org.bonitasoft.engine.api.impl.converter.ApplicationMenuModelConverter;
import org.bonitasoft.engine.api.impl.converter.ApplicationModelConverter;
import org.bonitasoft.engine.api.impl.converter.ApplicationPageModelConverter;
import org.bonitasoft.engine.api.impl.livingapplication.LivingApplicationAPIDelegate;
import org.bonitasoft.engine.api.impl.livingapplication.LivingApplicationExporterDelegate;
import org.bonitasoft.engine.api.impl.livingapplication.LivingApplicationMenuAPIDelegate;
import org.bonitasoft.engine.api.impl.livingapplication.LivingApplicationPageAPIDelegate;
import org.bonitasoft.engine.api.impl.transaction.application.SearchApplicationMenus;
import org.bonitasoft.engine.api.impl.transaction.application.SearchApplicationPages;
import org.bonitasoft.engine.api.impl.transaction.application.SearchApplications;
import org.bonitasoft.engine.api.impl.transaction.application.SearchApplicationsOfUser;
import org.bonitasoft.engine.business.application.Application;
import org.bonitasoft.engine.business.application.ApplicationCreator;
import org.bonitasoft.engine.business.application.ApplicationImportPolicy;
import org.bonitasoft.engine.business.application.ApplicationMenu;
import org.bonitasoft.engine.business.application.ApplicationMenuCreator;
import org.bonitasoft.engine.business.application.ApplicationMenuNotFoundException;
import org.bonitasoft.engine.business.application.ApplicationMenuUpdater;
import org.bonitasoft.engine.business.application.ApplicationNotFoundException;
import org.bonitasoft.engine.business.application.ApplicationPage;
import org.bonitasoft.engine.business.application.ApplicationPageNotFoundException;
import org.bonitasoft.engine.business.application.ApplicationService;
import org.bonitasoft.engine.business.application.ApplicationUpdater;
import org.bonitasoft.engine.business.application.Icon;
import org.bonitasoft.engine.business.application.converter.ApplicationMenuToNodeConverter;
import org.bonitasoft.engine.business.application.converter.ApplicationPageToNodeConverter;
import org.bonitasoft.engine.business.application.converter.ApplicationToNodeConverter;
import org.bonitasoft.engine.business.application.converter.ApplicationsToNodeContainerConverter;
import org.bonitasoft.engine.business.application.converter.NodeToApplicationConverter;
import org.bonitasoft.engine.business.application.exporter.ApplicationContainerExporter;
import org.bonitasoft.engine.business.application.exporter.ApplicationExporter;
import org.bonitasoft.engine.business.application.importer.ApplicationImporter;
import org.bonitasoft.engine.business.application.importer.StrategySelector;
import org.bonitasoft.engine.business.application.importer.validator.ApplicationImportValidator;
import org.bonitasoft.engine.business.application.importer.validator.ApplicationMenuCreatorValidator;
import org.bonitasoft.engine.business.application.importer.validator.ApplicationTokenValidator;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.ExportException;
import org.bonitasoft.engine.exception.ImportException;
import org.bonitasoft.engine.exception.NotFoundException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.page.PageService;
import org.bonitasoft.engine.profile.ProfileService;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.search.descriptor.SearchApplicationDescriptor;
import org.bonitasoft.engine.search.descriptor.SearchApplicationMenuDescriptor;
import org.bonitasoft.engine.search.descriptor.SearchApplicationPageDescriptor;
import org.bonitasoft.engine.search.impl.SearchFilter;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceSingleton;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;

/**
 * @author Elias Ricken de Medeiros
 */
@AvailableWhenTenantIsPaused
public class ApplicationAPIImpl implements ApplicationAPI {

    @Override
    public Application createApplication(final ApplicationCreator applicationCreator)
            throws CreationException {
        return getLivingApplicationAPIDelegate().createApplication(applicationCreator);
    }

    private LivingApplicationAPIDelegate getLivingApplicationAPIDelegate() {
        return new LivingApplicationAPIDelegate(getTenantAccessor(),
                getApplicationModelConverter(getTenantAccessor().getPageService()),
                SessionInfos.getUserIdFromSession(), new ApplicationTokenValidator());
    }

    protected ApplicationModelConverter getApplicationModelConverter(final PageService pageService) {
        return new ApplicationModelConverter(pageService);
    }

    private LivingApplicationPageAPIDelegate getApplicationPageAPIDelegate() {
        return new LivingApplicationPageAPIDelegate(getTenantAccessor(), new ApplicationPageModelConverter(),
                SessionInfos.getUserIdFromSession(),
                new ApplicationTokenValidator());
    }

    private LivingApplicationMenuAPIDelegate getApplicationMenuAPIDelegate() {
        return new LivingApplicationMenuAPIDelegate(getTenantAccessor(), new ApplicationMenuModelConverter(),
                new ApplicationMenuCreatorValidator(),
                SessionInfos.getUserIdFromSession());
    }

    private LivingApplicationExporterDelegate getLivingApplicationExporterDelegate() {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ApplicationService applicationService = tenantAccessor.getApplicationService();
        final PageService pageService = tenantAccessor.getPageService();
        ApplicationToNodeConverter applicationToNodeConverter = new ApplicationToNodeConverter(
                tenantAccessor.getProfileService(), applicationService,
                new ApplicationPageToNodeConverter(pageService), new ApplicationMenuToNodeConverter(applicationService),
                pageService);
        final ApplicationsToNodeContainerConverter applicationsToNodeContainerConverter = new ApplicationsToNodeContainerConverter(
                applicationToNodeConverter);
        final ApplicationContainerExporter applicationContainerExporter = new ApplicationContainerExporter();
        final ApplicationExporter applicationExporter = new ApplicationExporter(applicationsToNodeContainerConverter,
                applicationContainerExporter);
        return new LivingApplicationExporterDelegate(tenantAccessor.getApplicationService(), applicationExporter);
    }

    protected NodeToApplicationConverter getNodeToApplicationConverter(final PageService pageService,
            final ProfileService profileService, final ApplicationImportValidator importValidator) {
        return new NodeToApplicationConverter(profileService, pageService, importValidator);

    }

    @Override
    public Application getApplication(final long applicationId) throws ApplicationNotFoundException {
        return getLivingApplicationAPIDelegate().getApplication(applicationId);
    }

    @Override
    public Application getApplicationByToken(final String applicationToken) throws ApplicationNotFoundException {
        return getLivingApplicationAPIDelegate().getApplicationByToken(applicationToken);
    }

    @Override
    public void deleteApplication(final long applicationId) throws DeletionException {
        getLivingApplicationAPIDelegate().deleteApplication(applicationId);
    }

    @Override
    public Application updateApplication(final long applicationId, final ApplicationUpdater updater)
            throws ApplicationNotFoundException, UpdateException,
            AlreadyExistsException {
        return getLivingApplicationAPIDelegate().updateApplication(applicationId, updater);
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
    public SearchResult<Application> searchApplications(final SearchOptions searchOptions) throws SearchException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final SearchApplicationDescriptor appSearchDescriptor = tenantAccessor.getSearchEntitiesDescriptor()
                .getSearchApplicationDescriptor();
        final ApplicationModelConverter converter = getApplicationModelConverter(tenantAccessor.getPageService());
        final ApplicationService applicationService = tenantAccessor.getApplicationService();
        final Optional<SearchFilter> filterOnUserId = searchOptions.getFilters().stream()
                .filter(s -> s.getField().equals(USER_ID)).findFirst();
        if (filterOnUserId.isPresent()) {
            final SearchOptionsBuilder searchOptionsWithoutUserId = new SearchOptionsBuilder(searchOptions)
                    .setFilters(searchOptions.getFilters().stream().filter(s -> !s.getField().equals(USER_ID))
                            .collect(Collectors.toList()));
            final String userId = String.valueOf(filterOnUserId.get().getValue());
            if (String.valueOf(SessionService.SYSTEM_ID).equals(userId)) { // It's the tenant admin user
                return getLivingApplicationAPIDelegate().searchApplications(new SearchApplications(
                        applicationService, appSearchDescriptor,
                        searchOptionsWithoutUserId
                                .filter(APPLICATION_VISIBILITY, INTERNAL_PROFILE_SUPER_ADMIN.getProfileName()).done(),
                        converter));
            } else {
                return getLivingApplicationAPIDelegate().searchApplications(new SearchApplicationsOfUser(
                        Long.parseLong(userId), applicationService, appSearchDescriptor,
                        searchOptionsWithoutUserId.done(), converter));
            }
        }
        return getLivingApplicationAPIDelegate()
                .searchApplications(new SearchApplications(applicationService, appSearchDescriptor,
                        searchOptions, converter));
    }

    @Override
    public ApplicationPage createApplicationPage(final long applicationId, final long pageId, final String token)
            throws CreationException {
        return getApplicationPageAPIDelegate().createApplicationPage(applicationId, pageId, token);
    }

    @Override
    public ApplicationPage getApplicationPage(final String applicationName, final String applicationPageToken)
            throws ApplicationPageNotFoundException {
        return getApplicationPageAPIDelegate().getApplicationPage(applicationName, applicationPageToken);
    }

    @Override
    public SearchResult<ApplicationPage> searchApplicationPages(final SearchOptions searchOptions)
            throws SearchException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final SearchApplicationPageDescriptor appPageSearchDescriptor = tenantAccessor.getSearchEntitiesDescriptor()
                .getSearchApplicationPageDescriptor();
        final ApplicationPageModelConverter converter = new ApplicationPageModelConverter();
        final ApplicationService applicationService = tenantAccessor.getApplicationService();
        final SearchApplicationPages searchApplicationPages = new SearchApplicationPages(applicationService, converter,
                appPageSearchDescriptor, searchOptions);
        return getApplicationPageAPIDelegate().searchApplicationPages(searchApplicationPages);
    }

    @Override
    public ApplicationPage getApplicationPage(final long applicationPageId) throws ApplicationPageNotFoundException {
        return getApplicationPageAPIDelegate().getApplicationPage(applicationPageId);
    }

    @Override
    public void deleteApplicationPage(final long applicationPageId) throws DeletionException {
        getApplicationPageAPIDelegate().deleteApplicationPage(applicationPageId);
    }

    @Override
    public void setApplicationHomePage(final long applicationId, final long applicationPageId)
            throws UpdateException, ApplicationNotFoundException {
        getApplicationPageAPIDelegate().setApplicationHomePage(applicationId, applicationPageId);
    }

    @Override
    public ApplicationPage getApplicationHomePage(final long applicationId) throws ApplicationPageNotFoundException {
        return getApplicationPageAPIDelegate().getApplicationHomePage(applicationId);
    }

    @Override
    public ApplicationMenu createApplicationMenu(final ApplicationMenuCreator applicationMenuCreator)
            throws CreationException {
        return getApplicationMenuAPIDelegate().createApplicationMenu(applicationMenuCreator);
    }

    @Override
    public ApplicationMenu updateApplicationMenu(final long applicationMenuId, final ApplicationMenuUpdater updater)
            throws ApplicationMenuNotFoundException,
            UpdateException {
        return getApplicationMenuAPIDelegate().updateApplicationMenu(applicationMenuId, updater);
    }

    @Override
    public ApplicationMenu getApplicationMenu(final long applicationMenuId) throws ApplicationMenuNotFoundException {
        return getApplicationMenuAPIDelegate().getApplicationMenu(applicationMenuId);
    }

    @Override
    public void deleteApplicationMenu(final long applicationMenuId) throws DeletionException {
        getApplicationMenuAPIDelegate().deleteApplicationMenu(applicationMenuId);
    }

    @Override
    public SearchResult<ApplicationMenu> searchApplicationMenus(final SearchOptions searchOptions)
            throws SearchException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ApplicationService applicationService = tenantAccessor.getApplicationService();
        final ApplicationMenuModelConverter converter = new ApplicationMenuModelConverter();
        final SearchApplicationMenuDescriptor searchDescriptor = tenantAccessor.getSearchEntitiesDescriptor()
                .getSearchApplicationMenuDescriptor();
        final SearchApplicationMenus searchApplicationMenus = new SearchApplicationMenus(applicationService, converter,
                searchDescriptor, searchOptions);
        return getApplicationMenuAPIDelegate().searchApplicationMenus(searchApplicationMenus);
    }

    @Override
    public List<String> getAllPagesForProfile(final long profileId) {
        return getApplicationPageAPIDelegate().getAllPagesForProfile(profileId);
    }

    @Override
    public List<String> getAllPagesForProfile(String profile) {
        return getApplicationPageAPIDelegate().getAllPagesForProfile(profile);
    }

    @Override
    public byte[] exportApplications(final long... applicationIds) throws ExportException {
        return getLivingApplicationExporterDelegate().exportApplications(applicationIds);
    }

    @Override
    public List<ImportStatus> importApplications(final byte[] xmlContent, final ApplicationImportPolicy policy)
            throws ImportException, AlreadyExistsException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        ApplicationImporter applicationImporter;
        try {
            applicationImporter = tenantAccessor.lookup(ApplicationImporter.class);
        } catch (NotFoundException e) {
            throw new IllegalStateException(e);
        }
        return applicationImporter.importApplications(xmlContent, null, null, SessionInfos.getUserIdFromSession(),
                new StrategySelector().selectStrategy(policy));
    }

    @Override
    public Icon getIconOfApplication(long applicationId) throws ApplicationNotFoundException {
        return getLivingApplicationAPIDelegate().getIconOfApplication(applicationId);
    }
}
