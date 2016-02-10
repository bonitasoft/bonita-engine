/*
 * *****************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 * ******************************************************************************
 */

package com.bonitasoft.engine.profile;

import org.bonitasoft.engine.profile.DefaultProfilesUpdater;
import org.bonitasoft.engine.profile.ProfilesUpdaterRestartHandler;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * @author Baptiste Mesta
 */
public class ProfilesUpdaterRestartHandlerExt extends ProfilesUpdaterRestartHandler {

    @Override
    protected DefaultProfilesUpdater getProfileUpdater(final PlatformServiceAccessor platformServiceAccessor, 
            final TenantServiceAccessor tenantServiceAccessor) {
        return new DefaultProfilesUpdaterExt(platformServiceAccessor, tenantServiceAccessor);
    }
}
