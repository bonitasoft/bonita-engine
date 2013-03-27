/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package com.bonitasoft.engine.api;

import java.util.List;

import org.bonitasoft.engine.exception.BonitaReadException;
import org.bonitasoft.engine.exception.InvalidMigrationPlanException;
import org.bonitasoft.engine.exception.InvalidSessionException;
import org.bonitasoft.engine.exception.MigrationPlanCreationException;
import org.bonitasoft.engine.exception.MigrationPlanDeletionException;
import org.bonitasoft.engine.exception.MigrationPlanNotFoundException;
import org.bonitasoft.engine.exception.PageOutOfRangeException;
import org.bonitasoft.engine.exception.PrepareForMigrationFailedException;
import org.bonitasoft.engine.migration.MigrationPlan;
import org.bonitasoft.engine.migration.MigrationPlanCriterion;
import org.bonitasoft.engine.migration.MigrationPlanDescriptor;

/**
 * @author Baptiste Mesta
 */
public interface MigrationAPI {

    /**
     * Import a migration plan in the engine
     * 
     * @param content
     *            of the migration plan
     * @return the imported migration plan
     * @throws InvalidMigrationPlanException
     * @throws MigrationPlanCreationException
     */
    MigrationPlanDescriptor importMigrationPlan(byte[] content) throws InvalidSessionException, InvalidMigrationPlanException, MigrationPlanCreationException;

    /**
     * Get the content of a migration plan
     * 
     * @param id
     *            of the migration plan
     * @return the content of the migration plan
     * @throws MigrationPlanNotFoundException
     * @throws BonitaReadException
     */
    byte[] exportMigrationPlan(long id) throws InvalidSessionException, MigrationPlanNotFoundException, BonitaReadException;

    /**
     * Get available migration plan
     * 
     * @param pageIndex
     * @param numberPerPage
     * @param pagingCriterion
     * @return paginated list of migration plan
     * @throws BonitaReadException
     * @throws PageOutOfRangeException
     */
    List<MigrationPlanDescriptor> getMigrationPlanDescriptors(int pageIndex, int numberPerPage, MigrationPlanCriterion pagingCriterion)
            throws InvalidSessionException, BonitaReadException, PageOutOfRangeException;

    long getNumberOfMigrationPlan() throws InvalidSessionException, BonitaReadException;

    /**
     * Get a migration plan using its id
     * 
     * @param id
     *            of the migration plan to retrieve
     * @return the migration plan
     * @throws MigrationPlanNotFoundException
     * @throws BonitaReadException
     */
    MigrationPlan getMigrationPlan(long id) throws InvalidSessionException, BonitaReadException, MigrationPlanNotFoundException;

    /**
     * Delete a migration plan
     * 
     * @param id
     *            of the migration plan
     * @throws BonitaReadException
     * @throws MigrationPlanNotFoundException
     * @throws MigrationPlanDeletionException
     */
    void deleteMigrationPlan(long id) throws InvalidSessionException, MigrationPlanNotFoundException, BonitaReadException, MigrationPlanDeletionException;

    /**
     * prepare the migration of these process instances according to the specified migration plan
     * 
     * @param processInstanceIds
     *            process instances to prepare for migration
     * @param migrationPlanId
     *            the id of the migration plan
     * @throws InvalidSessionException
     * @throws MigrationPlanNotFoundException
     * @throws BonitaReadException
     * @throws PrepareForMigrationFailedException
     */
    void prepareProcessesForMigration(List<Long> processInstanceIds, long migrationPlanId) throws InvalidSessionException, PrepareForMigrationFailedException;

}
