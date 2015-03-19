/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine;

import com.bonitasoft.engine.service.PlatformServiceAccessor;
import com.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.bpm.CommonBPMServicesTest;

public class CommonBPMServicesSPTest extends CommonBPMServicesTest {

    @Override
    protected TenantServiceAccessor getTenantAccessor() {
        return (TenantServiceAccessor) super.getTenantAccessor();
    }

    @Override
    protected PlatformServiceAccessor getPlatformAccessor() {
        return (PlatformServiceAccessor) super.getPlatformAccessor();
    }
}
