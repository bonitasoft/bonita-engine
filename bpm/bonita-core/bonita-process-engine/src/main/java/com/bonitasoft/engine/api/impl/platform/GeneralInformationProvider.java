/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 **/

package com.bonitasoft.engine.api.impl.platform;

import java.util.Map;

import org.bonitasoft.engine.commons.Pair;
import org.bonitasoft.engine.platform.PlatformNotFoundException;
import org.bonitasoft.engine.platform.PlatformRetriever;
import org.bonitasoft.engine.platform.exception.SPlatformNotFoundException;
import org.bonitasoft.engine.platform.model.SPlatform;

import com.bonitasoft.manager.Manager;

/**
 * @author Elias Ricken de Medeiros
 */
public class GeneralInformationProvider {

    public static final String PLATFORM_INFO_KEY = "platformInfo";
    private final PlatformRetriever platformRetriever;

    public GeneralInformationProvider(PlatformRetriever platformRetriever) {
        this.platformRetriever = platformRetriever;
    }

    Manager getManager() {
        return Manager.getInstance();
    }

    public Map<String, String> getInfo() throws PlatformNotFoundException {
        try {
            SPlatform platform = platformRetriever.getPlatform();
            return getManager().getInfo(Pair.of(PLATFORM_INFO_KEY, platform.getInformation()));
        } catch (SPlatformNotFoundException e) {
            throw new PlatformNotFoundException(e.getMessage(), e);
        }
    }

}
