/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.core.page;

/**
 * @author Laurent Leseigneur
 * @author Emmanuel Duchastenier
 */
public class SPageBuilderFactoryImpl implements SPageBuilderFactory {

    @Override
    public SPageBuilder createNewInstance(final String name, final long installationDate, final long installedBy, final boolean provided) {
        final SPageImpl report = new SPageImpl(name, installationDate, installedBy, provided);
        report.setLastModificationDate(installationDate);
        return new SPageBuilderImpl(report);
    }

    @Override
    public SPageBuilder createNewInstance(final String name, final long installationDate, final long installedBy, final boolean provided,
            final String description) {
        final SPageImpl report = new SPageImpl(name, installationDate, installedBy, provided, description, installationDate);
        return new SPageBuilderImpl(report);
    }

    @Override
    public SPageBuilder createNewInstance(final String name, final long installedBy, final boolean provided, final String description) {
        final SPageImpl report = new SPageImpl(name, System.currentTimeMillis(), installedBy, provided, description, System.currentTimeMillis());
        return new SPageBuilderImpl(report);
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
    public String getLastModificationDateKey() {
        return "lastModificationDate";
    }
}
