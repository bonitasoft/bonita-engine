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
package org.bonitasoft.engine.data.instance.api;

import java.util.List;

import org.bonitasoft.engine.data.instance.exception.SDataInstanceException;
import org.bonitasoft.engine.data.instance.model.SDataInstance;
import org.bonitasoft.engine.data.instance.model.archive.SADataInstance;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

/**
 * @author Zhao Na
 * @author Elias Ricken de Medeiros
 * @author Feng Hui
 * @author Matthieu Chaffotte
 * @since 6.0
 */
public interface DataInstanceService {

    // just insert dataInstance to DB
    /**
     * Create dataInstance in DB for given dataInstance
     * 
     * @param dataInstance
     *            SDataInstance object
     * @throws SDataInstanceException
     */
    void createDataInstance(final SDataInstance dataInstance) throws SDataInstanceException;

    /**
     * Update the specific dataInstance according to the given descriptor
     * 
     * @param dataInstance
     *            SDataInstance object will be updated
     * @param descriptor
     *            Update description
     * @throws SDataInstanceException
     */
    void updateDataInstance(final SDataInstance dataInstance, final EntityUpdateDescriptor descriptor) throws SDataInstanceException;

    /**
     * Delete the specific dataInstance
     * 
     * @param dataInstance
     *            SDataInstance object will be deleted
     * @throws SDataInstanceException
     */
    void deleteDataInstance(final SDataInstance dataInstance) throws SDataInstanceException;

    /**
     * Get dataInstance by its id
     * 
     * @param dataInstanceId
     *            Identifier of dataInstance
     * @return a SDataInstance object
     * @throws SDataInstanceException
     */
    SDataInstance getDataInstance(final long dataInstanceId) throws SDataInstanceException;

    /**
     * Get dataInstance visible in the specific container
     * 
     * @param dataName
     *            Name of data instance
     * @param containerId
     *            Identifier of container
     * @param containerType
     *            Type of container, e.g process instance, activity instance and so on.
     * @return a SDataInstance object
     * @throws SDataInstanceException
     */
    SDataInstance getDataInstance(final String dataName, final long containerId, final String containerType,
            final ParentContainerResolver parentContainerResolver) throws SDataInstanceException;

    /**
     * Get dataInstances visible in the specific container for given names
     * 
     * @param dataNames
     *            A list of names of data instances
     * @param containerId
     *            Identifier of container
     * @param containerType
     *            Type of container, e.g process instance, activity instance and so on.
     * @return a list of SDataInstance objects
     * @throws SDataInstanceException
     */
    List<SDataInstance> getDataInstances(final List<String> dataNames, final long containerId, final String containerType,
            final ParentContainerResolver parentContainerResolver) throws SDataInstanceException;

    /**
     * Get all dataInstances visible in the specific container
     * 
     * @param containerId
     *            Identifier of container
     * @param containerType
     *            Type of container, e.g process instance, activity instance and so on.
     * @return
     * @throws SDataInstanceException
     */
    List<SDataInstance> getDataInstances(final long containerId, final String containerType,
            final ParentContainerResolver parentContainerResolver, final int fromIndex, final int numberOfResults)
            throws SDataInstanceException;

    /**
     * Get the local dataInstance by name in a certain container, the dataInstance is existed in this container
     * 
     * @param dataName
     *            Name of dataInstance
     * @param containerId
     *            Identifier of container
     * @param containerType
     *            Type of container, e.g process instance, activity instance and so on.
     * @return an SDataInstance object
     * @throws SDataInstanceException
     */
    SDataInstance getLocalDataInstance(String dataName, long containerId, String containerType) throws SDataInstanceException;

    /**
     * Get a list of local dataInstances for the specific container, those dataInstances must belong to the specified container. This method is paginated.
     * 
     * @param containerId
     *            Identifier of container
     * @param containerType
     *            Type of container, e.g process instance, activity instance and so on.
     * @return a list of SDataInstance objects
     * @throws SDataInstanceException
     */
    List<SDataInstance> getLocalDataInstances(long containerId, String containerType, int fromIndex, int numberOfResults) throws SDataInstanceException;

