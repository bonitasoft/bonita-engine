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
package org.bonitasoft.engine.bpm.classloader;

import org.bonitasoft.engine.classloader.ClassLoaderIdentifier;
import org.bonitasoft.engine.classloader.ParentClassLoaderResolver;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.bonitasoft.engine.sessionaccessor.STenantIdNotSetException;

public class BonitaBPMParentClassLoaderResolver implements ParentClassLoaderResolver {

    private final ReadSessionAccessor sessionAccessor;

    public BonitaBPMParentClassLoaderResolver(final ReadSessionAccessor sessionAccessor) {
        this.sessionAccessor = sessionAccessor;
    }

    @Override
    public ClassLoaderIdentifier getParentClassLoaderIdentifier(ClassLoaderIdentifier childId) {
        if (ScopeType.PROCESS.name().equals(childId.getType())) {
            try {
                return new ClassLoaderIdentifier(ScopeType.TENANT.name(), sessionAccessor.getTenantId());
            } catch (final STenantIdNotSetException e) {
                throw new BonitaRuntimeException("No tenant id set while creating the process classloader: " + childId);
            }
        } else if (ScopeType.TENANT.name().equals(childId.getType())) {
            return ClassLoaderIdentifier.GLOBAL;//global
        } else if ("___datasource___".equals(childId.getType())) {
            return ClassLoaderIdentifier.GLOBAL;
        } else {
            throw new BonitaRuntimeException("unable to find a parent for type: " + childId);
        }
    }
}
