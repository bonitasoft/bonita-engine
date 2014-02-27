/**
 * Copyright (C) 2011-2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.data.instance;

import java.util.List;

import org.bonitasoft.engine.data.DataSourceImplementation;
import org.bonitasoft.engine.data.instance.exception.SDataInstanceException;
import org.bonitasoft.engine.data.instance.model.SDataInstance;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 */
public interface DataInstanceDataSource extends DataSourceImplementation {

    String DATA_INSTANCE = "DATA_INSTANCE";

    void createDataInstance(final SDataInstance dataInstance) throws SDataInstanceException;

    void updateDataInstance(final SDataInstance dataInstance, final EntityUpdateDescriptor descriptor) throws SDataInstanceException;

    void deleteDataInstance(final SDataInstance dataInstance) throws SDataInstanceException;

    SDataInstance getDataInstance(final long dataInstanceId) throws SDataInstanceException;

    SDataInstance getDataInstance(final String dataName, final long containerId, final String containerType) throws SDataInstanceException;

    /**
     * Returns a paginated list of DataInstance objects
     * 
     * @param containerId
     *            the ID of the container to which the seek data belongs
     * @param containerType
     *            the type of the container to which the seek data belongs
     * @param fromIndex
     *            the begin index of the search
     * @param numberOfResults
     *            the max expected number of results
     * @return the corresponding list
     * @throws SDataInstanceException
     *             in case a search exception occurs.
     */
    List<SDataInstance> getDataInstances(long containerId, String containerType, int fromIndex, int numberOfResults) throws SDataInstanceException;

    List<SDataInstance> getDataInstances(List<Long> dataInstanceIds) throws SDataInstanceException;

}
