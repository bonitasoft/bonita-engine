/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api;

import org.bonitasoft.engine.api.NoSessionRequired;

import com.bonitasoft.engine.platform.LicenseInfo;

/**
 * Handle nodes
 * A node is a JVM that allows the engine to run on a platform
 * 
 * @author Baptiste Mesta
 */
public interface NodeAPI {

    /**
     * get the license informations for this node
     * 
     * @return
     *         License informations
     */
    @NoSessionRequired
    LicenseInfo getLicenseInfo();

}