    /**
     * Get SADataInstance object for specific dataInstance at the specific time
     * 
     * @param sourceObjectId
     *            Identifier of data instance which has been archived
     * @param time
     *            The archive time
     * @return an SADataInstance object
     * @throws SDataInstanceException
     */
    SADataInstance getSADataInstance(long sourceObjectId, long time) throws SDataInstanceException;

    /**
     * Get SADataInstance object archived in the specific time for name specified dataInstance in a container
     * 
     * @param containerId
     *            Identifier of container
     * @param containerType
     *            Type of container, e.g process instance, activity instance and so on.
     * @param dataName
     *            Name of data instance
     * @param time
     *            The archive time
     * @return an SADataInstance object
     * @throws SDataInstanceException
     */
    SADataInstance getSADataInstance(long containerId, String containerType,
            final ParentContainerResolver parentContainerResolver, String dataName, long time) throws SDataInstanceException;

    /**
     * Get all SADataInstance objects archived after specific time for specific dataInstance in a container
     * 
     * @param containerId
     *            Identifier of container
     * @param containerType
     *            Type of container, e.g process instance, activity instance and so on.
     * @param dataNames
     *            Name of data
     * @param time
     *            The archive time
     * @return a list of SADataInstance objects
     * @throws SDataInstanceException
     */
    List<SADataInstance> getSADataInstances(long containerId, String containerType,
            final ParentContainerResolver parentContainerResolver, List<String> dataNames, long time) throws SDataInstanceException;

    /**
     * Get number of dataInstance for specified container
     * 
     * @param containerId
     *            Identifier of container
     * @param containerType
     *            Type of container, e.g process instance, activity instance and so on.
     * @return the number of dataInstances
     * @throws SDataInstanceException
     */
    long getNumberOfDataInstances(long containerId, String containerType,
            final ParentContainerResolver parentContainerResolver) throws SDataInstanceException;


    /**
     * Gets the last archived SADataInstance object for the named data in the container.
     * 
     * @param dataName
     *            the name of the data
     * @param containerId
     *            the identifier of the container
     * @param containerType
     *            the type of the container
     * @return the last archived SADataInstance
     * @throws SDataInstanceException
     */
    SADataInstance getLastSADataInstance(String dataName, long containerId, String containerType,
            final ParentContainerResolver parentContainerResolver) throws SDataInstanceException;

    /**
     * Gets the last archived SADataInstance objects of the container.
     * 
     * @param containerId
     *            the identifier of the container
     * @param containerType
     *            the type of the container
     * @param startIndex
     * @param maxResults
     * @return the last archived SADataInstance
     * @throws SDataInstanceException
     */
    List<SADataInstance> getLastLocalSADataInstances(long containerId, String containerType, int startIndex, int maxResults) throws SDataInstanceException;

    /**
     * Get the local SADataInstances for this element
     * 
     * @param containerId
     * @param containerType
     * @param fromIndex
     * @param maxResults
     * @return
     * @throws SDataInstanceException
     */
    List<SADataInstance> getLocalSADataInstances(long containerId, String containerType, int fromIndex, int maxResults) throws SDataInstanceException;

    /**
     * Delete all local archived data instances for a specified container
     * 
     * @param containerId
     * @param dataInstanceContainerType
     * @throws SDataInstanceException
     * @since 6.1
     */
    void deleteLocalArchivedDataInstances(long containerId, String dataInstanceContainerType) throws SDataInstanceException;

    /**
     * Delete all local active data instances for a specified container
     * 
     * @param containerId
     * @param dataInstanceContainerType
     * @param dataPresent
     * @throws SDataInstanceException
     * @since 6.1
     */
    void deleteLocalDataInstances(long containerId, String dataInstanceContainerType, boolean dataPresent) throws SDataInstanceException;

    void archiveDataInstance(SDataInstance sDataInstance, long archiveDate) throws SDataInstanceException;


}
