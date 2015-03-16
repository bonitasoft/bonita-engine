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
package org.bonitasoft.engine.core.migration;

import java.util.List;

import org.bonitasoft.engine.core.migration.exceptions.SInvalidMigrationPlanException;
import org.bonitasoft.engine.core.migration.exceptions.SMigrationPlanCreationException;
import org.bonitasoft.engine.core.migration.exceptions.SMigrationPlanDeletionException;
import org.bonitasoft.engine.core.migration.exceptions.SMigrationPlanNotFoundException;
import org.bonitasoft.engine.core.migration.exceptions.SPrepareForMigrationFailedException;
import org.bonitasoft.engine.core.migration.model.SMigrationPlan;
import org.bonitasoft.engine.core.migration.model.SMigrationPlanDescriptor;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.SBonitaReadException;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public interface MigrationPlanService {

    String MIGRATION_PLAN_DESCRIPTOR = "MIGRATION_PLAN_DESCRIPTOR";

    String ADDED_A_NEW_MIGRATION_PLAN = "Added a new Migration plan";

    String DELETED_MIGRATION_PLAN = "deleted migration plan";

    /**
     * Import a migration plan in the engine
     * 
     * @param content
     *            of the migration plan
     * @return the imported migration plan
     */
    SMigrationPlanDescriptor importMigrationPlan(byte[] content) throws SInvalidMigrationPlanException, SMigrationPlanCreationException;

    /**
     * Get the content of a migration plan
     * 
     * @param id
     *            of the migration plan
     * @return the content of the migration plan
     * @throws SBonitaReadException
     * @throws SMigrationPlanNotFoundException
     */
    byte[] exportMigrationPlan(long id) throws SBonitaReadException, SMigrationPlanNotFoundException;

    /**
     * Get available migration plan
     * 
     * @param pageIndex
     * @param numberPerPage
     * @param pagingCriterion
     * @return paginated list of migration plan
     * @throws SBonitaReadException
     */
    List<SMigrationPlanDescriptor> getMigrationPlanDescriptors(int pageIndex, int numberPerPage, String field, OrderByType order) throws SBonitaReadException;

    /**
     * Get a migration plan using its id
     * 
     * @param id
     *            of the migration plan to retrieve
     * @return the migration plan
     * @throws SBonitaReadException
     * @throws SInvalidMigrationPlanException
     * @throws SMigrationPlanNotFoundException
     */
    SMigrationPlan getMigrationPlan(long id) throws SBonitaReadException, SInvalidMigrationPlanException, SMigrationPlanNotFoundException;

    /**
     * Delete a migration plan
     * 
     * @param id
     *            of the migration plan
     * @throws SBonitaReadException
     * @throws SMigrationPlanNotFoundException
     * @throws SMigrationPlanDeletionException
     */
    void deleteMigrationPlan(long id) throws SBonitaReadException, SMigrationPlanNotFoundException, SMigrationPlanDeletionException;

    /**
     * @param id
     * @return
     * @throws SBonitaReadException
     * @throws SMigrationPlanNotFoundException
     */
    SMigrationPlanDescriptor getMigrationPlanDescriptor(final long id) throws SBonitaReadException, SMigrationPlanNotFoundException;

    /**
     * @param id
     */
    long getNumberOfMigrationPlan() throws SBonitaReadException;

    /**
     * @param processInstanceIds
     * @param migrationPlanId
     * @throws SMigrationPlanNotFoundException
     * @throws SBonitaReadException
     * @throws SInvalidMigrationPlanException
     * @throws SProcessInstanceReadException
     * @throws SProcessInstanceNotFoundException
     */
    void prepareProcessesForMigration(List<Long> processInstanceIds, long migrationPlanId) throws SPrepareForMigrationFailedException;

}
