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
package org.bonitasoft.engine.core.process.instance.api;

import java.util.List;

import org.bonitasoft.engine.core.process.instance.model.SFlowElementInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAFlowElementInstance;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;

/**
 * @author Emmanuel Duchastenier
 */
public interface FlowElementInstanceService {

    List<SFlowElementInstance> searchFlowElementInstances(Class<? extends PersistentObject> entityClass, QueryOptions searchOptions)
            throws SBonitaReadException;

    /**
     * Retrieves the total number of <code>SFlowElementInstance</code> matching the given search criteria
     * 
     * @param entityClass
     *            the class of the <code>SFlowElementInstance</code> objects to search for.
     * @param countOptions
     *            the search options for this count method
     * @return the number found, 0 if none matching search criteria
     * @throws SBonitaReadException
     * @since 6.0
     */
    long getNumberOfFlowElementInstances(Class<? extends PersistentObject> entityClass, QueryOptions countOptions) throws SBonitaReadException;

    List<SAFlowElementInstance> searchArchivedFlowElementInstances(Class<? extends PersistentObject> entityClass, QueryOptions searchOptions)
            throws SBonitaReadException;

    /**
     * Retrieves the total number of <code>SAFlowElementInstance</code> matching the given search criteria
     * 
     * @param entityClass
     *            the class of the <code>SAFlowElementInstance</code> objects to search for.
     * @param countOptions
     *            the search options for this count method
     * @return the number found, 0 if none matching search criteria
     * @throws SBonitaReadException
     * @since 6.0
     */
    long getNumberOfArchivedFlowElementInstances(Class<? extends PersistentObject> entityClass, QueryOptions countOptions) throws SBonitaReadException;
}
