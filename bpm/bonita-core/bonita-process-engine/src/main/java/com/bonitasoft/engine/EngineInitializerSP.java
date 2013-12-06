package com.bonitasoft.engine;

import org.bonitasoft.engine.EngineInitializer;
import org.bonitasoft.engine.EngineInitializerProperties;
import org.bonitasoft.engine.PlatformTenantManager;
import org.bonitasoft.engine.api.impl.PlatformAPIImpl;

import com.bonitasoft.engine.api.impl.PlatformAPIExt;

public class EngineInitializerSP extends EngineInitializer {

    public EngineInitializerSP(final PlatformTenantManager platformManager, final EngineInitializerProperties platformProperties) {
        super(platformManager, platformProperties);
    }

    @Override
    protected PlatformAPIImpl createPlatformAPI() {
        return new PlatformAPIExt();
    }

}
