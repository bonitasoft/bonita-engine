/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package org.bonitasoft.engine.api.impl.converter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.business.application.Application;
import org.bonitasoft.engine.business.application.ApplicationCreator;
import org.bonitasoft.engine.business.application.ApplicationField;
import org.bonitasoft.engine.business.application.ApplicationUpdater;
import org.bonitasoft.engine.business.application.impl.ApplicationImpl;
import org.bonitasoft.engine.business.application.model.SApplication;
import org.bonitasoft.engine.business.application.model.builder.SApplicationBuilder;
import org.bonitasoft.engine.business.application.model.builder.SApplicationBuilderFactory;
import org.bonitasoft.engine.business.application.model.builder.SApplicationUpdateBuilder;
import org.bonitasoft.engine.business.application.model.builder.SApplicationUpdateBuilderFactory;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

/**
 * @author Elias Ricken de Medeiros
 */
public class ApplicationModelConverter {

    public SApplication buildSApplication(final ApplicationCreator creator, final long creatorUserId) {
        final Map<ApplicationField, Serializable> fields = creator.getFields();
        final String name = (String) fields.get(ApplicationField.TOKEN);
        final String displayName = (String) fields.get(ApplicationField.DISPLAY_NAME);
        final String version = (String) fields.get(ApplicationField.VERSION);
        final String description = (String) fields.get(ApplicationField.DESCRIPTION);
        final String iconPath = (String) fields.get(ApplicationField.ICON_PATH);
        final Long profileId = (Long) fields.get(ApplicationField.PROFILE_ID);
        final SApplicationBuilder builder = BuilderFactory.get(SApplicationBuilderFactory.class).createNewInstance(name, displayName, version, creatorUserId);
        builder.setDescription(description);
        builder.setIconPath(iconPath);
        builder.setProfileId(profileId);
        return builder.done();
    }

    public Application toApplication(final SApplication sApplication) {
        final ApplicationImpl application = new ApplicationImpl(sApplication.getToken(), sApplication.getVersion(), sApplication.getDescription());
        application.setId(sApplication.getId());
        application.setDisplayName(sApplication.getDisplayName());
        application.setCreatedBy(sApplication.getCreatedBy());
        application.setCreationDate(new Date(sApplication.getCreationDate()));
        application.setUpdatedBy(sApplication.getUpdatedBy());
        application.setLastUpdateDate(new Date(sApplication.getLastUpdateDate()));
        application.setState(sApplication.getState());
        application.setIconPath(sApplication.getIconPath());
        application.setHomePageId(sApplication.getHomePageId());
        application.setProfileId(sApplication.getProfileId());
        return application;
    }

    public List<Application> toApplication(final List<SApplication> sApplications) {
        final List<Application> applications = new ArrayList<Application>(sApplications.size());
        for (final SApplication sApplication : sApplications) {
            applications.add(toApplication(sApplication));
        }
        return applications;
    }

    public EntityUpdateDescriptor toApplicationUpdateDescriptor(final ApplicationUpdater updater, final long updaterUserId) {
        final SApplicationUpdateBuilder builder = BuilderFactory.get(SApplicationUpdateBuilderFactory.class).createNewInstance(updaterUserId);
        for (final Entry<ApplicationField, Serializable> entry : updater.getFields().entrySet()) {
            switch (entry.getKey()) {
                case TOKEN:
                    builder.updateToken((String) entry.getValue());
                    break;

                case DESCRIPTION:
                    builder.updateDescription((String) entry.getValue());
                    break;

                case DISPLAY_NAME:
                    builder.updateDisplayName((String) entry.getValue());
                    break;

                case ICON_PATH:
                    builder.updateIconPath((String) entry.getValue());
                    break;

                case PROFILE_ID:
                    builder.updateProfileId((Long) entry.getValue());
                    break;

                case STATE:
                    builder.updateState((String) entry.getValue());
                    break;

                case VERSION:
                    builder.updateVersion((String) entry.getValue());
                    break;
                case HOME_PAGE_ID:
                    builder.updateHomePageId((Long) entry.getValue());
                    break;
                default:
                    break;
            }
        }

        return builder.done();
    }

}
