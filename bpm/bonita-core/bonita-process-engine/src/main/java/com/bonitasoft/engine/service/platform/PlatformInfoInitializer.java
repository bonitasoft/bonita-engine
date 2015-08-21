/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 **/

package com.bonitasoft.engine.service.platform;

import com.bonitasoft.manager.Manager;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.platform.PlatformRetriever;
import org.bonitasoft.engine.platform.model.SPlatform;

/**
 * @author Elias Ricken de Medeiros
 */
public class PlatformInfoInitializer {

    private final PlatformRetriever platformRetriever;
    private final PlatformInformationService platformInformationService;
    private final Manager manager;

    public PlatformInfoInitializer(PlatformRetriever platformRetriever, PlatformInformationService platformInformationService) {
        this(platformRetriever, platformInformationService, Manager.getInstance());
    }

    protected PlatformInfoInitializer(PlatformRetriever platformRetriever, PlatformInformationService platformInformationService, Manager manager) {
        this.platformRetriever = platformRetriever;
        this.platformInformationService = platformInformationService;
        this.manager = manager;
    }

    public void ensurePlatformInfoIsSet() throws SBonitaException {
        SPlatform platform = platformRetriever.getPlatform();
        if(platform.getInformation() == null) {
            String platformInfo = manager.calculateNewPlatformInfo(platform.getInformation());
            platformInformationService.updatePlatformInfo(platform, platformInfo);
        }
    }

}
