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
package org.bonitasoft.engine.supervisor.mapping;

import java.util.List;

import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.supervisor.mapping.model.SProcessSupervisor;

/**
 * @author Yanyan Liu
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 * @since 6.0
 */
public interface SupervisorMappingService {

    String SUPERVISOR = "SUPERVISOR";

    /**
     * Create supervisor in DB according to the given supervisor
     *
     * @param supervisor
     *        a SSupervisor object
     * @return the new created supervisor
     * @throws SSupervisorCreationException
     */
    SProcessSupervisor createProcessSupervisor(SProcessSupervisor supervisor) throws SSupervisorCreationException;

    /**
     * get supervisor without display name by its id
     *
     * @param supervisorId
     *        identifier of supervisor
     * @return the supervisor with id equals the parameter
     * @throws SSupervisorNotFoundException
     */
    SProcessSupervisor getProcessSupervisor(long supervisorId) throws SSupervisorNotFoundException;

    /**
     * Delete the id specified supervisor
     *
     * @param supervisorId
     *        identifier of supervisor
     * @throws SSupervisorNotFoundException
     * @throws SSupervisorDeletionException
     */
    void deleteProcessSupervisor(long supervisorId) throws SSupervisorNotFoundException, SSupervisorDeletionException;

    /**
     * Delete the specific supervisor
     *
     * @param supervisor
     *        the supervisor will be deleted
     * @throws SSupervisorDeletionException
     */
    void deleteProcessSupervisor(SProcessSupervisor supervisor) throws SSupervisorDeletionException;

    /**
     * Delete all supervisors for the connected tenant
     *
     * @throws SSupervisorDeletionException
     * @since 6.1
     */
    void deleteAllProcessSupervisors() throws SSupervisorDeletionException;

    /**
     * Verify if the id specified user is the supervisor of id specified process definition
     *
     * @param processDefinitionId
     *        identifier of process definition
     * @param userId
     *        identifier of user
     * @return true if user is supervisor of the process, false otherwise
     * @throws SBonitaReadException
     */
    Boolean isProcessSupervisor(long processDefinitionId, long userId) throws SBonitaReadException;

    /**
     * Search all supervisors suit to the specific criteria
     *
     * @param queryOptions
     *        The QueryOptions object containing some query conditions
     * @return a list of SSupervisor objects
     * @throws SBonitaReadException
     */
    List<SProcessSupervisor> searchProcessSupervisors(QueryOptions queryOptions) throws SBonitaReadException;

    /**
     * Get total number of supervisors suit to the specific criteria
     *
     * @param searchOptions
     *        The QueryOptions object containing some query conditions
     * @return a list of SSupervisor objects
     * @throws SBonitaReadException
     */
    long getNumberOfProcessSupervisors(QueryOptions searchOptions) throws SBonitaReadException;

}
