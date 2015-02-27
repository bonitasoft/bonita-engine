/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.core.reporting;

import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Matthieu Chaffotte
 */
public interface SReport extends PersistentObject {

    String getName();

    String getDescription();

    long getInstallationDate();

    long getInstalledBy();

    boolean isProvided();

    /**
     * @return the date of the last modification of this report.
     */
    long getLastModificationDate();

    /**
     * @return the screenshot associated with this report, as a binary content.
     */
    byte[] getScreenshot();

}
