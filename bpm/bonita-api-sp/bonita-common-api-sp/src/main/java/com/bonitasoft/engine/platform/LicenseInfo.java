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
 * Informations on the license of a node
 * 
 * @author Matthieu Chaffotte
 */
public interface LicenseInfo extends Serializable {

    /**
     * @return
     *         name of the license owner
     */
    String getLicensee();

    /**
     * @return
     *         date after which the license will expire
     */
    Date getExpirationDate();

    /**
     * @return
     *         name of the edition this license allow to run
     */
    String getEdition();

    /**
     * @return
     *         list of features that are available using this license
     */
    List<String> getFeatures();

    /**
     * @return
     *         the number of CPU cores this license allows
     */
    int getNumberOfCPUCores();

}
