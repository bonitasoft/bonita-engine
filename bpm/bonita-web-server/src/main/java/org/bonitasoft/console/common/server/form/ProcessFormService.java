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
package org.bonitasoft.console.common.server.form;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceNotFoundException;
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedHumanTaskInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedHumanTaskInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstance;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstanceNotFoundException;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstancesSearchDescriptor;
import org.bonitasoft.engine.bpm.process.ProcessDefinitionNotFoundException;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceNotFoundException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.session.APISession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessFormService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessFormService.class.getName());

    public String getProcessPath(final APISession apiSession, final long processDefinitionId)
            throws BonitaException, UnsupportedEncodingException {
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI(apiSession)
                .getProcessDeploymentInfo(processDefinitionId);
        return encodePathSegment(processDeploymentInfo.getName())
                + "/"
                + encodePathSegment(processDeploymentInfo.getVersion());
    }

    public String encodePathSegment(final String stringToEncode) throws UnsupportedEncodingException {
        //URLEncoder#encode encodes spaces to '+' but we want '%20' instead in the path part of the URL
        // '/' gets encoded to %2F whereas is should be left as it is since it in perfectly safe in the path part of the URL
        // '+' gets encoded to %2B whereas is should be left as it is since it in perfectly safe in the path part of the URL
        return URLEncoder.encode(stringToEncode, StandardCharsets.UTF_8).replaceAll("\\+", "%20").replaceAll("%2F", "/")
                .replaceAll("%2B", "+");
    }

    public long getProcessDefinitionId(final APISession apiSession, final String processName,
            final String processVersion)
            throws BonitaException {
        if (processName != null && processVersion != null) {
            try {
                return getProcessAPI(apiSession).getProcessDefinitionId(processName, processVersion);
            } catch (final ProcessDefinitionNotFoundException e) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Wrong parameters for process name and version", e);
                }
            }
        }
        return -1L;
    }

    public long getTaskInstanceId(final APISession apiSession, final long processInstanceId, final String taskName,
            final long userId)
            throws BonitaException {
        final ProcessAPI processAPI = getProcessAPI(apiSession);
        long ensuredUserId;
        if (userId != -1L) {
            ensuredUserId = userId;
        } else {
            ensuredUserId = apiSession.getUserId();
        }
        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 1);
        searchOptionsBuilder.filter(HumanTaskInstanceSearchDescriptor.PARENT_CONTAINER_ID, processInstanceId);
        searchOptionsBuilder.filter(HumanTaskInstanceSearchDescriptor.NAME, taskName);
        final SearchResult<HumanTaskInstance> searchMyAvailableHumanTasks = processAPI.searchMyAvailableHumanTasks(
                ensuredUserId,
                searchOptionsBuilder.done());
        if (searchMyAvailableHumanTasks.getCount() > 0) {
            return searchMyAvailableHumanTasks.getResult().get(0).getId();
        } else {
            final SearchOptionsBuilder archivedSearchOptionsBuilder = new SearchOptionsBuilder(0, 1);
            archivedSearchOptionsBuilder.filter(ArchivedHumanTaskInstanceSearchDescriptor.ASSIGNEE_ID, ensuredUserId);
            archivedSearchOptionsBuilder.filter(ArchivedHumanTaskInstanceSearchDescriptor.PARENT_PROCESS_INSTANCE_ID,
                    processInstanceId);
            archivedSearchOptionsBuilder.filter(ArchivedHumanTaskInstanceSearchDescriptor.NAME, taskName);
            final SearchResult<ArchivedHumanTaskInstance> searchArchivedHumanTasks = processAPI
                    .searchArchivedHumanTasks(archivedSearchOptionsBuilder
                            .done());
            if (searchArchivedHumanTasks.getCount() > 0) {
                return searchArchivedHumanTasks.getResult().get(0).getSourceObjectId();
            } else {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Could not find task available with name " + taskName + " for process instance "
                            + processInstanceId);
                }
                throw new ActivityInstanceNotFoundException(-1L);
            }
        }
    }

    public String getTaskName(final APISession apiSession, final long taskInstanceId) throws BonitaException {
        if (taskInstanceId != -1L) {
            final ProcessAPI processAPI = getProcessAPI(apiSession);
            try {
                final ActivityInstance activity = processAPI.getActivityInstance(taskInstanceId);
                return activity.getName();
            } catch (final ActivityInstanceNotFoundException e) {
                final ArchivedActivityInstance activity = processAPI.getArchivedActivityInstance(taskInstanceId);
                return activity.getName();
            }
        }
        return null;
    }

    public long ensureProcessDefinitionId(final APISession apiSession, final long processDefinitionId,
            final long processInstanceId, final long taskInstanceId)
            throws BonitaException {
        if (processDefinitionId != -1L) {
            return processDefinitionId;
        } else if (processInstanceId != -1L) {
            return getProcessDefinitionIdFromProcessInstanceId(apiSession, processInstanceId);
        } else {
            return getProcessDefinitionIdFromTaskId(apiSession, taskInstanceId);
        }
    }

    protected long getProcessDefinitionIdFromTaskId(final APISession apiSession, final long taskInstanceId)
            throws BonitaHomeNotSetException,
            ServerAPIException, UnknownAPITypeException, ActivityInstanceNotFoundException {
        final ProcessAPI processAPI = getProcessAPI(apiSession);
        try {
            final ActivityInstance activity = processAPI.getActivityInstance(taskInstanceId);
            return activity.getProcessDefinitionId();
        } catch (final ActivityInstanceNotFoundException e) {
            final ArchivedActivityInstance activity = processAPI.getArchivedActivityInstance(taskInstanceId);
            return activity.getProcessDefinitionId();
        }
    }

    protected long getProcessDefinitionIdFromProcessInstanceId(final APISession apiSession,
            final long processInstanceId) throws BonitaHomeNotSetException,
            ServerAPIException, UnknownAPITypeException, ArchivedProcessInstanceNotFoundException {
        final ProcessAPI processAPI = getProcessAPI(apiSession);
        try {
            final ProcessInstance processInstance = processAPI.getProcessInstance(processInstanceId);
            return processInstance.getProcessDefinitionId();
        } catch (final ProcessInstanceNotFoundException e) {
            final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 1);
            searchOptionsBuilder.filter(ArchivedProcessInstancesSearchDescriptor.SOURCE_OBJECT_ID, processInstanceId);
            searchOptionsBuilder.sort(ArchivedProcessInstancesSearchDescriptor.ARCHIVE_DATE, Order.ASC);
            SearchResult<ArchivedProcessInstance> searchArchivedProcessInstances = null;
            try {
                searchArchivedProcessInstances = processAPI
                        .searchArchivedProcessInstancesInAllStates(searchOptionsBuilder.done());
            } catch (final SearchException se) {
                throw new ArchivedProcessInstanceNotFoundException(se);
            }
            if (searchArchivedProcessInstances != null && searchArchivedProcessInstances.getCount() > 0) {
                return searchArchivedProcessInstances.getResult().get(0).getProcessDefinitionId();
            } else {
                throw new ArchivedProcessInstanceNotFoundException(processInstanceId);
            }
        }
    }

    protected ProcessAPI getProcessAPI(final APISession apiSession)
            throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        return TenantAPIAccessor.getProcessAPI(apiSession);
    }

}
