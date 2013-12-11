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
package org.bonitasoft.engine.service.impl;

import org.bonitasoft.engine.api.*;
import org.bonitasoft.engine.api.impl.*;
import org.bonitasoft.engine.exception.APIImplementationNotFoundException;
import org.bonitasoft.engine.service.APIAccessResolver;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Matthieu Chaffotte
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
        apis.put(RepairAPI.class.getName(), new RepairAPIImpl());
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
