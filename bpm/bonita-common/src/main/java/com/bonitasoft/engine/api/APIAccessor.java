/*
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package com.bonitasoft.engine.api;

import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.MonitoringAPI;

/**
 * @author Matthieu Chaffotte
 */
public interface APIAccessor extends org.bonitasoft.engine.api.APIAccessor {

    @Override
    IdentityAPI getIdentityAPI();

    @Override
    ProcessAPI getProcessAPI();

    @Override
    MonitoringAPI getMonitoringAPI();

    LogAPI getLogAPI();

    @Override
    CommandAPI getCommandAPI();

}
