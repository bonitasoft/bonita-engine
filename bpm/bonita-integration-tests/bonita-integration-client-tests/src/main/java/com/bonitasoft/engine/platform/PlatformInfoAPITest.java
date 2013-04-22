/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.platform;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.bonitasoft.engine.CommonAPISPTest;
import com.bonitasoft.engine.api.PlatformAPIAccessor;
import com.bonitasoft.engine.api.PlatformInfoAPI;

public class PlatformInfoAPITest extends CommonAPISPTest {

    @Test
    public void getLincenseInfo() throws Exception {
        final PlatformInfoAPI platformInfoAPI = PlatformAPIAccessor.getPlatformInfoAPI();
        final LicenseInfo licenseInfo = platformInfoAPI.getLicenseInfo();

        assertTrue(licenseInfo.getExpirationDate().getTime() > System.currentTimeMillis());
        assertTrue(licenseInfo.getNumberOfCPUCores() != 0);
        assertTrue(!licenseInfo.getEdition().isEmpty());
        assertTrue(!licenseInfo.getFeatures().isEmpty());
        assertTrue(!licenseInfo.getLicensee().isEmpty());
    }
}
