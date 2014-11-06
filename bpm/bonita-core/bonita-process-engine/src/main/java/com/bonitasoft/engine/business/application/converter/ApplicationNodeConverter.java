/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/

package com.bonitasoft.engine.business.application.converter;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.exception.ExecutionException;
import org.bonitasoft.engine.profile.ProfileService;
import org.bonitasoft.engine.profile.exception.profile.SProfileNotFoundException;
import org.bonitasoft.engine.profile.model.SProfile;

import com.bonitasoft.engine.business.application.ApplicationService;
import com.bonitasoft.engine.business.application.model.SApplication;
import com.bonitasoft.engine.business.application.model.SApplicationPage;
import com.bonitasoft.engine.business.application.model.builder.SApplicationBuilder;
import com.bonitasoft.engine.business.application.model.builder.SApplicationBuilderFactory;
import com.bonitasoft.engine.business.application.xml.ApplicationNode;

/**
 * @author Elias Ricken de Medeiros
 */
public class ApplicationNodeConverter {

    private final ProfileService profileService;
    private final ApplicationService applicationService;

    public ApplicationNodeConverter(ProfileService profileService, ApplicationService applicationService) {
        this.profileService = profileService;
        this.applicationService = applicationService;
    }

    public ApplicationNode toNode(SApplication application) throws ExecutionException {
        ApplicationNode applicationNode = new ApplicationNode();
        applicationNode.setToken(application.getToken());
        applicationNode.setDisplayName(application.getDisplayName());
        applicationNode.setVersion(application.getVersion());
        applicationNode.setDescription(application.getDescription());
        applicationNode.setState(application.getState());
        applicationNode.setIconPath(application.getIconPath());
        setProfile(application, applicationNode);
        setHomePage(application, applicationNode);
        return applicationNode;
    }

    private void setHomePage(SApplication application, ApplicationNode applicationNode) throws ExecutionException {
        if (application.getHomePageId() != null) {
            try {
                SApplicationPage homePage = applicationService.getApplicationPage(application.getHomePageId());
                applicationNode.setHomePage(homePage.getToken());
            } catch (SBonitaException e) {
                throw new ExecutionException(e);
            }
        }
    }

    private void setProfile(SApplication application, ApplicationNode applicationNode) throws ExecutionException {
        try {
            if (application.getProfileId() != null) {
                SProfile profile = profileService.getProfile(application.getProfileId());
                applicationNode.setProfile(profile.getName());
            }
        } catch (SBonitaException e) {
            throw new ExecutionException(e);
        }
    }

    public SApplication toSApplication(ApplicationNode applicationNode, long createdBy) throws ExecutionException {


        SApplicationBuilder builder = BuilderFactory.get(SApplicationBuilderFactory.class).createNewInstance(applicationNode.getToken(), applicationNode.getDisplayName(), applicationNode.getVersion(), createdBy);
        builder.setIconPath(applicationNode.getIconPath());
        builder.setDescription(applicationNode.getDescription());
        builder.setState(applicationNode.getState());

        setProfile(applicationNode, builder);

        return builder.done();
    }

    private void setProfile(ApplicationNode applicationNode, SApplicationBuilder builder) throws ExecutionException {
        if(applicationNode.getProfile() != null) {
            try {
                SProfile profile = profileService.getProfileByName(applicationNode.getProfile());
                builder.setProfileId(profile.getId());
            } catch (SProfileNotFoundException e) {
                throw new ExecutionException(e);
            }
        }
    }

}
