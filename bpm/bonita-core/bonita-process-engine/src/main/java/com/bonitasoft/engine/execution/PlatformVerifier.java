/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 **/

package com.bonitasoft.engine.execution;

import com.bonitasoft.manager.Manager;
import org.bonitasoft.engine.platform.PlatformRetriever;
import org.bonitasoft.engine.platform.exception.SPlatformNotFoundException;

/**
 * @author Elias Ricken de Medeiros
 */
public class PlatformVerifier {

    private final PlatformRetriever platformRetriever;

    public PlatformVerifier(PlatformRetriever platformRetriever) {
        this.platformRetriever = platformRetriever;
    }

    public String check() throws SPlatformNotFoundException {
        return getManager().checkPlatformInfo(platformRetriever.getPlatform().getInformation());
    }

    protected Manager getManager() {
        return Manager.getInstance();
    }

}
