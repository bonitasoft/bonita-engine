/**
 * Copyright (C) 2012-2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.api.impl.transaction.process;

import java.util.List;

import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceModificationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceReadException;
import org.bonitasoft.engine.lock.SLockException;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * @author Zhang Bole
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 */
public class DeleteArchivedProcessInstances implements TransactionContent {

    private static final int BATCH_SIZE = 100;

    private final ProcessInstanceService processInstanceService;

    private final long processDefinitionId;

    private final ArchiveService archiveService;

    public DeleteArchivedProcessInstances(final TenantServiceAccessor tenantAccessor, final long processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
        processInstanceService = tenantAccessor.getProcessInstanceService();
        archiveService = tenantAccessor.getArchiveService();
    }

    @Override
    public void execute() throws SBonitaException {
        deleteArchivedProcessInstancesFromDefinition(processDefinitionId);
    }

    public void deleteArchivedProcessInstancesFromDefinition(final long processDefinitionId) throws SFlowNodeReadException,
            SProcessInstanceModificationException, SProcessInstanceReadException, SProcessInstanceNotFoundException, SLockException {
        List<Long> sourceProcessInstanceIds;
        final ReadPersistenceService archivePersistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        if (archivePersistenceService != null) {
            do {
                // from index always will be zero because elements will be deleted
                sourceProcessInstanceIds = processInstanceService.getSourceProcesInstanceIdsOfArchProcessInstancesFromDefinition(archivePersistenceService,
                        processDefinitionId, 0, BATCH_SIZE, OrderByType.DESC);
                for (final Long orgProcessId : sourceProcessInstanceIds) {
                    processInstanceService.deleteArchivedProcessInstanceElements(orgProcessId, processDefinitionId);
                }

            } while (!sourceProcessInstanceIds.isEmpty());
        }
    }

    protected long getProcessDefinitionId() {
        return processDefinitionId;
    }

}
