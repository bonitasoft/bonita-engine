/**
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package com.bonitasoft.engine.service.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.LoginAPI;
import org.bonitasoft.engine.api.MigrationAPI;
import org.bonitasoft.engine.api.MonitoringAPI;
import org.bonitasoft.engine.api.PlatformAPI;
import org.bonitasoft.engine.api.PlatformCommandAPI;
import org.bonitasoft.engine.api.PlatformLoginAPI;
import org.bonitasoft.engine.api.PlatformMonitoringAPI;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.impl.CommandAPIImpl;
import org.bonitasoft.engine.api.impl.IdentityAPIImpl;
import org.bonitasoft.engine.api.impl.MigrationAPIImpl;
import org.bonitasoft.engine.api.impl.MonitoringAPIImpl;
import org.bonitasoft.engine.api.impl.PlatformCommandAPIImpl;
import org.bonitasoft.engine.api.impl.PlatformLoginAPIImpl;
import org.bonitasoft.engine.api.impl.PlatformMonitoringAPIImpl;
import org.bonitasoft.engine.exception.APIImplementationNotFoundException;
import org.bonitasoft.engine.service.APIAccessResolver;

import com.bonitasoft.engine.api.LogAPI;
import com.bonitasoft.engine.api.impl.LogAPIExt;
import com.bonitasoft.engine.api.impl.LoginAPIExt;
import com.bonitasoft.engine.api.impl.PlatformAPIExt;
import com.bonitasoft.engine.api.impl.ProcessAPIExt;

/**
 * @author Matthieu Chaffotte
 */
public class APIAccessResolverImpl implements APIAccessResolver {

    private static final Map<String, Object> apis = new HashMap<String, Object>(12);

    private static final List<String> NO_SESSION_APIS = Arrays.asList(PlatformLoginAPI.class.getName(), LoginAPI.class.getName(),
            com.bonitasoft.engine.api.LoginAPI.class.getName());

    static {
        apis.put(PlatformAPI.class.getName(), new PlatformAPIExt());
        apis.put(com.bonitasoft.engine.api.PlatformAPI.class.getName(), new PlatformAPIExt());
        apis.put(PlatformLoginAPI.class.getName(), new PlatformLoginAPIImpl());
        apis.put(PlatformMonitoringAPI.class.getName(), new PlatformMonitoringAPIImpl());
        apis.put(LoginAPI.class.getName(), new LoginAPIExt());
        apis.put(com.bonitasoft.engine.api.LoginAPI.class.getName(), new LoginAPIExt());
        apis.put(IdentityAPI.class.getName(), new IdentityAPIImpl());
        apis.put(MonitoringAPI.class.getName(), new MonitoringAPIImpl());
        apis.put(ProcessAPI.class.getName(), new ProcessAPIExt());
        apis.put(com.bonitasoft.engine.api.ProcessAPI.class.getName(), new ProcessAPIExt());
        apis.put(MigrationAPI.class.getName(), new MigrationAPIImpl());
        apis.put(LogAPI.class.getName(), new LogAPIExt());
        apis.put(CommandAPI.class.getName(), new CommandAPIImpl());
        apis.put(PlatformCommandAPI.class.getName(), new PlatformCommandAPIImpl());
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
