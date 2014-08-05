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

import org.bonitasoft.engine.builder.BuilderFactory;

import com.bonitasoft.engine.business.application.Application;
import com.bonitasoft.engine.business.application.ApplicationCreator;
import com.bonitasoft.engine.business.application.ApplicationCreator.ApplicationField;
import com.bonitasoft.engine.business.application.SApplication;
import com.bonitasoft.engine.business.application.SApplicationBuilder;
import com.bonitasoft.engine.business.application.SApplicationBuilderFactory;
import com.bonitasoft.engine.business.application.impl.ApplicationImpl;


/**
 * @author Elias Ricken de Medeiros
 *
 */
public class ApplicationConvertor {

    public SApplication buildSApplication(final ApplicationCreator creator, final long creatorUserId) {
        final Map<ApplicationField, Serializable> fields = creator.getFields();
        final String name = (String) fields.get(ApplicationField.NAME);
        final String version = (String) fields.get(ApplicationField.VERSION);
        final String description = (String) fields.get(ApplicationField.DESCRIPTION);
        final String path = (String) fields.get(ApplicationField.PATH);
        final String iconPath = (String) fields.get(ApplicationField.ICON_PATH);
        final SApplicationBuilder builder = BuilderFactory.get(SApplicationBuilderFactory.class).createNewInstance(name, version, path, creatorUserId);
        builder.setDescription(description);
        builder.setIconPath(iconPath);
        return builder.done();
    }

    public Application toApplication(final SApplication sApplication) {
        final ApplicationImpl application = new ApplicationImpl(sApplication.getName(), sApplication.getVersion(), sApplication.getPath(),
                sApplication.getDescription());
        application.setId(sApplication.getId());
        application.setCreatedBy(sApplication.getCreatedBy());
        application.setCreationDate(new Date(sApplication.getCreationDate()));
        application.setUpdatedBy(sApplication.getUpdatedBy());
        application.setLastUpdateDate(new Date(sApplication.getLastUpdateDate()));
        application.setState(sApplication.getState());
        application.setIconPath(sApplication.getIconPath());
        return application;
    }

    public List<Application> toApplication(final List<SApplication> sApplications) {
        final List<Application> applications = new ArrayList<Application>(sApplications.size());
        for (final SApplication sApplication : sApplications) {
            applications.add(toApplication(sApplication));
        }
        return applications;
    }

}
