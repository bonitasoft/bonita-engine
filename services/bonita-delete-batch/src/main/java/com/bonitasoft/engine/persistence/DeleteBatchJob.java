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
package com.bonitasoft.engine.persistence;

import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.engine.events.model.FireEventException;
import org.bonitasoft.engine.scheduler.JobExecutionException;
import org.bonitasoft.engine.scheduler.SJobConfigurationException;
import org.bonitasoft.engine.scheduler.StatelessJob;
import org.bonitasoft.engine.services.PersistenceService;
import org.bonitasoft.engine.services.SPersistenceException;

/**
 * @author Baptiste Mesta
 */
public class DeleteBatchJob implements StatelessJob {

    private static final long serialVersionUID = 1L;

    private static PersistenceService persistenceService;

    public static void setPersistenceService(final PersistenceService persistenceService) {
        DeleteBatchJob.persistenceService = persistenceService;
    }

    @Override
    public String getName() {
        return "BATCH_DELETE";
    }

    @Override
    public String getDescription() {
        return "Batch delete of flagged elements";
    }

    @Override
    public void execute() throws JobExecutionException, FireEventException {
        try {
            persistenceService.purge();
        } catch (final SPersistenceException e) {
            throw new JobExecutionException(e);
        }
    }

    @Override
    public void setAttributes(final Map<String, Serializable> attributes) throws SJobConfigurationException {
    }

    @Override
    public boolean isWrappedInTransaction() {
        return true;
    }

}
