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
package org.bonitasoft.engine.business.application.converter;

import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.api.ImportError;
import org.bonitasoft.engine.api.ImportStatus;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.business.application.ApplicationService;
import org.bonitasoft.engine.business.application.importer.ImportResult;
import org.bonitasoft.engine.business.application.model.SApplication;
import org.bonitasoft.engine.business.application.model.SApplicationPage;
import org.bonitasoft.engine.business.application.model.builder.SApplicationBuilder;
import org.bonitasoft.engine.business.application.model.builder.SApplicationBuilderFactory;
import org.bonitasoft.engine.business.application.model.builder.SApplicationPageBuilderFactory;
import org.bonitasoft.engine.business.application.xml.ApplicationNode;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.exception.ExportException;
import org.bonitasoft.engine.exception.ImportException;
import org.bonitasoft.engine.page.PageService;
import org.bonitasoft.engine.page.SPage;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.profile.ProfileService;
import org.bonitasoft.engine.profile.exception.profile.SProfileNotFoundException;
import org.bonitasoft.engine.profile.model.SProfile;

/**
 * @author Elias Ricken de Medeiros
 */
public class ApplicationNodeConverter {

    private final ProfileService profileService;
    private final ApplicationService applicationService;
    private final ApplicationPageNodeConverter applicationPageNodeConverter;
    private final ApplicationMenuNodeConverter applicationMenuNodeConverter;
    private final PageService pageService;

    public ApplicationNodeConverter(final ProfileService profileService, final ApplicationService applicationService,
            final ApplicationPageNodeConverter applicationPageNodeConverter, final ApplicationMenuNodeConverter applicationMenuNodeConverter,
            final PageService pageService) {
        this.profileService = profileService;
        this.applicationService = applicationService;
        this.applicationPageNodeConverter = applicationPageNodeConverter;
        this.applicationMenuNodeConverter = applicationMenuNodeConverter;
        this.pageService = pageService;
    }

    public ApplicationNode toNode(final SApplication application) throws ExportException {
        try {
            final ApplicationNode applicationNode = new ApplicationNode();
            applicationNode.setToken(application.getToken());
            applicationNode.setDisplayName(application.getDisplayName());
            applicationNode.setVersion(application.getVersion());
            applicationNode.setDescription(application.getDescription());
            applicationNode.setState(application.getState());
            applicationNode.setIconPath(application.getIconPath());
            setLayout(application, applicationNode);
            setProfile(application, applicationNode);
            setHomePage(application, applicationNode);
            setPages(application.getId(), applicationNode);
            applicationMenuNodeConverter.addMenusToApplicationNode(application.getId(), null, applicationNode, null);
            return applicationNode;
        } catch (SBonitaException e) {
            throw new ExportException(e);
        }
    }

    private void setLayout(final SApplication application, final ApplicationNode applicationNode) throws SBonitaReadException, SObjectNotFoundException {
        if (application.getLayoutId() != null) {
            SPage page = pageService.getPage(application.getLayoutId());
            applicationNode.setLayout(page.getName());
        }
    }

    private void setPages(final long applicationId, final ApplicationNode applicationNode) throws SBonitaReadException, SObjectNotFoundException {
        int startIndex = 0;
        final int maxResults = 50;
        List<SApplicationPage> pages;
        do {
            pages = applicationService.searchApplicationPages(buildApplicationPagesQueryOptions(applicationId, startIndex, maxResults));
            for (final SApplicationPage page : pages) {
                applicationNode.addApplicationPage(applicationPageNodeConverter.toPage(page));
            }
            startIndex += maxResults;
        } while (pages.size() == maxResults);
    }

    public QueryOptions buildApplicationPagesQueryOptions(final long applicationId, final int startIndex, final int pageSize) {
        final SApplicationPageBuilderFactory factory = BuilderFactory.get(SApplicationPageBuilderFactory.class);
        final List<OrderByOption> orderByOptions = Collections.singletonList(new OrderByOption(SApplicationPage.class, factory.getIdKey(), OrderByType.ASC));
        final List<FilterOption> filters = Collections.singletonList(new FilterOption(SApplicationPage.class, factory.getApplicationIdKey(), applicationId));
        return new QueryOptions(startIndex, pageSize, orderByOptions, filters, null);
    }

    private void setHomePage(final SApplication application, final ApplicationNode applicationNode) throws SBonitaReadException, SObjectNotFoundException {
        if (application.getHomePageId() != null) {
            final SApplicationPage homePage = applicationService.getApplicationPage(application.getHomePageId());
            applicationNode.setHomePage(homePage.getToken());
        }
    }

    private void setProfile(final SApplication application, final ApplicationNode applicationNode) throws SProfileNotFoundException {
        if (application.getProfileId() != null) {
            final SProfile profile = profileService.getProfile(application.getProfileId());
            applicationNode.setProfile(profile.getName());
        }
    }

    public ImportResult toSApplication(final ApplicationNode applicationNode, final long createdBy) throws SBonitaReadException, ImportException {
        final ImportStatus importStatus = new ImportStatus(applicationNode.getToken());

        final SApplicationBuilder builder = BuilderFactory.get(SApplicationBuilderFactory.class).createNewInstance(applicationNode.getToken(),
                applicationNode.getDisplayName(), applicationNode.getVersion(), createdBy, getLayoutId(applicationNode, importStatus), null);
        builder.setIconPath(applicationNode.getIconPath());
        builder.setDescription(applicationNode.getDescription());
        builder.setState(applicationNode.getState());

        final ImportError importError = setProfile(applicationNode, builder);
        if (importError != null) {
            importStatus.addError(importError);
        }

        final SApplication application = builder.done();
        return new ImportResult(application, importStatus);
    }

    private Long getLayoutId(final ApplicationNode applicationNode, final ImportStatus importStatus) throws SBonitaReadException, ImportException {
        SPage layout = pageService.getPageByName(getLayoutName(applicationNode));
        if (layout == null) {
            return handleMissingLayout(applicationNode, importStatus);
        }
        return layout.getId();
    }

    protected Long handleMissingLayout(final ApplicationNode applicationNode, final ImportStatus importStatus) throws ImportException {
        throw new ImportException(String.format("Unable to import application with token '%s' because the default layout '%s' was not found.",
                applicationNode.getToken(), getLayoutName(applicationNode)));
    }

    protected String getLayoutName(final ApplicationNode applicationNode) {
        return ApplicationService.DEFAULT_LAYOUT_NAME;
    }

    private ImportError setProfile(final ApplicationNode applicationNode, final SApplicationBuilder builder) {
        ImportError importError = null;
        if (applicationNode.getProfile() != null) {
            try {
                final SProfile profile = profileService.getProfileByName(applicationNode.getProfile());
                builder.setProfileId(profile.getId());
            } catch (final SProfileNotFoundException e) {
                importError = new ImportError(applicationNode.getProfile(), ImportError.Type.PROFILE);
            }
        }
        return importError;
    }

}
