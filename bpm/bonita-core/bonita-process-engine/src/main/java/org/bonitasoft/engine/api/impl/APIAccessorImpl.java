/*
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package org.bonitasoft.engine.api.impl;

import java.io.Serializable;

import org.bonitasoft.engine.api.APIAccessor;
import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.LogAPI;
import org.bonitasoft.engine.api.MonitoringAPI;
import org.bonitasoft.engine.api.ProcessAPI;

/**
 * @author Matthieu Chaffotte
 */
public class APIAccessorImpl implements APIAccessor, Serializable {

    private static final long serialVersionUID = -7317110051980496939L;

    @Override
    public IdentityAPI getIdentityAPI() {
        return new IdentityAPIImpl();
    }

    @Override
    public ProcessAPI getProcessAPI() {
        return new ProcessAPIImpl();
    }

    @Override
    public MonitoringAPI getMonitoringAPI() {
        return new MonitoringAPIImpl();
    }

    @Override
    public LogAPI getLogAPI() {
        return new LogAPIImpl();
    }

    @Override
    public CommandAPI getCommandAPI() {
        return new CommandAPIImpl();
    }

}
