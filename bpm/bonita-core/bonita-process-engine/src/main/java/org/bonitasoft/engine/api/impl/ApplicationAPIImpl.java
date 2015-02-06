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

import org.bonitasoft.engine.api.ApplicationAPI;
import org.bonitasoft.engine.api.ImportStatus;
import org.bonitasoft.engine.api.impl.application.ApplicationAPIDelegate;
import org.bonitasoft.engine.api.impl.application.ApplicationExporterDelegate;
import org.bonitasoft.engine.api.impl.application.ApplicationMenuAPIDelegate;
import org.bonitasoft.engine.api.impl.application.ApplicationPageAPIDelegate;
import org.bonitasoft.engine.api.impl.converter.ApplicationModelConverter;
import org.bonitasoft.engine.api.impl.converter.ApplicationMenuModelConverter;
import org.bonitasoft.engine.api.impl.converter.ApplicationPageModelConverter;
import org.bonitasoft.engine.api.impl.transaction.application.SearchApplicationMenus;
import org.bonitasoft.engine.api.impl.transaction.application.SearchApplicationPages;
import org.bonitasoft.engine.api.impl.transaction.application.SearchApplications;
import org.bonitasoft.engine.api.impl.validator.ApplicationMenuCreatorValidator;
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
import org.bonitasoft.engine.business.application.converter.ApplicationContainerConverter;
import org.bonitasoft.engine.business.application.converter.ApplicationMenuNodeConverter;
import org.bonitasoft.engine.business.application.converter.ApplicationNodeConverter;
import org.bonitasoft.engine.business.application.converter.ApplicationPageNodeConverter;
import org.bonitasoft.engine.business.application.exporter.ApplicationContainerExporter;
import org.bonitasoft.engine.business.application.exporter.ApplicationExporter;
import org.bonitasoft.engine.business.application.importer.ApplicationContainerImporter;
import org.bonitasoft.engine.business.application.importer.ApplicationImporter;
import org.bonitasoft.engine.business.application.importer.ApplicationMenuImporter;
import org.bonitasoft.engine.business.application.importer.ApplicationPageImporter;
import org.bonitasoft.engine.business.application.importer.ApplicationsImporter;
import org.bonitasoft.engine.business.application.importer.StrategySelector;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.ExportException;
import org.bonitasoft.engine.exception.ImportException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.search.descriptor.SearchApplicationDescriptor;
import org.bonitasoft.engine.search.descriptor.SearchApplicationMenuDescriptor;
import org.bonitasoft.engine.search.descriptor.SearchApplicationPageDescriptor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceSingleton;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;

/**
 * @author Elias Ricken de Medeiros
 */
public class ApplicationAPIImpl implements ApplicationAPI {

    @Override
    public Application createApplication(final ApplicationCreator applicationCreator) throws AlreadyExistsException, CreationException {
        return getApplicationAPIDelegate().createApplication(applicationCreator);
    }

    private ApplicationAPIDelegate getApplicationAPIDelegate() {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ApplicationModelConverter converter = new ApplicationModelConverter();
        final ApplicationAPIDelegate delegate = new ApplicationAPIDelegate(tenantAccessor, converter,
                SessionInfos.getUserIdFromSession());
        return delegate;
    }

    private ApplicationPageAPIDelegate getApplicationPageAPIDelegate() {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ApplicationPageModelConverter converter = new ApplicationPageModelConverter();
        return new ApplicationPageAPIDelegate(tenantAccessor, converter, SessionInfos.getUserIdFromSession());
    }

    private ApplicationMenuAPIDelegate getApplicationMenuAPIDelegate() {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ApplicationMenuModelConverter converter = new ApplicationMenuModelConverter();
        final ApplicationMenuCreatorValidator validator = new ApplicationMenuCreatorValidator();
        final ApplicationMenuAPIDelegate delegate = new ApplicationMenuAPIDelegate(tenantAccessor, converter, validator,
                SessionInfos.getUserIdFromSession());
        return delegate;
    }

    private ApplicationExporterDelegate getApplicationExporterDelegate() {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ApplicationService applicationService = tenantAccessor.getApplicationService();
        final ApplicationNodeConverter applicationNodeConverter = new ApplicationNodeConverter(tenantAccessor.getProfileService(),
                applicationService, new ApplicationPageNodeConverter(tenantAccessor.getPageService()), new ApplicationMenuNodeConverter(applicationService));
        final ApplicationContainerConverter applicationContainerConverter = new ApplicationContainerConverter(applicationNodeConverter);
        final ApplicationContainerExporter applicationContainerExporter = new ApplicationContainerExporter();
        final ApplicationExporter applicationExporter = new ApplicationExporter(applicationContainerConverter, applicationContainerExporter);
        return new ApplicationExporterDelegate(tenantAccessor.getApplicationService(), applicationExporter);
    }

