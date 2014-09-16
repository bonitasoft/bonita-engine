/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.page.impl;

import com.bonitasoft.engine.page.SPageBuilder;
import com.bonitasoft.engine.page.SPageBuilderFactory;

/**
 * @author Baptiste Mesta
 */
public class SPageBuilderFactoryImpl implements SPageBuilderFactory {

    @Override
    public SPageBuilder createNewInstance(final String name, final String description, final String displayName, final long installationDate,
            final long installedBy, final boolean provided, final String contentName) {
        return new SPageBuilderImpl(new SPageImpl(name, description, displayName, installationDate, installedBy, provided, installationDate, installedBy,
                contentName));
    }

    @Override
    public SPageBuilder createNewInstance(final String name, final long installationDate, final int installedBy, final boolean provided,
            final String contentName) {
        return new SPageBuilderImpl(new SPageImpl(name, installationDate, installedBy, provided, contentName));
    }

    @Override
    public String getNameKey() {
        return "name";
    }

    @Override
    public String getDescriptionKey() {
        return "description";
    }

    @Override
    public String getInstallationDateKey() {
        return "installationDate";
    }

    @Override
    public String getInstalledByKey() {
        return "installedBy";
    }

    @Override
    public String getIdKey() {
        return "id";
    }

    @Override
    public String getProvidedKey() {
        return "provided";
    }

    @Override
    public String getDisplayNameKey() {
        return "displayName";
    }

    @Override
    public String getLastModificationDateKey() {
        return "lastModificationDate";
    }

    @Override
    public String getLastUpdatedByKey() {
        return "lastUpdateBy";
    }

}
