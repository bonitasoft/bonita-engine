/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 **/

package com.bonitasoft.engine.service.platform;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.bonitasoft.engine.commons.PlatformLifecycleService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;

/**
 * @author Elias Ricken de Medeiros
 */
public class PlatformInfoUpdateScheduledExecutor implements PlatformLifecycleService {

    public static final int PERIOD = 5;
    private final TransactionalPlatformInformationUpdater transactionalPlatformInformationUpdater;
    private ScheduledExecutorService scheduledExecutor;

    private int period;

    public PlatformInfoUpdateScheduledExecutor(TransactionalPlatformInformationUpdater transactionalPlatformInformationUpdater) {
        this.transactionalPlatformInformationUpdater = transactionalPlatformInformationUpdater;
        this.period = PERIOD;
    }

    protected int getPeriod() {
        return period;
    }

    protected ScheduledExecutorService getScheduledExecutor() {
        if(scheduledExecutor == null) {
            scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
        }
        return scheduledExecutor;
    }

    @Override
    public void start() throws SBonitaException {
        getScheduledExecutor().scheduleWithFixedDelay(transactionalPlatformInformationUpdater, getPeriod(), getPeriod(), TimeUnit.SECONDS);
    }

    @Override
    public void stop() throws SBonitaException {
        getScheduledExecutor().shutdown();
        scheduledExecutor = null;
    }

    @Override
    public void pause() throws SBonitaException {
    }

    @Override
    public void resume() throws SBonitaException {
    }

}
