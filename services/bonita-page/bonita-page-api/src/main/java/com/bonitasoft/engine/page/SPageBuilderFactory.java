/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.page;

/**
 * @author Matthieu Chaffotte
 */
public interface SPageBuilderFactory {

    SPageBuilder createNewInstance(String name, String description, String displayName, long installationDate, long installedBy, boolean provided,
            String contentName,
            byte[] content);

    SPageBuilder createNewInstance(String name, long installationDate, int installedBy, boolean provided, String contentName);

    String getIdKey();

    String getNameKey();

    String getDisplayNameKey();

    String getDescriptionKey();

    String getProvidedKey();

    String getInstallationDateKey();

    String getLastModificationDateKey();

    String getLastUpdatedByKey();

    String getInstalledByKey();

}
