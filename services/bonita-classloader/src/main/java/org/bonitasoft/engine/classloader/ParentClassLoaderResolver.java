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
package org.bonitasoft.engine.classloader;

import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.springframework.stereotype.Component;

@Component
public class ParentClassLoaderResolver {

    /**
     * @return the key of the parent or null if it is the global
     */
    public ClassLoaderIdentifier getParentClassLoaderIdentifier(ClassLoaderIdentifier childId) {
        if (ScopeType.PROCESS.equals(childId.getType())) {
            // We should not depend on the session to know what is the parent of a classloader
            return ClassLoaderIdentifier.TENANT;
        } else if (ScopeType.TENANT.equals(childId.getType())) {
            return ClassLoaderIdentifier.GLOBAL;
        } else if (ClassLoaderIdentifier.GLOBAL.equals(childId)) {
            return ClassLoaderIdentifier.APPLICATION;
        } else {
            throw new BonitaRuntimeException("unable to find a parent for type: " + childId);
        }
    }
}
