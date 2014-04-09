/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api;

/**
 * In addition to tenant state (enable / disable), there is a tenant "mode", saying on an enabled tenant if the tenant is in {@link #AVAILABLE} mode or in a
 * {@link #MAINTENANCE} mode, that is, if we can normally access it, or if only maintenance actions can be made.
 * 
 * @see AvailableOnMaintenanceTenant
 * @author Emmanuel Duchastenier
 */
public enum TenantMode {

    AVAILABLE, // normal mode, the tenant is available (if tenant is enabled)
    MAINTENANCE // in maintenance, only some few specific tenant API calls can be made
}
