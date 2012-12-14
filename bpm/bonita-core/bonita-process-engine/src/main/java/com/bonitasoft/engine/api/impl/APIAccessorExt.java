/*
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package com.bonitasoft.engine.api.impl;

import java.io.Serializable;

import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.MonitoringAPI;
import org.bonitasoft.engine.api.impl.APIAccessorImpl;
import org.bonitasoft.engine.api.impl.CommandAPIImpl;
import org.bonitasoft.engine.api.impl.IdentityAPIImpl;
import org.bonitasoft.engine.api.impl.MonitoringAPIImpl;

import com.bonitasoft.engine.api.APIAccessor;
import com.bonitasoft.engine.api.LogAPI;
import com.bonitasoft.engine.api.ProcessAPI;

/**
 * @author Matthieu Chaffotte
 */
public class APIAccessorExt extends APIAccessorImpl implements APIAccessor, Serializable {

    private static final long serialVersionUID = -7317110051980496939L;

    @Override
    public IdentityAPI getIdentityAPI() {
        return new IdentityAPIImpl();
    }

    @Override
    public ProcessAPI getProcessAPI() {
        return new ProcessAPIExt();
    }

    @Override
    public MonitoringAPI getMonitoringAPI() {
        return new MonitoringAPIImpl();
    }

    @Override
    public LogAPI getLogAPI() {
        return new LogAPIExt();
    }

    @Override
    public CommandAPI getCommandAPI() {
        return new CommandAPIImpl();
    }

}
