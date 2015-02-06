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
 */
public interface SReportBuilderFactory {

    SReportBuilder createNewInstance(String name, long installationDate, long installedBy, boolean provided);

    SReportBuilder createNewInstance(String name, long installationDate, long installedBy, boolean provided, String description, byte[] screenshot);

    SReportBuilder createNewInstance(String name, long installedBy, boolean provided, String description, byte[] screenshot);

    String getIdKey();

    String getNameKey();

    String getDescriptionKey();

    String getProvidedKey();

    String getInstallationDateKey();

    String getInstalledByKey();

}
