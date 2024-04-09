/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.web.rest.server.engineclient;

import java.util.List;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.data.DataDefinition;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinitionNotFoundException;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfoSearchDescriptor;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfoUpdater;
import org.bonitasoft.engine.bpm.process.V6FormDeployException;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.ProcessInstanceHierarchicalDeletionException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.web.rest.model.bpm.process.ProcessItem;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIForbiddenException;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIItemNotFoundException;
import org.bonitasoft.web.toolkit.client.common.exception.api.APINotFoundException;
import org.bonitasoft.web.toolkit.client.common.i18n.T_;
import org.bonitasoft.web.toolkit.client.common.texttemplate.Arg;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Process engine API client
 * Wrapper to processAPI for all process methods
 *
 * @author Colin PUY
 */
public class ProcessEngineClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessEngineClient.class.getName());

    private static final int DELETE_PROCESS_BUNCH_SIZE = 100;

    protected final ProcessAPI processAPI;

    public ProcessEngineClient(final ProcessAPI processAPI) {
        this.processAPI = processAPI;
    }

    public ProcessDeploymentInfo getProcessDeploymentInfo(final long processId) {
        try {
            return getProcessApi().getProcessDeploymentInfo(processId);
        } catch (final ProcessDefinitionNotFoundException e) {
            LOGGER.debug("Unable to find process with id " + processId);
            throw new APIItemNotFoundException(org.bonitasoft.web.rest.model.bpm.process.ProcessDefinition.TOKEN,
                    APIID.makeAPIID(processId));
        } catch (final Exception e) {
            throw new APIException("Error when getting process deployment information", e);
        }
    }

    public ProcessAPI getProcessApi() {
        return processAPI;
    }

    public ProcessDefinition deploy(final BusinessArchive businessArchive) {
        try {
            return getProcessApi().deploy(businessArchive);
        } catch (final AlreadyExistsException e) {
            final DesignProcessDefinition processDefinition = businessArchive.getProcessDefinition();
            throw new APIForbiddenException(new T_("Process %appName% in version %version% already exists",
                    new Arg("appName", processDefinition.getName()), new Arg(
                            "version", processDefinition.getVersion())),
                    e);
        } catch (final V6FormDeployException e) {
            final DesignProcessDefinition processDefinition = businessArchive.getProcessDefinition();
            throw new APIException(new T_(
                    "Process %appName% in version %version% contains 6.x Legacy artifacts (forms or case overview page). Those are based on Google Web Toolkit (GWT), a technology that is no longer supported by Bonita. To know more, check the documentation.",
                    new Arg("appName", processDefinition.getName()),
                    new Arg("version", processDefinition.getVersion())), e);
        } catch (final Exception e) {
            throw new APIException(new T_("Unable to deploy business archive"), e);
        }
    }

    public void enableProcess(final long processId) {
        try {
            getProcessApi().enableProcess(processId);
        } catch (final Exception e) {
            throw new APIException(new T_("Unable to enable process"), e);
        }
    }

    public void disableProcess(final long processId) {
        try {
            getProcessApi().disableProcess(processId);
        } catch (final Exception e) {
            throw new APIException(new T_("Unable to disable process"), e);
        }
    }

    public ProcessDeploymentInfo updateProcessDeploymentInfo(final long processId,
            final ProcessDeploymentInfoUpdater processDeploymentInfoUpdater) {
        try {
            getProcessApi().updateProcessDeploymentInfo(processId, processDeploymentInfoUpdater);
            return getProcessApi().getProcessDeploymentInfo(processId);
        } catch (final Exception e) {
            throw new APIException(new T_("Error when updating process deployment informations"), e);
        }
    }

    public void deleteDisabledProcesses(final List<Long> processIds) {
        try {
            for (final Long id : processIds) {
                deleteProcessInstancesByBunch(id, DELETE_PROCESS_BUNCH_SIZE, processIds);
                deleteArchivedProcessInstancesByBunch(id, DELETE_PROCESS_BUNCH_SIZE, processIds);
                getProcessApi().deleteProcessDefinition(id);
            }
        } catch (final BonitaException e) {
            if (e.getCause() instanceof ProcessDefinitionNotFoundException) {
                throw new APIItemNotFoundException(org.bonitasoft.web.rest.model.bpm.process.ProcessDefinition.TOKEN);
            } else {
                throw new APIException("Error when deleting some of the processes in the list " + processIds, e);
            }
        }
    }

    /**
     * Delete archived process instances by bunch for a given processId
     */
    public void deleteArchivedProcessInstancesByBunch(final long processId, final int bunchSize,
            final List<Long> processesAllowedToBeDeletedIds)
            throws DeletionException, ProcessDefinitionNotFoundException {
        long numberOfDeletedArchivedProcessInstances = 0;
        do {
            try {
                numberOfDeletedArchivedProcessInstances = getProcessApi().deleteArchivedProcessInstances(processId, 0,
                        bunchSize);
            } catch (final ProcessInstanceHierarchicalDeletionException e) {
                final long parentProcessInstanceID = e.getProcessInstanceId();
                final long parentProcessID = getProcessApi()
                        .getProcessDefinitionIdFromProcessInstanceId(parentProcessInstanceID);
                if (processesAllowedToBeDeletedIds.contains(parentProcessID)) {
                    deleteProcessInstancesByBunch(parentProcessID, DELETE_PROCESS_BUNCH_SIZE,
                            processesAllowedToBeDeletedIds);
                } else {
                    LOGGER.warn(
                            "Process with ID " + processId + " cannot be deleted without also deleting its parent ("
                                    + parentProcessID + ").");
                }
            }
        } while (numberOfDeletedArchivedProcessInstances >= bunchSize);
    }

    /**
     * Delete process instances by bunch for a given processId
     */
    public void deleteProcessInstancesByBunch(final long processId, final int bunchSize,
            final List<Long> processesAllowedToBeDeletedIds)
            throws DeletionException, ProcessDefinitionNotFoundException {
        long numberOfDeletedProcessInstances = 0;
        do {
            try {
                numberOfDeletedProcessInstances = getProcessApi().deleteProcessInstances(processId, 0, bunchSize);
            } catch (final ProcessInstanceHierarchicalDeletionException e) {
                final long parentProcessInstanceID = e.getProcessInstanceId();
                final long parentProcessID = getProcessApi()
                        .getProcessDefinitionIdFromProcessInstanceId(parentProcessInstanceID);
                if (processesAllowedToBeDeletedIds.contains(parentProcessID)) {
                    deleteProcessInstancesByBunch(parentProcessID, DELETE_PROCESS_BUNCH_SIZE,
                            processesAllowedToBeDeletedIds);
                } else {
                    LOGGER.warn(
                            "Process with ID " + processId + " cannot be deleted without also deleting its parent ("
                                    + parentProcessID + ").");
                }
            }
        } while (numberOfDeletedProcessInstances >= bunchSize);
    }

    public SearchResult<ProcessDeploymentInfo> searchProcessDefinitions(final SearchOptions searchOptions) {
        try {
            return getProcessApi().searchProcessDeploymentInfos(searchOptions);
        } catch (final Exception e) {
            throw new APIException("Error when searching process definition", e);
        }
    }

    public SearchResult<ProcessDeploymentInfo> searchProcessDefinitionsSupervisedBy(final long userId,
            final SearchOptions searchOptions) {
        try {
            return getProcessApi().searchProcessDeploymentInfosSupervisedBy(userId, searchOptions);
        } catch (final Exception e) {
            throw new APIException("Error when searching process definition supervised by user " + userId, e);
        }
    }

    public SearchResult<ProcessDeploymentInfo> searchUncategorizedProcessDefinitionsUserCanStart(final long userId,
            final SearchOptions searchOptions) {
        try {
            return getProcessApi().searchUncategorizedProcessDeploymentInfosCanBeStartedBy(userId, searchOptions);
        } catch (final Exception e) {
            throw new APIException(
                    "Error when searching uncategorized process definition which can be started by user " + userId, e);
        }
    }

    public SearchResult<ProcessDeploymentInfo> searchRecentlyStartedProcessDefinitions(final long userId,
            final SearchOptions searchOptions) {
        try {
            return getProcessApi().searchProcessDeploymentInfosStartedBy(userId, searchOptions);
        } catch (final Exception e) {
            throw new APIException("Error when searching recently started process by user " + userId, e);
        }
    }

    public long countResolvedProcesses() {
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 0);
        builder.filter(ProcessDeploymentInfoSearchDescriptor.CONFIGURATION_STATE,
                ProcessItem.VALUE_CONFIGURATION_STATE_RESOLVED);
        try {
            return getProcessApi().searchProcessDeploymentInfos(builder.done()).getCount();
        } catch (final Exception e) {
            throw new APIException("Error when counting resolved processes", e);
        }
    }

    public SearchResult<ProcessDeploymentInfo> searchProcessDeploymentInfosWithAssignedOrPendingHumanTasksSupervisedBy(
            final long supervisorId,
            final SearchOptions searchOptions) {
        try {
            return getProcessApi().searchProcessDeploymentInfosWithAssignedOrPendingHumanTasksSupervisedBy(supervisorId,
                    searchOptions);
        } catch (final SearchException e) {
            throw new APIException("Error when searching process user can start", e);
        }
    }

    public SearchResult<ProcessDeploymentInfo> searchProcessDeploymentInfosWithAssignedOrPendingHumanTasks(
            final SearchOptions searchOptions) {
        try {
            return getProcessApi().searchProcessDeploymentInfosWithAssignedOrPendingHumanTasks(searchOptions);
        } catch (final SearchException e) {
            throw new APIException("Error when searching process user can start", e);
        }
    }

    public SearchResult<ProcessDeploymentInfo> searchProcessDeploymentInfosWithAssignedOrPendingHumanTasksFor(
            final long userId,
            final SearchOptions searchOptions) {
        try {
            return getProcessApi().searchProcessDeploymentInfosWithAssignedOrPendingHumanTasksFor(userId,
                    searchOptions);
        } catch (final SearchException e) {
            throw new APIException("Error when searching process user can start", e);
        }
    }

    public SearchResult<ProcessDeploymentInfo> searchProcessDeploymentInfos(final long userId,
            final SearchOptions searchOptions) {
        try {
            return getProcessApi().searchProcessDeploymentInfosCanBeStartedBy(userId, searchOptions);
        } catch (final SearchException e) {
            throw new APIException("Error when searching process user can start", e);
        }
    }

    public List<DataDefinition> getProcessDataDefinitions(final long processId) {
        try {
            return processAPI.getProcessDataDefinitions(processId, 0, Integer.MAX_VALUE);
        } catch (final ProcessDefinitionNotFoundException e) {
            throw new APINotFoundException(
                    new T_("Unable to get process data definitions, process %processId% not found",
                            new Arg("processId", processId)));
        }
    }

}
