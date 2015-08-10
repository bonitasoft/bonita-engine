/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 **/

package com.bonitasoft.engine.service.platform;

import org.bonitasoft.engine.platform.PlatformRetriever;
import org.bonitasoft.engine.platform.exception.SPlatformNotFoundException;
import org.bonitasoft.engine.platform.exception.SPlatformUpdateException;
import org.bonitasoft.engine.platform.model.SPlatform;

import com.bonitasoft.manager.Manager;

/**
 * @author Elias Ricken de Medeiros
 */
public class PlatformInformationManagerImpl implements PlatformInformationManager {

    private final PlatformRetriever platformRetriever;
    private final PlatformInformationService platformInformationService;
    private final PlatformInformationProvider platformInformationProvider;

    public PlatformInformationManagerImpl(PlatformRetriever platformRetriever, PlatformInformationService platformInformationService,
            PlatformInformationProvider platformInformationProvider) {
        this.platformRetriever = platformRetriever;
        this.platformInformationService = platformInformationService;
        this.platformInformationProvider = platformInformationProvider;
    }

    public void update() throws SPlatformNotFoundException, SPlatformUpdateException {
        int toUpdate = platformInformationProvider.getAndReset();
        if (toUpdate > 0) {
            SPlatform platform = platformRetriever.getPlatform();
            String newInfo = calculateNewInfo(toUpdate, platform.getInformation());
            platformInformationService.updatePlatformInfo(platform, newInfo);
        }
    }

    private String calculateNewInfo(final int toUpdate, final String currentInfo) throws SPlatformUpdateException {
        String newInfo = currentInfo;
        for (int i = 0; i < toUpdate; i++) {
            newInfo = getManager().calculateNewPlatformInfo(newInfo);
        }
        return newInfo;
    }

    protected Manager getManager() {
        return Manager.getInstance();
    }

}
