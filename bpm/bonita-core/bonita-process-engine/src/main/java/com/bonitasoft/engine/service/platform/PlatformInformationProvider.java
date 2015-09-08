/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 **/

package com.bonitasoft.engine.service.platform;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Elias Ricken de Medeiros
 */
public class PlatformInformationProvider {

    private AtomicInteger partialInfo;

    public PlatformInformationProvider() {
        partialInfo = new AtomicInteger();
    }

    public int getAndReset() {
        return partialInfo.getAndSet(0);
    }

    public int get() {
        return partialInfo.get();
    }

    public void register() {
        partialInfo.incrementAndGet();
    }

}
