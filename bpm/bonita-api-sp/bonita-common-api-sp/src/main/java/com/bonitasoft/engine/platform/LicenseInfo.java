/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.platform;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Defines information about the node license
 *
 * @author Matthieu Chaffotte
 */
public interface LicenseInfo extends Serializable {

    /**
     * Retrieves the name of the license owner
     *
     * @return the name of the license owner
     */
    String getLicensee();

    /**
     * Retrieves the license expiration date
     *
     * @return the license expiration date
     */
    Date getExpirationDate();

    /**
     * Retrieves the name of the edition this license allows to run
     *
     * @return the name of the edition this license allows to run
     */
    String getEdition();

    /**
     * Retrieves a list containing the names of all features available for this license
     *
     * @return a list containing the names of all features available for this license
     */
    List<String> getFeatures();

    /**
     * Retrieves the number of CPU cores this license allows
     *
     * @return the number of CPU cores this license allows
     */
    int getNumberOfCPUCores();

}
