/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api;

import org.bonitasoft.engine.exception.UpdateException;

/**
 * This API gives access to tenant management.
 * 
 * @author Matthieu Chaffotte
 * @author Baptiste Mesta
 */
public interface TenantManagementAPI {

    /**
     * @return
     *         true if the tenant is paused.
     */
    boolean isPaused();

    /**
     * Pause the tenant so nothing is executed anymore.
     * when the tenant is paused:
     * Only technical user can login when the tenant is paused.
     * All users connected are disconnected (apart from the technical user).
     * Only IdentityAPI, ThemeAPI and ProfileAPI are accessible.
     * 
     * @throws UpdateException
     *             if the tenant cannot be paused.
     */
    void pause() throws UpdateException;

    /**
     * Resume the tenant to a normal state after a pause.
     * 
     * @throws UpdateException
     *             if the tenant cannot be resumed.
     */
    void resume() throws UpdateException;

}
