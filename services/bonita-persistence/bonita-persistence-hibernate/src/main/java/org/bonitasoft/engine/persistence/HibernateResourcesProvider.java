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
package org.bonitasoft.engine.persistence;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public class HibernateResourcesProvider {

    private Set<String> resources = new HashSet<String>();

    private Map<? extends String, ? extends String> classAliasMappings = new HashMap<String, String>();

    public void setResources(final Set<String> resources) {
        final Set<String> hashSet = new HashSet<String>(resources.size());
        for (final String resource : resources) {
            hashSet.add(resource.trim());
        }
        this.resources = hashSet;
    }

    public Set<String> getResources() {
        return resources;
    }

    public Map<? extends String, ? extends String> getClassAliasMappings() {
        return classAliasMappings;
    }

    public void setClassAliasMappings(final Map<? extends String, ? extends String> classAliasMappings) {
        this.classAliasMappings = classAliasMappings;
    }
}
