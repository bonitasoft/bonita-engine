/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.monitoring;

import org.bonitasoft.engine.test.WaitUntil;

import com.bonitasoft.engine.api.PlatformMonitoringAPI;

/**
 * @author Matthieu Chaffotte
 */
public class ActiveTransactionWaitUntil extends WaitUntil {

    private final PlatformMonitoringAPI monitoringAPI;

    public ActiveTransactionWaitUntil(final PlatformMonitoringAPI monitoringAPI, final int repeatEach, final int timeout) {
        super(repeatEach, timeout);
        this.monitoringAPI = monitoringAPI;
    }

    @Override
    protected boolean check() throws Exception {
        return monitoringAPI.getNumberOfActiveTransactions() == 0;
    }

}
