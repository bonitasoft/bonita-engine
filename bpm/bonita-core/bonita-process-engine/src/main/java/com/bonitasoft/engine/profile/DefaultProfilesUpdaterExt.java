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

import org.bonitasoft.engine.profile.DefaultProfilesUpdater;
import org.bonitasoft.engine.profile.ProfilesImporter;
import org.bonitasoft.engine.profile.impl.ExportedProfile;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * Class that creates and updates default profiles<br/>
 * Called at node restart and tenant creation
 * @author Philippe Ozil
 */
public class DefaultProfilesUpdaterExt extends DefaultProfilesUpdater {

    public DefaultProfilesUpdaterExt(final PlatformServiceAccessor platformServiceAccessor, final TenantServiceAccessor tenantServiceAccessor) {
        super(platformServiceAccessor, tenantServiceAccessor);
    }

    @Override
    protected ProfilesImporter createProfilesImporter(List<ExportedProfile> defaultProfiles) {
        return new ProfilesImporterExt(tenantServiceAccessor.getProfileService(), tenantServiceAccessor.getIdentityService(),
                ((com.bonitasoft.engine.service.TenantServiceAccessor) tenantServiceAccessor).getPageService(), defaultProfiles,
                org.bonitasoft.engine.profile.ImportPolicy.UPDATE_DEFAULTS);
    }
}
