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
 * @author Feng Hui
 * @author Matthieu Chaffotte
 */
public interface Tenant extends BonitaObject {

    long getId();

    String getName();

    String getDescription();

    String getIconName();

    String getIconPath();

    Date getCreationDate();

    String getState();

    boolean isDefaultTenant();

    /**
     * Is this tenant in Maintenance mode, that is, can we normally use this tenant ?
     * 
     * @return true if in maintenance, false otherwise.
     */
    boolean isInMaintenance();

}
