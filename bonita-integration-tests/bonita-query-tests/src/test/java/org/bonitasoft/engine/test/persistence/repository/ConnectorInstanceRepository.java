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
package org.bonitasoft.engine.test.persistence.repository;

import java.util.List;

import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.core.process.instance.model.SConnectorInstance;
import org.bonitasoft.engine.core.process.instance.model.SConnectorInstanceWithFailureInfo;
import org.hibernate.Query;
import org.hibernate.SessionFactory;

public class ConnectorInstanceRepository extends TestRepository {

    public ConnectorInstanceRepository(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    @SuppressWarnings("unchecked")
    public List<SConnectorInstance> getConnectorInstances(final long containerId, final String containerType, long tenantId) {
        getSession().enableFilter("tenantFilter").setParameter("tenantId", tenantId);
        Query namedQuery = getNamedQuery("getConnectorInstances");
        namedQuery.setParameter("containerId", containerId);
        namedQuery.setParameter("containerType", containerType);
        return namedQuery.list();
    }

    @SuppressWarnings("unchecked")
    public List<SConnectorInstance> getConnectorInstancesOrderedById(final long containerId, final String containerType, long tenantId) {
        getSession().enableFilter("tenantFilter").setParameter("tenantId", tenantId);
        Query namedQuery = getNamedQuery("getConnectorInstancesOrderedById");
        namedQuery.setParameter("containerId", containerId);
        namedQuery.setParameter("containerType", containerType);
        return namedQuery.list();
    }

    public List<SConnectorInstanceWithFailureInfo> getConnectorInstancesWithFailureInfo(final long containerId, final String containerType, String state,
            long tenantId) {
        getSession().enableFilter("tenantFilter").setParameter("tenantId", tenantId);
        Query namedQuery = getNamedQuery("getConnectorInstancesWithFailureInfoInState");
        namedQuery.setParameter("containerId", containerId);
        namedQuery.setParameter("containerType", containerType);
        namedQuery.setParameter("state", state);
        return namedQuery.list();
    }

    public long getNumberOfConnectorInstances(final long containerId, final String containerType, long tenantId) {
        getSession().enableFilter("tenantFilter").setParameter("tenantId", tenantId);
        Query namedQuery = getNamedQuery("getNumberOfConnectorInstances");
        namedQuery.setParameter("containerId", containerId);
        namedQuery.setParameter("containerType", containerType);
        return ((Number) namedQuery.uniqueResult()).longValue();
    }

    @SuppressWarnings("unchecked")
    public SConnectorInstance getNextExecutableConnectorInstance(final long containerId, final String containerType, final ConnectorEvent activationEvent,
            long tenantId) {
        getSession().enableFilter("tenantFilter").setParameter("tenantId", tenantId);
        Query namedQuery = getNamedQuery("getNextExecutableConnectorInstance");
        namedQuery.setParameter("containerId", containerId);
        namedQuery.setParameter("containerType", containerType);
        namedQuery.setParameter("activationEvent", activationEvent);
        List<SConnectorInstance> results = namedQuery.list();
        return (results != null && results.size() > 0) ? results.get(0) : null;
    }

    @SuppressWarnings("unchecked")
    public List<SConnectorInstance> searchSConnectorInstance(final long containerId, final String containerType, long tenantId) {
        getSession().enableFilter("tenantFilter").setParameter("tenantId", tenantId);
        Query namedQuery = getNamedQuery("searchSConnectorInstance");
        return namedQuery.list();
    }

    public long getNumberOfSConnectorInstance(final long containerId, final String containerType, long tenantId) {
        getSession().enableFilter("tenantFilter").setParameter("tenantId", tenantId);
        Query namedQuery = getNamedQuery("getNumberOfSConnectorInstance");
        return ((Number) namedQuery.uniqueResult()).longValue();
    }

    @SuppressWarnings("unchecked")
    public List<SConnectorInstance> getConnectorInstances(final long containerId, final String containerType, final ConnectorEvent activationEvent,
            String state, long tenantId) {
        getSession().enableFilter("tenantFilter").setParameter("tenantId", tenantId);
        Query namedQuery = getNamedQuery("getConnectorInstancesWithState");
        namedQuery.setParameter("containerId", containerId);
        namedQuery.setParameter("containerType", containerType);
        namedQuery.setParameter("activationEvent", activationEvent);
        namedQuery.setParameter("state", state);
        return namedQuery.list();
    }

    @SuppressWarnings("unchecked")
    public List<SConnectorInstanceWithFailureInfo> getConnectorInstanceWithFailureInfo(final long containerId, long tenantId) {
        getSession().enableFilter("tenantFilter").setParameter("tenantId", tenantId);
        Query namedQuery = getNamedQuery("getConnectorInstanceWithFailureInfo");
        namedQuery.setParameter("id", containerId);
        return namedQuery.list();
    }
}