    private ApplicationsImporter getApplicationImporter(final ApplicationImportPolicy policy) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final StrategySelector strategySelector = new StrategySelector();
        final ApplicationContainerImporter containerImporter = new ApplicationContainerImporter();
        final ApplicationService applicationService = tenantAccessor.getApplicationService();
        final ApplicationPageNodeConverter applicationPageNodeConverter = new ApplicationPageNodeConverter(tenantAccessor.getPageService());
        final ApplicationMenuNodeConverter applicationMenuNodeConverter = new ApplicationMenuNodeConverter(applicationService);
        final ApplicationNodeConverter applicationNodeConverter = new ApplicationNodeConverter(tenantAccessor.getProfileService(),
                applicationService, applicationPageNodeConverter, applicationMenuNodeConverter);
        final ApplicationPageImporter applicationPageImporter = new ApplicationPageImporter(tenantAccessor.getApplicationService(),
                applicationPageNodeConverter);
        final ApplicationMenuImporter applicationMenuImporter = new ApplicationMenuImporter(tenantAccessor.getApplicationService(),
                applicationMenuNodeConverter);
        final ApplicationImporter applicationImporter = new ApplicationImporter(tenantAccessor.getApplicationService(),
                strategySelector.selectStrategy(policy), applicationNodeConverter, applicationPageImporter, applicationMenuImporter);
        return new ApplicationsImporter(containerImporter, applicationImporter);
    }

    @Override
    public Application getApplication(final long applicationId) throws ApplicationNotFoundException {
        return getApplicationAPIDelegate().getApplication(applicationId);
    }

    @Override
    public void deleteApplication(final long applicationId) throws DeletionException {
        getApplicationAPIDelegate().deleteApplication(applicationId);
    }

    @Override
    public Application updateApplication(final long applicationId, final ApplicationUpdater updater) throws ApplicationNotFoundException, UpdateException,
            AlreadyExistsException {
        return getApplicationAPIDelegate().updateApplication(applicationId, updater);
    }

    private TenantServiceAccessor getTenantAccessor() {
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
        final SearchApplicationDescriptor appSearchDescriptor = tenantAccessor.getSearchEntitiesDescriptor().getSearchApplicationDescriptor();
        final ApplicationModelConverter converter = new ApplicationModelConverter();
        final ApplicationService applicationService = tenantAccessor.getApplicationService();
        final SearchApplications searchApplications = new SearchApplications(applicationService, appSearchDescriptor, searchOptions, converter);
        return getApplicationAPIDelegate().searchApplications(searchApplications);
    }

    @Override
    public ApplicationPage createApplicationPage(final long applicationId, final long pageId, final String token) throws AlreadyExistsException,
            CreationException {
        return getApplicationPageAPIDelegate().createApplicationPage(applicationId, pageId, token);
    }

    @Override
    public ApplicationPage getApplicationPage(final String applicationName, final String applicationPageToken) throws ApplicationPageNotFoundException {
        return getApplicationPageAPIDelegate().getApplicationPage(applicationName, applicationPageToken);
    }

    @Override
    public SearchResult<ApplicationPage> searchApplicationPages(final SearchOptions searchOptions) throws SearchException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final SearchApplicationPageDescriptor appPageSearchDescriptor = tenantAccessor.getSearchEntitiesDescriptor().getSearchApplicationPageDescriptor();
        final ApplicationPageModelConverter converter = new ApplicationPageModelConverter();
        final ApplicationService applicationService = tenantAccessor.getApplicationService();
        final SearchApplicationPages searchApplicationPages = new SearchApplicationPages(applicationService, converter, appPageSearchDescriptor, searchOptions);
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
    public void setApplicationHomePage(final long applicationId, final long applicationPageId) throws UpdateException, ApplicationNotFoundException {
        getApplicationPageAPIDelegate().setApplicationHomePage(applicationId, applicationPageId);
    }

    @Override
    public ApplicationPage getApplicationHomePage(final long applicationId) throws ApplicationPageNotFoundException {
        return getApplicationPageAPIDelegate().getApplicationHomePage(applicationId);
    }

    @Override
    public ApplicationMenu createApplicationMenu(final ApplicationMenuCreator applicationMenuCreator) throws CreationException {
        return getApplicationMenuAPIDelegate().createApplicationMenu(applicationMenuCreator);
    }

    @Override
    public ApplicationMenu updateApplicationMenu(final long applicationMenuId, final ApplicationMenuUpdater updater) throws ApplicationMenuNotFoundException,
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
    public SearchResult<ApplicationMenu> searchApplicationMenus(final SearchOptions searchOptions) throws SearchException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ApplicationService applicationService = tenantAccessor.getApplicationService();
        final ApplicationMenuModelConverter converter = new ApplicationMenuModelConverter();
        final SearchApplicationMenuDescriptor searchDescriptor = tenantAccessor.getSearchEntitiesDescriptor().getSearchApplicationMenuDescriptor();
        final SearchApplicationMenus searchApplicationMenus = new SearchApplicationMenus(applicationService, converter, searchDescriptor, searchOptions);
        return getApplicationMenuAPIDelegate().searchApplicationMenus(searchApplicationMenus);
    }

    @Override
    public List<String> getAllPagesForProfile(final long profileId) {
        return getApplicationPageAPIDelegate().getAllPagesForProfile(profileId);
    }

    @Override
    public byte[] exportApplications(final long... applicationIds) throws ExportException {
        return getApplicationExporterDelegate().exportApplications(applicationIds);
    }

    @Override
    public List<ImportStatus> importApplications(final byte[] xmlContent, final ApplicationImportPolicy policy) throws ImportException, AlreadyExistsException {
        return getApplicationImporter(policy).importApplications(xmlContent, SessionInfos.getUserIdFromSession());
    }

}
