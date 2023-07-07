/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.api.*;
import org.bonitasoft.engine.api.impl.*;
import org.bonitasoft.engine.exception.APIImplementationNotFoundException;
import org.bonitasoft.engine.service.APIAccessResolver;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class APIAccessResolverImpl implements APIAccessResolver {

    private static final Map<String, Object> apis = new HashMap<>();

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
        apis.put(TenantAdministrationAPI.class.getName(), new TenantAdministrationAPIImpl());
        apis.put(BusinessDataAPI.class.getName(), new BusinessDataAPIImpl());
        apis.put(TemporaryContentAPI.class.getName(), new TemporaryContentAPIImpl());
    }

    @Override
    public <T> T getAPIImplementation(Class<T> apiInterface) throws APIImplementationNotFoundException {
        final Object api = getApiImplementation(apiInterface);
        if (api == null) {
            throw new APIImplementationNotFoundException(
                    "No API implementation was found for: " + apiInterface.getName());
        }
        return apiInterface.cast(api);
    }

    protected Object getApiImplementation(Class<?> apiInterface) {
        return apis.get(apiInterface.getName());
    }

}
