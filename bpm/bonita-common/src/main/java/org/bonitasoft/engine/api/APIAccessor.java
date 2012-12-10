/*
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package org.bonitasoft.engine.api;

import com.bonitasoft.engine.api.LogAPI;

/**
 * @author Matthieu Chaffotte
 */
public interface APIAccessor {

    IdentityAPI getIdentityAPI();

    ProcessAPI getProcessAPI();

    MonitoringAPI getMonitoringAPI();

    LogAPI getLogAPI();

    CommandAPI getCommandAPI();

}
