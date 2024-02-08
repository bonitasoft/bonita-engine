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
package org.bonitasoft.engine.test.persistence.repository;

import static org.bonitasoft.engine.core.process.instance.event.impl.EventInstanceRepositoryImpl.QUERY_RESET_IN_PROGRESS_WAITING_EVENTS;

import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

/**
 * @author Emmanuel Duchastenier
 */
@Repository
public class BPMEventRepository extends TestRepository {

    public BPMEventRepository(final SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    @SuppressWarnings("unchecked")
    public List<Long> getInProgressMessageInstances() {
        Query namedQuery = getNamedQuery("getInProgressMessageInstances");
        return namedQuery.list();
    }

    public int resetProgressMessageInstances() {
        Query namedQuery = getNamedQuery("resetProgressMessageInstances");
        return namedQuery.executeUpdate();
    }

    @SuppressWarnings("unchecked")
    public List<Long> getInProgressWaitingEvents() {
        Query namedQuery = getNamedQuery("getInProgressWaitingEvents");
        return namedQuery.list();
    }

    public int resetInProgressWaitingEvents() {
        Query namedQuery = getNamedQuery(QUERY_RESET_IN_PROGRESS_WAITING_EVENTS);
        return namedQuery.executeUpdate();
    }

    @SuppressWarnings("unchecked")
    public int deleteMessageInstanceByIds(List<Long> ids) {
        Query namedQuery = getNamedQuery("deleteMessageInstanceByIds");

        namedQuery.setParameterList("ids", ids);
        return namedQuery.executeUpdate();
    }

    @SuppressWarnings("unchecked")
    public List<Long> getMessageInstanceIdOlderThanCreationDate(long creationDate) {
        Query namedQuery = getNamedQuery("getMessageInstanceIdOlderThanCreationDate");
        namedQuery.setParameter("creationDate", creationDate);
        return namedQuery.list();
    }
}
