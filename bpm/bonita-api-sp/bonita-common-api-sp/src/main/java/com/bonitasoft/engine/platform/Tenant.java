/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.platform;

import java.util.Date;

import org.bonitasoft.engine.bpm.BonitaObject;

/**
 * Gives access to <code>tenant</code>
 *
 * @author Feng Hui
 * @author Matthieu Chaffotte
 */
public interface Tenant extends BonitaObject {

    /**
     * Retrieves the tenant identifier
     *
     * @return the tenant identifier
     */
    long getId();

    /**
     * Retrieves the tenant name
     *
     * @return the tenant name
     */
    String getName();

    /**
     * Retrieves the tenant description
     *
     * @return the tenant description
     */
    String getDescription();

    /**
     * Retrieves the tenant icon name
     *
     * @return the tenant icon name
     */
    String getIconName();

    /**
     * Retrieves the tenant icon path
     *
     * @return the tenant icon path
     */
    String getIconPath();

    /**
     * Retrieves the tenant creation date
     *
     * @return the tenant creation date
     */
    Date getCreationDate();

    /**
     * Retrieves the tenant state. The possible values are: DEACTIVATED, ACTIVATED, PAUSED.
     *
     * @return the tenant state
     */
    String getState();

    /**
     * Verifies if it is the default tenant.
     *
     * @return true if it is the default tenant; false otherwise
     */
    boolean isDefaultTenant();

}
