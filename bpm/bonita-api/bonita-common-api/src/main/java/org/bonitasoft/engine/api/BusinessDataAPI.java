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
package org.bonitasoft.engine.api;

import java.util.List;

import org.bonitasoft.engine.bpm.data.DataNotFoundException;
import org.bonitasoft.engine.business.data.BusinessDataReference;

/**
 * This API allows to list the {@link org.bonitasoft.engine.business.data.BusinessDataReference} related to specific {@link org.bonitasoft.engine.bpm.process.ProcessInstance}
 * @author Elias Ricken de Medeiros
 * @author Laurent Leseigneur
 * @since 7.0.0
 * @see org.bonitasoft.engine.business.data.BusinessDataReference
 * @see org.bonitasoft.engine.bpm.process.ProcessInstance
 */
public interface BusinessDataAPI {

    /**
     * Returns the {@link org.bonitasoft.engine.business.data.BusinessDataReference} of the named business data of the process instance.
     * The value is returned in a DataInstance object.
     *
     * @param businessDataName
     *        The name of the business data
     * @param processInstanceId
     *        The identifier of the process instance
     * @return the reference of the business data
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid, e.g. the session has expired.
     * @throws org.bonitasoft.engine.bpm.data.DataNotFoundException
     *         If the specified business data value cannot be found.
     */
    BusinessDataReference getProcessBusinessDataReference(String businessDataName, long processInstanceId) throws DataNotFoundException;

    /**
     * Lists the paginated @link BusinessDataReference}s of the process instance order by identifier.
     *
     * @param processInstanceId
     *        The identifier of the process instance
     * @param startIndex
     *        the index of the first result (starting from 0).
     * @param maxResults
     *        the maximum number of result per page
     * @return the paginated references of the business data
     */
    List<BusinessDataReference> getProcessBusinessDataReferences(long processInstanceId, int startIndex, int maxResults);

}
