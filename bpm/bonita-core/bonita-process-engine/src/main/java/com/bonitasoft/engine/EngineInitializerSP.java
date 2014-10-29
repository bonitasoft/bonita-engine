/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine;

import org.bonitasoft.engine.EngineInitializer;
import org.bonitasoft.engine.EngineInitializerProperties;
import org.bonitasoft.engine.PlatformTenantManager;
import org.bonitasoft.engine.api.PlatformAPI;

import com.bonitasoft.engine.api.impl.PlatformAPIExt;

public class EngineInitializerSP extends EngineInitializer {

    public EngineInitializerSP(final PlatformTenantManager platformManager, final EngineInitializerProperties platformProperties) {
        super(platformManager, platformProperties);
    }

    @Override
    protected PlatformAPI buildPlatformAPIInstance() {
        return new PlatformAPIExt();
    }

    public static void init() throws Exception {
        new EngineInitializerSP(PlatformTenantManager.getInstance(), new EngineInitializerProperties()).initializeEngine();
    }

    public static void unload() throws Exception {
        new EngineInitializerSP(PlatformTenantManager.getInstance(), new EngineInitializerProperties()).unloadEngine();
    }
}
