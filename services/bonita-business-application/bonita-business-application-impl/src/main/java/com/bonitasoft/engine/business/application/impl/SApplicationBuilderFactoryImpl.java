/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.business.application.impl;

import com.bonitasoft.engine.business.application.SApplicationBuilder;
import com.bonitasoft.engine.business.application.SApplicationBuilderFactory;
import com.bonitasoft.engine.business.application.SApplicationState;


/**
 * @author Elias Ricken de Medeiros
 *
 */
public class SApplicationBuilderFactoryImpl implements SApplicationBuilderFactory {

    @Override
    public SApplicationBuilder createNewInstance(final String name, final String version, final String path, final long createdBy) {
        final long currentDate = System.currentTimeMillis();
        return new SApplicationBuilderImpl(new SApplicationImpl(name, version, path, currentDate, createdBy, SApplicationState.DEACTIVATED.name()));
    }

    @Override
    public String getIdKey() {
        return SApplicationFields.ID;
    }

    @Override
    public String getNameKey() {
        return SApplicationFields.NAME;
    }

    @Override
    public String getVersionKey() {
        return SApplicationFields.VERSION;
    }

    @Override
    public String getPathKey() {
        return SApplicationFields.PATH;
    }

    @Override
    public String getDescriptionKey() {
        return SApplicationFields.DESCRIPTION;
    }

    @Override
    public String getIconPathKey() {
        return SApplicationFields.ICON_PATH;
    }

    @Override
    public String getCreationDateKey() {
        return SApplicationFields.CREATION_DATE;
    }

    @Override
    public String getCreatedByKey() {
        return SApplicationFields.CREATED_BY;
    }

    @Override
    public String getLastUpdatedDateKey() {
        return SApplicationFields.LAST_UPDATE_DATE;
    }

    @Override
    public String getUpdatedByKey() {
        return SApplicationFields.UPDATED_BY;
    }

    @Override
    public String getStateKey() {
        return SApplicationFields.HOME_PAGE_ID;
    }

}
