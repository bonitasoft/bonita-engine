/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl.convertor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

import com.bonitasoft.engine.business.application.Application;
import com.bonitasoft.engine.business.application.ApplicationCreator;
import com.bonitasoft.engine.business.application.ApplicationField;
import com.bonitasoft.engine.business.application.ApplicationMenu;
import com.bonitasoft.engine.business.application.ApplicationMenuCreator;
import com.bonitasoft.engine.business.application.ApplicationMenuCreator.ApplicationMenuField;
import com.bonitasoft.engine.business.application.ApplicationPage;
import com.bonitasoft.engine.business.application.ApplicationUpdater;
import com.bonitasoft.engine.business.application.impl.ApplicationImpl;
import com.bonitasoft.engine.business.application.impl.ApplicationMenuImpl;
import com.bonitasoft.engine.business.application.impl.ApplicationPageImpl;
import com.bonitasoft.engine.business.application.model.SApplication;
import com.bonitasoft.engine.business.application.model.SApplicationMenu;
import com.bonitasoft.engine.business.application.model.SApplicationPage;
import com.bonitasoft.engine.business.application.model.builder.SApplicationBuilder;
import com.bonitasoft.engine.business.application.model.builder.SApplicationBuilderFactory;
import com.bonitasoft.engine.business.application.model.builder.SApplicationMenuBuilder;
import com.bonitasoft.engine.business.application.model.builder.SApplicationMenuBuilderFactory;
import com.bonitasoft.engine.business.application.model.builder.SApplicationUpdateBuilder;
import com.bonitasoft.engine.business.application.model.builder.SApplicationUpdateBuilderFactory;


/**
 * @author Elias Ricken de Medeiros
 *
 */
public class ApplicationConvertor {

    public SApplication buildSApplication(final ApplicationCreator creator, final long creatorUserId) {
        final Map<ApplicationField, Serializable> fields = creator.getFields();
        final String name = (String) fields.get(ApplicationField.NAME);
        final String displayName = (String) fields.get(ApplicationField.DISPLAY_NAME);
        final String version = (String) fields.get(ApplicationField.VERSION);
        final String description = (String) fields.get(ApplicationField.DESCRIPTION);
        final String path = (String) fields.get(ApplicationField.PATH);
        final String iconPath = (String) fields.get(ApplicationField.ICON_PATH);
        final Long profileId = (Long) fields.get(ApplicationField.PROFILE_ID);
        final SApplicationBuilder builder = BuilderFactory.get(SApplicationBuilderFactory.class).createNewInstance(name, displayName, version, path, creatorUserId);
        builder.setDescription(description);
        builder.setIconPath(iconPath);
        builder.setProfileId(profileId);
        return builder.done();
    }

    public Application toApplication(final SApplication sApplication) {
        final ApplicationImpl application = new ApplicationImpl(sApplication.getName(), sApplication.getVersion(), sApplication.getPath(),
                sApplication.getDescription());
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
        final SApplicationUpdateBuilder builder = BuilderFactory.get(SApplicationUpdateBuilderFactory.class).createNewInstance();
        for (final Entry<ApplicationField, Serializable> entry : updater.getFields().entrySet()) {
            switch (entry.getKey()) {
                case NAME:
                    builder.updateName((String) entry.getValue());
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

                case PATH:
                    builder.updatePath((String) entry.getValue());
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
                default:
                    break;
            }
        }

        if (builder.done().getFields().size() > 0) {
            builder.updateUpdatedBy(updaterUserId);
        }

        return builder.done();
    }

    public ApplicationPage toApplicationPage(final SApplicationPage sApplicationPage) {
        final ApplicationPageImpl appPage = new ApplicationPageImpl(sApplicationPage.getApplicationId(), sApplicationPage.getPageId(),
                sApplicationPage.getName());
        appPage.setId(sApplicationPage.getId());
        return appPage;
    }

    public List<ApplicationPage> toApplicationPage(final List<SApplicationPage> sApplicationPages) {
        final List<ApplicationPage> appPages = new ArrayList<ApplicationPage>(sApplicationPages.size());
        for (final SApplicationPage sApplicationPage : sApplicationPages) {
            appPages.add(toApplicationPage(sApplicationPage));
        }
        return appPages;
    }

    public SApplicationMenu buildSApplicationMenu(final ApplicationMenuCreator creator) {
        final Map<ApplicationMenuField, Serializable> fields = creator.getFields();
        final String displayName = (String) fields.get(ApplicationMenuField.DISPLAY_NAME);
        final long applicationPageId = (Long) fields.get(ApplicationMenuField.APPLICATION_PAGE_ID);
        final int index = (Integer) fields.get(ApplicationMenuField.INDEX);
        final Long parentId = (Long) fields.get(ApplicationMenuField.PARENT_ID);

        final SApplicationMenuBuilder builder = BuilderFactory.get(SApplicationMenuBuilderFactory.class).createNewInstance(displayName, applicationPageId,
                index);
        if (parentId != null) {
            builder.setParentId(parentId);
        }
        return builder.done();
    }

    public ApplicationMenu toApplicationMenu(final SApplicationMenu sApplicationMenu) {
        final ApplicationMenuImpl menu = new ApplicationMenuImpl(sApplicationMenu.getDisplayName(), sApplicationMenu.getApplicationPageId(),
                sApplicationMenu.getIndex());
        menu.setId(sApplicationMenu.getId());
        menu.setParentId(sApplicationMenu.getParentId());
        return menu;
    }

    public List<ApplicationMenu> toApplicationMenu(final List<SApplicationMenu> sApplicationMenus) {
        final List<ApplicationMenu> menus = new ArrayList<ApplicationMenu>(sApplicationMenus.size());
        for (final SApplicationMenu sMenu : sApplicationMenus) {
            menus.add(toApplicationMenu(sMenu));
        }
        return menus;
    }

}
