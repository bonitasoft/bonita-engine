/**
 * Copyright (C) 2015 Bonitasoft S.A.
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
package org.bonitasoft.engine.test.persistence.repository;

import java.util.List;

import org.bonitasoft.engine.resources.BARResourceType;
import org.bonitasoft.engine.resources.SBARResource;
import org.bonitasoft.engine.resources.SBARResourceLight;
import org.hibernate.Query;
import org.hibernate.SessionFactory;

public class ProcessResourceRepository extends TestRepository {

    public ProcessResourceRepository(final SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    public SBARResource getBARResource(long processDefinitionId, BARResourceType type, String name) {
        final Query namedQuery = getNamedQuery("getBARResource");
        namedQuery.setParameter("processDefinitionId", processDefinitionId);
        namedQuery.setParameter("type", type);
        namedQuery.setParameter("name", name);
        return (SBARResource) namedQuery.uniqueResult();
    }
    public List<SBARResource> getBARResourcesOfType(long processDefinitionId, BARResourceType type) {
        final Query namedQuery = getNamedQuery("getBARResourcesOfType");
        namedQuery.setParameter("processDefinitionId", processDefinitionId);
        namedQuery.setParameter("type", type);
        return namedQuery.list();
    }
    public List<SBARResourceLight> getBARResourcesLightOfType(long processDefinitionId, BARResourceType type) {
        final Query namedQuery = getNamedQuery("getBARResourcesLightOfType");
        namedQuery.setParameter("processDefinitionId", processDefinitionId);
        namedQuery.setParameter("type", type);
        return namedQuery.list();
    }
    public long getNumberOfBARResourcesOfType(long processDefinitionId, BARResourceType type) {
        final Query namedQuery = getNamedQuery("getNumberOfBARResourcesOfType");
        namedQuery.setParameter("processDefinitionId", processDefinitionId);
        namedQuery.setParameter("type", type);
        return (long) namedQuery.uniqueResult();
    }



}
