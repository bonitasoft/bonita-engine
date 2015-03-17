/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.service.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.ApplicationAPI;
import org.bonitasoft.engine.api.BusinessDataAPI;
import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.LoginAPI;
import org.bonitasoft.engine.api.PageAPI;
import org.bonitasoft.engine.api.PermissionAPI;
import org.bonitasoft.engine.api.PlatformAPI;
import org.bonitasoft.engine.api.PlatformCommandAPI;
import org.bonitasoft.engine.api.PlatformLoginAPI;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.ProcessConfigurationAPI;
import org.bonitasoft.engine.api.ProfileAPI;
import org.bonitasoft.engine.api.TenantAdministrationAPI;
import org.bonitasoft.engine.api.ThemeAPI;
import org.bonitasoft.engine.api.impl.ApplicationAPIImpl;
import org.bonitasoft.engine.api.impl.BusinessDataAPIImpl;
import org.bonitasoft.engine.api.impl.CommandAPIImpl;
import org.bonitasoft.engine.api.impl.IdentityAPIImpl;
import org.bonitasoft.engine.api.impl.LoginAPIImpl;
import org.bonitasoft.engine.api.impl.PageAPIImpl;
import org.bonitasoft.engine.api.impl.PermissionAPIImpl;
import org.bonitasoft.engine.api.impl.PlatformAPIImpl;
import org.bonitasoft.engine.api.impl.PlatformCommandAPIImpl;
import org.bonitasoft.engine.api.impl.PlatformLoginAPIImpl;
import org.bonitasoft.engine.api.impl.ProcessAPIImpl;
import org.bonitasoft.engine.api.impl.ProcessConfigurationAPIImpl;
import org.bonitasoft.engine.api.impl.ProfileAPIImpl;
import org.bonitasoft.engine.api.impl.TenantAdministrationAPIImpl;
import org.bonitasoft.engine.api.impl.ThemeAPIImpl;
import org.bonitasoft.engine.exception.APIImplementationNotFoundException;
import org.bonitasoft.engine.service.APIAccessResolver;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class APIAccessResolverImpl implements APIAccessResolver {

    private static final Map<String, Object> apis = new HashMap<String, Object>(12);

    private static final List<String> NO_SESSION_APIS = Arrays.asList(PlatformLoginAPI.class.getName(), LoginAPI.class.getName());

    static {
        apis.put(PlatformAPI.class.getName(), new PlatformAPIImpl());
        apis.put(PlatformLoginAPI.class.getName(), new PlatformLoginAPIImpl());
        apis.put(PlatformCommandAPI.class.getName(), new PlatformCommandAPIImpl());
        apis.put(LoginAPI.class.getName(), new LoginAPIImpl());
        apis.put(IdentityAPI.class.getName(), new IdentityAPIImpl());
        apis.put(ProcessAPI.class.getName(), new ProcessAPIImpl());
        apis.put(CommandAPI.class.getName(), new CommandAPIImpl());
        apis.put(ProfileAPI.class.getName(), new ProfileAPIImpl());
        apis.put(ThemeAPI.class.getName(), new ThemeAPIImpl());
        apis.put(PermissionAPI.class.getName(), new PermissionAPIImpl());
        apis.put(PageAPI.class.getName(), new PageAPIImpl());
        apis.put(ApplicationAPI.class.getName(), new ApplicationAPIImpl());
        apis.put(ProcessConfigurationAPI.class.getName(), new ProcessConfigurationAPIImpl());
        apis.put(TenantAdministrationAPI.class.getName(), new TenantAdministrationAPIImpl());
        apis.put(BusinessDataAPI.class.getName(), new BusinessDataAPIImpl());
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
