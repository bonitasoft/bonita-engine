/**
 * Copyright (C) 2016 Bonitasoft S.A.
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

import org.bonitasoft.engine.resources.STenantResource;
import org.bonitasoft.engine.resources.STenantResourceLight;
import org.bonitasoft.engine.resources.TenantResourceType;
import org.hibernate.Query;
import org.hibernate.SessionFactory;

public class TenantResourceRepository extends TestRepository {

    public TenantResourceRepository(final SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    public STenantResource getTenantResource(TenantResourceType type, String name) {
        final Query namedQuery = getNamedQuery("getTenantResource");
        namedQuery.setParameter("type", type);
        namedQuery.setParameter("name", name);
        return (STenantResource) namedQuery.uniqueResult();
    }
    public List<STenantResource> getTenantResourcesOfType(TenantResourceType type) {
        final Query namedQuery = getNamedQuery("getTenantResourcesOfType");
        namedQuery.setParameter("type", type);
        return namedQuery.list();
    }
    public List<STenantResourceLight> getTenantResourcesLightOfType(TenantResourceType type) {
        final Query namedQuery = getNamedQuery("getTenantResourcesLightOfType");
        namedQuery.setParameter("type", type);
        return namedQuery.list();
    }
    public long getNumberOfTenantResourcesOfType(TenantResourceType type) {
        final Query namedQuery = getNamedQuery("getNumberOfTenantResourcesOfType");
        namedQuery.setParameter("type", type);
        return (long) namedQuery.uniqueResult();
    }



}
