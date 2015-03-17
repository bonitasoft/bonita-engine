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
package org.bonitasoft.engine.external.web.forms;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.impl.SessionInfos;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.command.SCommandExecutionException;
import org.bonitasoft.engine.command.SCommandParameterizationException;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.OperationService;
import org.bonitasoft.engine.core.operation.exception.SOperationExecutionException;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.process.comment.api.SCommentService;
import org.bonitasoft.engine.core.process.definition.model.builder.ServerModelConvertor;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeExecutionException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeReadException;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.execution.ProcessExecutor;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.log.LogMessageBuilder;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.operation.Operation;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceSingleton;

/**
 * @author Ruiheng Fan
 * @author Baptiste Mesta
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class ExecuteActionsAndTerminateTask extends ExecuteActionsBaseEntry {

    public static final String ACTIVITY_INSTANCE_ID_KEY = "ACTIVITY_INSTANCE_ID_KEY";

    public static final String USER_ID_KEY = "USER_ID_KEY";

    @Override
    public Serializable execute(final Map<String, Serializable> parameters, final TenantServiceAccessor tenantAccessor)
            throws SCommandParameterizationException, SCommandExecutionException {
        final List<Operation> operations = getOperations(parameters);
        final Map<String, Serializable> operationsContext = getOperationsContext(parameters);
        final long sActivityInstanceID = getActivityInstanceId(parameters);

        try {
            final ClassLoaderService classLoaderService = tenantAccessor.getClassLoaderService();
            final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
            final SFlowNodeInstance flowNodeInstance = activityInstanceService.getFlowNodeInstance(sActivityInstanceID);
            final long processDefinitionID = flowNodeInstance.getProcessDefinitionId();
            final ClassLoader processClassloader = classLoaderService.getLocalClassLoader(ScopeType.PROCESS.name(), processDefinitionID);
            final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(processClassloader);
                updateActivityInstanceVariables(operations, operationsContext, sActivityInstanceID, processDefinitionID);
            } finally {
                Thread.currentThread().setContextClassLoader(contextClassLoader);
            }
            long executedByUserId = getExecuteByUserId(parameters);
            executeActivity(flowNodeInstance, executedByUserId);
        } catch (final SBonitaException e) {
            log(tenantAccessor, e);
            throw new SCommandExecutionException(
                    "Error executing command 'Map<String, Serializable> ExecuteActionsAndTerminateTask(List<Operation>, Map<String, Serializable>, long activityInstanceId)'",
                    e);
        }
        return null;
    }

    protected Long getExecuteByUserId(final Map<String, Serializable> parameters) {
        Serializable executeByUserId = parameters.get(USER_ID_KEY);
        // executeByUserId is not defined when the use is doing the task by himself
        if (executeByUserId == null) {
            return SessionInfos.getSessionInfos().getUserId();
        }
        return (Long) executeByUserId;
    }

    protected Long getActivityInstanceId(final Map<String, Serializable> parameters) throws SCommandParameterizationException {
        final String message = "Mandatory parameter " + ACTIVITY_INSTANCE_ID_KEY + " is missing or not convertible to long.";
        return getMandatoryParameter(parameters, ACTIVITY_INSTANCE_ID_KEY, message);
    }

    protected List<Operation> getOperations(final Map<String, Serializable> parameters) throws SCommandParameterizationException {
        final String message = "Mandatory parameter " + OPERATIONS_LIST_KEY + " is missing or not convertible to List.";
        final List<Operation> operations = getParameter(parameters, OPERATIONS_LIST_KEY, message);
        if (operations == null) {
            return Collections.emptyList();
        }
        return operations;
    }

    protected Map<String, Serializable> getOperationsContext(final Map<String, Serializable> parameters) throws SCommandParameterizationException {
        final String message = "Mandatory parameter " + OPERATIONS_INPUT_KEY + " is missing or not convertible to Map.";
        final Map<String, Serializable> operations = getParameter(parameters, OPERATIONS_INPUT_KEY, message);
        if (operations == null) {
            return Collections.emptyMap();
        }
        return operations;
    }

    protected void updateActivityInstanceVariables(final List<Operation> operations, final Map<String, Serializable> operationsContext,
            final long activityInstanceId, final Long processDefinitionID) throws SOperationExecutionException {
        SExpressionContext sExpressionContext = buildExpressionContext(operationsContext, activityInstanceId, processDefinitionID);
        List<SOperation> sOperations = ServerModelConvertor.convertOperations(operations);
        getOperationService().execute(sOperations, activityInstanceId, DataInstanceContainer.ACTIVITY_INSTANCE.name(), sExpressionContext);
    }

    private SExpressionContext buildExpressionContext(final Map<String, Serializable> operationsContext, final long activityInstanceId,
            final Long processDefinitionID) {
        final SExpressionContext sExpressionContext = new SExpressionContext();
        sExpressionContext.setSerializableInputValues(operationsContext);
        sExpressionContext.setContainerId(activityInstanceId);
        sExpressionContext.setContainerType(DataInstanceContainer.ACTIVITY_INSTANCE.name());
        sExpressionContext.setProcessDefinitionId(processDefinitionID);
        return sExpressionContext;
    }

    private OperationService getOperationService() {
        final TenantServiceAccessor tenantAccessor = TenantServiceSingleton.getInstance(getTenantId());
        return tenantAccessor.getOperationService();
    }

    protected void executeActivity(final SFlowNodeInstance flowNodeInstance, long executerUserId) throws SFlowNodeReadException, SFlowNodeExecutionException {
        final TenantServiceAccessor tenantAccessor = TenantServiceSingleton.getInstance(getTenantId());
        final ProcessExecutor processExecutor = tenantAccessor.getProcessExecutor();
        final TechnicalLoggerService logger = tenantAccessor.getTechnicalLoggerService();
        final SessionInfos sessionInfos = SessionInfos.getSessionInfos();

        final long executerSubstituteId = sessionInfos.getUserId();
        // no need to handle failed state, all is in the same tx, if the node fail we just have an exception on client side + rollback
        processExecutor.executeFlowNode(flowNodeInstance.getId(), null, null, flowNodeInstance.getProcessDefinitionId(), executerUserId, executerSubstituteId);
        if (logger.isLoggable(getClass(), TechnicalLogSeverity.INFO) && flowNodeInstance.getStateId() != 0 /* don't log when create subtask */) {
            final String message = LogMessageBuilder.buildExecuteTaskContextMessage(flowNodeInstance, sessionInfos.getUsername(), executerUserId,
                    executerSubstituteId, null); // no inputs taken in this LEGACY command for old-version web form execution
            logger.log(getClass(), TechnicalLogSeverity.INFO, message);
        }

        addSystemCommentOnProcessInstanceWhenExecutingTaskFor(flowNodeInstance, executerUserId, executerSubstituteId);
    }

    protected void addSystemCommentOnProcessInstanceWhenExecutingTaskFor(final SFlowNodeInstance flowNodeInstance, final long executerUserId,
            final long executerSubstituteUserId) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TechnicalLoggerService logger = tenantAccessor.getTechnicalLoggerService();
        final SCommentService commentService = tenantAccessor.getCommentService();
        final SessionInfos session = SessionInfos.getSessionInfos();

        if (executerUserId != executerSubstituteUserId) {
            final IdentityService identityService = tenantAccessor.getIdentityService();
            try {
                final SUser executerUser = identityService.getUser(executerUserId);
                final StringBuilder stb = new StringBuilder();
                stb.append("The user " + session.getUsername() + " ");
                stb.append("acting as delegate of the user " + executerUser.getUserName() + " ");
                stb.append("has done the task \"" + flowNodeInstance.getDisplayName() + "\".");
                commentService.addSystemComment(flowNodeInstance.getParentProcessInstanceId(), stb.toString());
            } catch (final SBonitaException e) {
                logger.log(this.getClass(), TechnicalLogSeverity.ERROR, "Error when adding a comment on the process instance.", e);
            }
        }
    }
}
