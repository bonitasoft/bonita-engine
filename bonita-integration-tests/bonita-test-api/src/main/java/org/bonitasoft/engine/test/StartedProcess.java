/*
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
 */

package org.bonitasoft.engine.test;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.data.ArchivedDataInstance;
import org.bonitasoft.engine.bpm.data.ArchivedDataNotFoundException;
import org.bonitasoft.engine.bpm.data.DataNotFoundException;
import org.bonitasoft.engine.bpm.process.ProcessActivationException;
import org.bonitasoft.engine.bpm.process.ProcessDefinitionNotFoundException;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.command.CommandExecutionException;
import org.bonitasoft.engine.command.CommandNotFoundException;
import org.bonitasoft.engine.command.CommandParameterizationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.UpdateException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * @author mazourd
 */
public class StartedProcess {

    private ProcessInstance processInstance;

    private UserTaskAPI userTaskAPI;

    private ProcessAPI processAPI;

    private long temporaryUserTaskId;

    public StartedProcess() {
    }

    public StartedProcess(ProcessAPI processAPI, ProcessInstance processInstance, UserTaskAPI userTaskAPI) {
        this.processAPI = processAPI;
        this.processInstance = processInstance;
        this.userTaskAPI = userTaskAPI;
    }

    public  ReachedTask waitForTaskToFail() throws Exception {
        return  new ReachedTask(userTaskAPI.waitForTaskToFail(processInstance).getId(),processAPI);
    }
    public ReachedTask waitForUserTask(String taskName) throws CommandExecutionException, TimeoutException, CommandParameterizationException,
            CommandNotFoundException {
        this.temporaryUserTaskId = userTaskAPI.waitForUserTask(processInstance.getId(), taskName, -1);
        return new ReachedTask(this.temporaryUserTaskId, processAPI);
    }

    public ReachedDataInstance getArchivedData(String dataName) throws ArchivedDataNotFoundException {
        return new ReachedDataInstance(getProcessAPI().getArchivedActivityDataInstance(dataName, temporaryUserTaskId));
    }

    public ReachedDataInstanceList getArchivedDatas(int startIndex, int maxResults){
        ArrayList<ReachedDataInstance> List = new ArrayList<>();
        List<ArchivedDataInstance> archivedDataInstances = getProcessAPI().getArchivedActivityDataInstances(temporaryUserTaskId,startIndex, maxResults);
        for (ArchivedDataInstance archivedDataInstance : archivedDataInstances){
            List.add(new ReachedDataInstance(archivedDataInstance));
        }
        return new ReachedDataInstanceList(List);
    }
    public  ReachedDataInstance updateActivityInstanceVariables(Map<String, Serializable> variables) throws UpdateException {
        processAPI.updateActivityInstanceVariables(temporaryUserTaskId,variables);
        return new ReachedDataInstance(getProcessAPI().getActivityDataInstances(temporaryUserTaskId, 0, 10).get(0));
    }
    public ReachedTask updateActivityDataInstance(String dataName, Serializable dataValue) throws UpdateException {
        processAPI.updateActivityDataInstance(dataName, temporaryUserTaskId, dataValue);
        return new ReachedTask(temporaryUserTaskId,processAPI);
    }

    public ReachedDataInstance accessDataInTask(String dataInstanceName) throws DataNotFoundException {
        return new ReachedDataInstance(getProcessAPI().getProcessDataInstance(dataInstanceName, processInstance.getId()));
    }
    public void disableAndDeleteProcess() throws ProcessActivationException, ProcessDefinitionNotFoundException, DeletionException {
        getProcessAPI().disableProcess(processInstance.getId());

        // Delete all process instances
        long nbDeletedProcessInstances;
        do {
            nbDeletedProcessInstances = getProcessAPI().deleteProcessInstances(processInstance.getId(), 0, 100);
        } while (nbDeletedProcessInstances > 0);

        // Delete all archived process instances
        long nbDeletedArchivedProcessInstances;
        do {
            nbDeletedArchivedProcessInstances = getProcessAPI().deleteArchivedProcessInstances(processInstance.getId(), 0, 100);
        } while (nbDeletedArchivedProcessInstances > 0);

        getProcessAPI().deleteProcessDefinition(processInstance.getId());
    }

    public ProcessInstance getProcessInstance() {
        return processInstance;
    }

    public void setProcessInstance(ProcessInstance processInstance) {
        this.processInstance = processInstance;
    }

    public ProcessAPI getProcessAPI() {
        return processAPI;
    }

    public void setProcessAPI(ProcessAPI processAPI) {
        this.processAPI = processAPI;
    }

    public UserTaskAPI getUserTaskAPI() {
        return userTaskAPI;
    }

    public void setUserTaskAPI(UserTaskAPI userTaskAPI) {
        this.userTaskAPI = userTaskAPI;
    }

    public long getTemporaryUserTaskId() {
        return temporaryUserTaskId;
    }
}
