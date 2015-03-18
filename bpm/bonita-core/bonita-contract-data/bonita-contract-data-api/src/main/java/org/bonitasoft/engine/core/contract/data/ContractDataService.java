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
package org.bonitasoft.engine.core.contract.data;

import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.persistence.SBonitaReadException;

/**
 * @author Matthieu Chaffotte
 */
public interface ContractDataService {

    void addUserTaskData(final long userTaskId, Map<String, Serializable> data) throws SContractDataCreationException;

    Serializable getUserTaskDataValue(final long userTaskId, String dataName) throws SContractDataNotFoundException, SBonitaReadException;

    void deleteUserTaskData(final long userTaskId) throws SContractDataDeletionException;

    void archiveAndDeleteUserTaskData(final long userTaskId, final long archiveDate) throws SObjectModificationException;

    Serializable getArchivedUserTaskDataValue(final long userTaskId, String dataName) throws SContractDataNotFoundException, SBonitaReadException;

    void addProcessData(long processInstanceId, Map<String, Serializable> data) throws SContractDataCreationException;

    Serializable getProcessDataValue(long processInstanceId, String dataName) throws SContractDataNotFoundException, SBonitaReadException;

    void deleteProcessData(long processInstanceId) throws SContractDataDeletionException;

    void archiveAndDeleteProcessData(long processInstanceId, long archiveDate) throws SObjectModificationException;

    Serializable getArchivedProcessDataValue(long processInstanceId, String dataName) throws SContractDataNotFoundException,
            SBonitaReadException;
}
