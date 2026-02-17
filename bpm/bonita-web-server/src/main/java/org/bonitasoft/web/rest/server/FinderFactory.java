/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.web.rest.server;

import java.util.HashMap;
import java.util.Map;

import org.restlet.resource.Finder;
import org.restlet.resource.ServerResource;

/**
 * Provides Restlet resource finders for remaining Restlet-based REST API endpoints.
 */
public class FinderFactory {

    protected final Map<Class<? extends ServerResource>, ResourceFinder> finders;

    public FinderFactory() {
        finders = getDefaultFinders();
    }

    public FinderFactory(final Map<Class<? extends ServerResource>, ResourceFinder> finders) {
        this.finders = finders;
    }

    protected Map<Class<? extends ServerResource>, ResourceFinder> getDefaultFinders() {
        return new HashMap<>();
    }

    public Finder create(final Class<? extends ServerResource> clazz) {
        final Finder finder = finders.get(clazz);
        if (finder == null) {
            throw new RuntimeException("Finder unimplemented for class " + clazz);
        }
        return finder;
    }

    public Finder createExtensionResource() {
        return new ApiExtensionResourceFinder();
    }

}
