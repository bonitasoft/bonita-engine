/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.service.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.ApplicationAPI;
import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.LoginAPI;
import org.bonitasoft.engine.api.PageAPI;
import org.bonitasoft.engine.api.PermissionAPI;
import org.bonitasoft.engine.api.PlatformAPI;
import org.bonitasoft.engine.api.PlatformCommandAPI;
import org.bonitasoft.engine.api.PlatformLoginAPI;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.ProfileAPI;
import org.bonitasoft.engine.api.ThemeAPI;
import org.bonitasoft.engine.api.impl.PageAPIImpl;
import org.bonitasoft.engine.api.impl.PermissionAPIImpl;
import org.bonitasoft.engine.api.impl.PlatformCommandAPIImpl;
import org.bonitasoft.engine.api.impl.PlatformLoginAPIImpl;
import org.bonitasoft.engine.api.impl.TenantManagementAPIImpl;
import org.bonitasoft.engine.exception.APIImplementationNotFoundException;
import org.bonitasoft.engine.service.APIAccessResolver;

import com.bonitasoft.engine.api.LogAPI;
import com.bonitasoft.engine.api.MonitoringAPI;
import com.bonitasoft.engine.api.NodeAPI;
import com.bonitasoft.engine.api.PlatformMonitoringAPI;
import com.bonitasoft.engine.api.TenantManagementAPI;
import com.bonitasoft.engine.api.impl.ApplicationAPIImpl;
import com.bonitasoft.engine.api.impl.CommandAPIExt;
import com.bonitasoft.engine.api.impl.IdentityAPIExt;
import com.bonitasoft.engine.api.impl.LogAPIExt;
import com.bonitasoft.engine.api.impl.LoginAPIExt;
import com.bonitasoft.engine.api.impl.MonitoringAPIImpl;
import com.bonitasoft.engine.api.impl.NodeAPIImpl;
import com.bonitasoft.engine.api.impl.PageAPIExt;
import com.bonitasoft.engine.api.impl.PlatformAPIExt;
import com.bonitasoft.engine.api.impl.PlatformMonitoringAPIImpl;
import com.bonitasoft.engine.api.impl.ProcessAPIExt;
import com.bonitasoft.engine.api.impl.ProfileAPIExt;
import com.bonitasoft.engine.api.impl.ReportingAPIExt;
import com.bonitasoft.engine.api.impl.TenantManagementAPIExt;
import com.bonitasoft.engine.api.impl.ThemeAPIExt;

/**
 * @author Matthieu Chaffotte
 */
public class APIAccessResolverImpl implements APIAccessResolver {

    private static final Map<String, Object> apis = new HashMap<String, Object>(25);

    private static final List<String> NO_SESSION_APIS = Arrays.asList(PlatformLoginAPI.class.getName(), LoginAPI.class.getName(),
            com.bonitasoft.engine.api.LoginAPI.class.getName(), NodeAPI.class.getName());

    static {
        apis.put(PlatformAPI.class.getName(), new PlatformAPIExt());
        apis.put(com.bonitasoft.engine.api.PlatformAPI.class.getName(), new PlatformAPIExt());
        apis.put(PlatformLoginAPI.class.getName(), new PlatformLoginAPIImpl());
        apis.put(PlatformMonitoringAPI.class.getName(), new PlatformMonitoringAPIImpl());
        apis.put(LoginAPI.class.getName(), new LoginAPIExt());
        apis.put(com.bonitasoft.engine.api.LoginAPI.class.getName(), new LoginAPIExt());
        apis.put(IdentityAPI.class.getName(), new IdentityAPIExt());
        apis.put(com.bonitasoft.engine.api.IdentityAPI.class.getName(), new IdentityAPIExt());
        apis.put(MonitoringAPI.class.getName(), new MonitoringAPIImpl());
        apis.put(ProcessAPI.class.getName(), new ProcessAPIExt());
        apis.put(com.bonitasoft.engine.api.ProcessAPI.class.getName(), new ProcessAPIExt());
        apis.put(LogAPI.class.getName(), new LogAPIExt());
        apis.put(CommandAPI.class.getName(), new CommandAPIExt());
        apis.put(PlatformCommandAPI.class.getName(), new PlatformCommandAPIImpl());
        apis.put(NodeAPI.class.getName(), new NodeAPIImpl());
        apis.put(com.bonitasoft.engine.api.ReportingAPI.class.getName(), new ReportingAPIExt());
        apis.put(ProfileAPI.class.getName(), new ProfileAPIExt());
        apis.put(com.bonitasoft.engine.api.ProfileAPI.class.getName(), new ProfileAPIExt());
        apis.put(org.bonitasoft.engine.api.TenantManagementAPI.class.getName(), new TenantManagementAPIImpl());
        apis.put(TenantManagementAPI.class.getName(), new TenantManagementAPIExt());
        apis.put(ThemeAPI.class.getName(), new ThemeAPIExt());
        apis.put(com.bonitasoft.engine.api.ThemeAPI.class.getName(), new ThemeAPIExt());
        apis.put(com.bonitasoft.engine.api.TenantManagementAPI.class.getName(), new TenantManagementAPIExt());
        apis.put(PageAPI.class.getName(), new PageAPIImpl());
        apis.put(com.bonitasoft.engine.api.ApplicationAPI.class.getName(), new ApplicationAPIImpl());
        apis.put(ApplicationAPI.class.getName(), new org.bonitasoft.engine.api.impl.ApplicationAPIImpl());
        apis.put(PermissionAPI.class.getName(), new PermissionAPIImpl());
    }

    @Override
    public Object getAPIImplementation(final String interfaceName) throws APIImplementationNotFoundException {
        final Object api = apis.get(interfaceName);
        if (api == null) {
            throw new APIImplementationNotFoundException("No API implementation was found for: " + interfaceName);
        }
        return api;
    }

    @Override
    public boolean needSession(final String interfaceName) {
        return !NO_SESSION_APIS.contains(interfaceName);
    }

}
