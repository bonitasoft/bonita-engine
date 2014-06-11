/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl.transaction;

import org.bonitasoft.engine.api.impl.transaction.ServiceStrategy;
import org.bonitasoft.engine.commons.LifecycleService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;

/**
 * @author Matthieu Chaffotte
 */
public class PauseServiceStrategy implements ServiceStrategy {

    private static final long serialVersionUID = 3053409607324898381L;

    @Override
    public String getStateName() {
        return "pause";
    }

    @Override
    public void changeState(final LifecycleService service) throws SBonitaException {
        service.pause();
    }

    @Override
    public boolean shouldRefreshClassLoaders() {
        return false;
    }

}
