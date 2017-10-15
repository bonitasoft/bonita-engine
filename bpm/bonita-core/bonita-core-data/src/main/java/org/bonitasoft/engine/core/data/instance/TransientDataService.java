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
package org.bonitasoft.engine.core.data.instance;

import java.util.List;

import org.bonitasoft.engine.data.instance.exception.SDataInstanceException;
import org.bonitasoft.engine.data.instance.model.SDataInstance;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

public interface TransientDataService {

    /**
     * @param dataNames
     * @param containerId
     * @param containerType
     * @return
     * @throws SDataInstanceException
     */
    List<SDataInstance> getDataInstances(List<String> dataNames, long containerId, String containerType) throws SDataInstanceException;

    /**
     * @param dataInstance
     * @throws SDataInstanceException
     */
    void createDataInstance(SDataInstance dataInstance) throws SDataInstanceException;

    /**
     * @param dataInstance
     * @param descriptor
     * @throws SDataInstanceException
     */
    void updateDataInstance(SDataInstance dataInstance, EntityUpdateDescriptor descriptor) throws SDataInstanceException;

    /**
     * @param dataInstance
     * @throws SDataInstanceException
     */
    void deleteDataInstance(SDataInstance dataInstance) throws SDataInstanceException;

    /**
     * @param dataInstanceId
     * @return
     * @throws SDataInstanceException
     */
    SDataInstance getDataInstance(long dataInstanceId) throws SDataInstanceException;

    /**
     * @param dataName
     * @param containerId
     * @param containerType
     * @return
     * @throws SDataInstanceException
     */
    SDataInstance getDataInstance(String dataName, long containerId, String containerType) throws SDataInstanceException;

    /**
     * @param containerId
     * @param containerType
     * @param fromIndex
     * @param numberOfResults
     * @return
     * @throws SDataInstanceException
     */
    List<SDataInstance> getDataInstances(long containerId, String containerType, int fromIndex, int numberOfResults) throws SDataInstanceException;

    /**
     * @param dataInstanceIds
     * @return
     */
    List<SDataInstance> getDataInstances(List<Long> dataInstanceIds);

}
