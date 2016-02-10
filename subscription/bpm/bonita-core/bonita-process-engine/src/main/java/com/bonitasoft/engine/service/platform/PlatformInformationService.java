/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 **/

package com.bonitasoft.engine.service.platform;

import org.bonitasoft.engine.platform.exception.SPlatformUpdateException;
import org.bonitasoft.engine.platform.model.SPlatform;

/**
 * @author Elias Ricken de Medeiros
 */
public interface PlatformInformationService {

    /**
     * Updates the platform information
     * @param platform the platform to be updated
     * @param platformInfo the new platform information
     * @throws SPlatformUpdateException
     * @since 7.1.0
     */
    void updatePlatformInfo(SPlatform platform, String platformInfo) throws SPlatformUpdateException;

}
