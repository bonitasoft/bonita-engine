/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/

package com.bonitasoft.engine.business.application.converter;

import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.api.ImportError;
import org.bonitasoft.engine.api.ImportStatus;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.exception.ExecutionException;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.profile.ProfileService;
import org.bonitasoft.engine.profile.exception.profile.SProfileNotFoundException;
import org.bonitasoft.engine.profile.model.SProfile;

import com.bonitasoft.engine.business.application.ApplicationService;
import com.bonitasoft.engine.business.application.importer.ImportResult;
import com.bonitasoft.engine.business.application.model.SApplication;
import com.bonitasoft.engine.business.application.model.SApplicationPage;
import com.bonitasoft.engine.business.application.model.builder.SApplicationBuilder;
import com.bonitasoft.engine.business.application.model.builder.SApplicationBuilderFactory;
import com.bonitasoft.engine.business.application.model.builder.SApplicationPageBuilderFactory;
import com.bonitasoft.engine.business.application.xml.ApplicationNode;

/**
 * @author Elias Ricken de Medeiros
 */
public class ApplicationNodeConverter {

    private final ProfileService profileService;
    private final ApplicationService applicationService;
    private final ApplicationPageNodeConverter applicationPageNodeConverter;
    private final ApplicationMenuNodeConverter applicationMenuNodeConverter;

    public ApplicationNodeConverter(final ProfileService profileService, final ApplicationService applicationService,
            final ApplicationPageNodeConverter applicationPageNodeConverter, final ApplicationMenuNodeConverter applicationMenuNodeConverter) {
        this.profileService = profileService;
        this.applicationService = applicationService;
        this.applicationPageNodeConverter = applicationPageNodeConverter;
        this.applicationMenuNodeConverter = applicationMenuNodeConverter;
    }

    public ApplicationNode toNode(final SApplication application) throws ExecutionException {
        final ApplicationNode applicationNode = new ApplicationNode();
        applicationNode.setToken(application.getToken());
        applicationNode.setDisplayName(application.getDisplayName());
        applicationNode.setVersion(application.getVersion());
        applicationNode.setDescription(application.getDescription());
        applicationNode.setState(application.getState());
        applicationNode.setIconPath(application.getIconPath());
        setProfile(application, applicationNode);
        setHomePage(application, applicationNode);
        setPages(application.getId(), applicationNode);
        setMenus(application.getId(), applicationNode);
        return applicationNode;
    }

    private void setMenus(final long applicationId, final ApplicationNode applicationNode) throws ExecutionException {
        try {
            applicationMenuNodeConverter.addMenusToApplicationNode(applicationId, null, applicationNode, null);
        } catch (final SBonitaException e) {
            throw new ExecutionException(e);
        }
    }

    private void setPages(final long applicationId, final ApplicationNode applicationNode) throws ExecutionException {
        try {
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
        } catch (final SBonitaException e) {
            throw new ExecutionException(e);
        }
    }

    public QueryOptions buildApplicationPagesQueryOptions(final long applicationId, final int startIndex, final int pageSize) {
        final SApplicationPageBuilderFactory factory = BuilderFactory.get(SApplicationPageBuilderFactory.class);
        final List<OrderByOption> orderByOptions = Collections.singletonList(new OrderByOption(SApplicationPage.class, factory.getIdKey(), OrderByType.ASC));
        final List<FilterOption> filters = Collections.singletonList(new FilterOption(SApplicationPage.class, factory.getApplicationIdKey(), applicationId));
        return new QueryOptions(startIndex, pageSize, orderByOptions, filters, null);
    }

    private void setHomePage(final SApplication application, final ApplicationNode applicationNode) throws ExecutionException {
        if (application.getHomePageId() != null) {
            try {
                final SApplicationPage homePage = applicationService.getApplicationPage(application.getHomePageId());
                applicationNode.setHomePage(homePage.getToken());
            } catch (final SBonitaException e) {
                throw new ExecutionException(e);
            }
        }
    }

    private void setProfile(final SApplication application, final ApplicationNode applicationNode) throws ExecutionException {
        try {
            if (application.getProfileId() != null) {
                final SProfile profile = profileService.getProfile(application.getProfileId());
                applicationNode.setProfile(profile.getName());
            }
        } catch (final SBonitaException e) {
            throw new ExecutionException(e);
        }
    }

    public ImportResult toSApplication(final ApplicationNode applicationNode, final long createdBy) throws ExecutionException {
        final ImportStatus importStatus = new ImportStatus(applicationNode.getToken());

        final SApplicationBuilder builder = BuilderFactory.get(SApplicationBuilderFactory.class).createNewInstance(applicationNode.getToken(),
                applicationNode.getDisplayName(), applicationNode.getVersion(), createdBy);
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

    private ImportError setProfile(final ApplicationNode applicationNode, final SApplicationBuilder builder) throws ExecutionException {
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
