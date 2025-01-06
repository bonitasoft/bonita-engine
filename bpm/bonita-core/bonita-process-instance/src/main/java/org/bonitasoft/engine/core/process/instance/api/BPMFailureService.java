/**
 * Copyright (C) 2024 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.instance.model.SABPMFailure;
import org.bonitasoft.engine.core.process.instance.model.SBPMFailure;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.services.SPersistenceException;

public interface BPMFailureService {

    SBPMFailure createFlowNodeFailure(SFlowNodeInstance flowNodeInstance,
            Failure failure) throws SPersistenceException;

    List<SBPMFailure> getFlowNodeFailures(long flowNodeInstanceId, int maxResults) throws SBonitaReadException;

    void archiveFlowNodeFailures(long flowNodeInstanceId, long archiveDate) throws SBonitaException;

    void deleteFlowNodeFailures(long flowNodeInstanceId) throws SBonitaException;

    void deleteArchivedFlowNodeFailures(List<Long> flowNodeInstanceIds) throws SBonitaException;

    List<SABPMFailure> getArchivedFlowNodeFailures(long flowNodeInstanceId, int maxResults) throws SBonitaReadException;

    SBPMFailure createProcessInstanceFailure(SProcessInstance processInstance, Failure failure)
            throws SPersistenceException;

    List<SBPMFailure> getProcessInstanceFailures(long processInstanceId, int maxResults) throws SBonitaReadException;

    void archiveProcessInstanceFailures(long processInstanceId, long archiveDate) throws SBonitaException;

    void deleteProcessInstanceFailures(long processInstanceId) throws SBonitaException;

    void deleteArchivedProcessInstanceFailures(List<Long> processInstanceIds) throws SBonitaException;

    List<SABPMFailure> getArchivedProcessInstanceFailures(long processInstanceId, int maxResults)
            throws SBonitaReadException;

    List<SBPMFailure> getSubProcessInstanceFailures(long rootProcessInstanceId, int maxResults)
            throws SBonitaReadException;

    List<SABPMFailure> getArchivedSubProcessInstanceFailures(long rootProcessInstanceId, int maxResults)
            throws SBonitaReadException;

    record Failure(String scope, Throwable throwable){}
}
