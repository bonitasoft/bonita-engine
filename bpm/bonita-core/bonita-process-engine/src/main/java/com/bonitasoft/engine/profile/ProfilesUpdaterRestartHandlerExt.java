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

import java.util.List;

import org.bonitasoft.engine.profile.ProfilesImporter;
import org.bonitasoft.engine.profile.ProfilesUpdaterRestartHandler;
import org.bonitasoft.engine.profile.impl.ExportedProfile;
import org.bonitasoft.engine.service.TenantServiceAccessor;

import com.bonitasoft.engine.api.impl.PlatformAPIExt;

/**
 * @author Baptiste Mesta
 */
public class ProfilesUpdaterRestartHandlerExt extends ProfilesUpdaterRestartHandler {

    @Override
    protected String getProfilesFileName() {
        return PlatformAPIExt.PROFILES_FILE_SP;
    }

    @Override
    protected ProfilesImporter createProfilesImporter(TenantServiceAccessor tenantServiceAccessor, List<ExportedProfile> profilesFromXML) {
        return new ProfilesImporterExt(tenantServiceAccessor.getProfileService(), tenantServiceAccessor
                .getIdentityService(), ((com.bonitasoft.engine.service.TenantServiceAccessor) tenantServiceAccessor).getPageService(), profilesFromXML,
                org.bonitasoft.engine.profile.ImportPolicy.UPDATE_DEFAULTS);
    }
}
