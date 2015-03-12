/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.core.reporting;

/**
 * @author Matthieu Chaffotte
 * @author Emmanuel Duchastenier
 */
public class SReportBuilderFactoryImpl implements SReportBuilderFactory {

    @Override
    public SReportBuilder createNewInstance(final String name, final long installationDate, final long installedBy, final boolean provided) {
        final SReportImpl report = new SReportImpl(name, installationDate, installedBy, provided);
        report.setLastModificationDate(installationDate);
        return new SReportBuilderImpl(report);
    }

    @Override
    public SReportBuilder createNewInstance(final String name, final long installationDate, final long installedBy, final boolean provided,
            final String description, final byte[] screenshot) {
        final SReportImpl report = new SReportImpl(name, installationDate, installedBy, provided, description, installationDate, screenshot);
        return new SReportBuilderImpl(report);
    }

    @Override
    public SReportBuilder createNewInstance(final String name, final long installedBy, final boolean provided, final String description, final byte[] screenshot) {
        final SReportImpl report = new SReportImpl(name, System.currentTimeMillis(), installedBy, provided, description, System.currentTimeMillis(), screenshot);
        return new SReportBuilderImpl(report);
    }

    @Override
    public String getNameKey() {
        return SReportFields.NAME;
    }

    @Override
    public String getDescriptionKey() {
        return SReportFields.DESCRIPTION;
    }

    @Override
    public String getInstallationDateKey() {
        return SReportFields.INSTALLATION_DATE;
    }

    @Override
    public String getInstalledByKey() {
        return SReportFields.INSTALLED_BY;
    }

    @Override
    public String getIdKey() {
        return SReportFields.ID;
    }

    @Override
    public String getProvidedKey() {
        return SReportFields.PROVIDED;
    }
}
