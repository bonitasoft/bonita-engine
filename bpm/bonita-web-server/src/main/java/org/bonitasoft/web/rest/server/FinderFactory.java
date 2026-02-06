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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.web.rest.server.api.bdm.BusinessDataReferenceResource;
import org.bonitasoft.web.rest.server.api.bdm.BusinessDataReferenceResourceFinder;
import org.bonitasoft.web.rest.server.api.bdm.BusinessDataReferencesResource;
import org.bonitasoft.web.rest.server.api.bdm.BusinessDataReferencesResourceFinder;
import org.bonitasoft.web.rest.server.api.bpm.cases.ArchivedCaseVariableResource;
import org.bonitasoft.web.rest.server.api.bpm.cases.ArchivedCaseVariableResourceFinder;
import org.bonitasoft.web.rest.server.api.bpm.cases.ArchivedCaseVariablesResource;
import org.bonitasoft.web.rest.server.api.bpm.cases.ArchivedCaseVariablesResourceFinder;
import org.bonitasoft.web.rest.server.api.bpm.flownode.ActivityVariableResource;
import org.bonitasoft.web.rest.server.api.bpm.flownode.ActivityVariableResourceFinder;
import org.bonitasoft.web.rest.server.api.bpm.flownode.archive.ArchivedActivityVariableResource;
import org.bonitasoft.web.rest.server.api.bpm.flownode.archive.ArchivedActivityVariableResourceFinder;
import org.bonitasoft.web.rest.server.api.bpm.message.BPMMessageResource;
import org.bonitasoft.web.rest.server.api.bpm.message.BPMMessageResourceFinder;
import org.bonitasoft.web.rest.server.api.bpm.signal.BPMSignalResource;
import org.bonitasoft.web.rest.server.api.bpm.signal.BPMSignalResourceFinder;
import org.restlet.resource.Finder;
import org.restlet.resource.ServerResource;

/**
 * This class serves:
 * <ul>
 * <li>for Restlet resources to provide access to Engine session and APIS</li>
 * <li>for server to client object conversion (currently used only for APIS that can return Business Data)</li>
 * </ul>
 * See {@link BusinessDataReferenceResourceFinder#toClientObject(Serializable)} for more details.
 */
public class FinderFactory {

    protected final Map<Class<? extends ServerResource>, ResourceFinder> finders;
    final List<ResourceFinder> resourceFinders = new ArrayList<>();

    public FinderFactory() {
        finders = getDefaultFinders();
        createResourceFinderList(finders);
    }

    public FinderFactory(final Map<Class<? extends ServerResource>, ResourceFinder> finders) {
        this.finders = finders;
        createResourceFinderList(finders);

    }

    private void createResourceFinderList(final Map<Class<? extends ServerResource>, ResourceFinder> finders) {
        for (final Map.Entry<Class<? extends ServerResource>, ResourceFinder> classFinderEntry : finders.entrySet()) {
            final ResourceFinder resourceFinder = classFinderEntry.getValue();
            resourceFinders.add(resourceFinder);
            resourceFinder.setFinderFactory(this);
        }
    }

    protected Map<Class<? extends ServerResource>, ResourceFinder> getDefaultFinders() {
        final Map<Class<? extends ServerResource>, ResourceFinder> finders = new HashMap<>();
        finders.put(ActivityVariableResource.class, new ActivityVariableResourceFinder());
        finders.put(BPMMessageResource.class, new BPMMessageResourceFinder());
        finders.put(BPMSignalResource.class, new BPMSignalResourceFinder());
        finders.put(BusinessDataReferenceResource.class, new BusinessDataReferenceResourceFinder());
        finders.put(BusinessDataReferencesResource.class, new BusinessDataReferencesResourceFinder());

        finders.put(ArchivedCaseVariableResource.class, new ArchivedCaseVariableResourceFinder());
        finders.put(ArchivedCaseVariablesResource.class, new ArchivedCaseVariablesResourceFinder());
        finders.put(ArchivedActivityVariableResource.class, new ArchivedActivityVariableResourceFinder());

        return finders;
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

    public ResourceFinder getResourceFinderFor(final Serializable object) {
        for (final ResourceFinder resourceFinder : resourceFinders) {
            if (resourceFinder.handlesResource(object)) {
                return resourceFinder;
            }

        }
        return null;
    }

    public Serializable getContextResultElement(final Serializable object) {
        final ResourceFinder resourceFinderFor = getResourceFinderFor(object);
        if (resourceFinderFor != null) {
            return resourceFinderFor.toClientObject(object);
        }
        return object;
    }

}
