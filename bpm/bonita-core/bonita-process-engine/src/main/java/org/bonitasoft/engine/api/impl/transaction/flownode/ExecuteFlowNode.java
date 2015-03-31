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
package org.bonitasoft.engine.api.impl.transaction.flownode;

import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.engine.api.impl.SessionInfos;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.core.contract.data.ContractDataService;
import org.bonitasoft.engine.core.process.comment.api.SCommentService;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SUserTaskInstance;
import org.bonitasoft.engine.execution.ProcessExecutor;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.log.LogMessageBuilder;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.session.model.SSession;

/**
 * @author Matthieu Chaffotte
 */
public class ExecuteFlowNode implements TransactionContent {

    private final ProcessExecutor processExecutor;

    private final TechnicalLoggerService logger;

    private final SCommentService commentService;

    private final IdentityService identityService;

    private final ContractDataService contractDataService;

    private final SFlowNodeInstance flowNodeInstance;

    private final long userId;

    private final Map<String, Serializable> inputs;

    public ExecuteFlowNode(final TenantServiceAccessor tenantAccessor, final long userId, final SFlowNodeInstance flowNodeInstance,
            final Map<String, Serializable> inputs) {
        processExecutor = tenantAccessor.getProcessExecutor();
        logger = tenantAccessor.getTechnicalLoggerService();
        commentService = tenantAccessor.getCommentService();
        identityService = tenantAccessor.getIdentityService();
        contractDataService = tenantAccessor.getContractDataService();
        this.flowNodeInstance = flowNodeInstance;
        this.userId = userId;
        this.inputs = inputs;
    }

    @Override
    public void execute() throws SBonitaException {
        final SSession session = SessionInfos.getSession();
        if (session != null) {
            final long executerSubstituteUserId = session.getUserId();
            final long executerUserId;
            if (userId == 0) {
                executerUserId = executerSubstituteUserId;
            } else {
                executerUserId = userId;
            }
            final boolean isFirstState = flowNodeInstance.getStateId() == 0;

            if (flowNodeInstance instanceof SUserTaskInstance) {
                contractDataService.addUserTaskData(flowNodeInstance.getId(), inputs);
            }
            // no need to handle failed state, all is in the same tx, if the node fail we just have an exception on client side + rollback
            processExecutor.executeFlowNode(flowNodeInstance.getId(), null, null, flowNodeInstance.getParentProcessInstanceId(), executerUserId,
                    executerSubstituteUserId);
            if (logger.isLoggable(getClass(), TechnicalLogSeverity.INFO) && !isFirstState /* don't log when create subtask */) {
                final String message = LogMessageBuilder.buildExecuteTaskContextMessage(flowNodeInstance, session.getUserName(), executerUserId,
                        executerSubstituteUserId, inputs);
                logger.log(getClass(), TechnicalLogSeverity.INFO, message);
            }
            addSystemCommentOnProcessInstanceWhenExecutingTaskFor(flowNodeInstance, executerUserId, executerSubstituteUserId);
        }
    }

    protected void addSystemCommentOnProcessInstanceWhenExecutingTaskFor(final SFlowNodeInstance flowNodeInstance, final long executerUserId,
            final long executerSubstituteUserId) {
        final SSession session = SessionInfos.getSession();
        if (executerUserId != executerSubstituteUserId) {
            try {
                final SUser executerUser = identityService.getUser(executerUserId);
                final StringBuilder stb = new StringBuilder();
                stb.append("The user " + session.getUserName() + " ");
                stb.append("acting as delegate of the user " + executerUser.getUserName() + " ");
                stb.append("has done the task \"" + flowNodeInstance.getDisplayName() + "\".");
                commentService.addSystemComment(flowNodeInstance.getParentProcessInstanceId(), stb.toString());
            } catch (final SBonitaException e) {
                logger.log(this.getClass(), TechnicalLogSeverity.ERROR, "Error when adding a comment on the process instance.", e);
            }
        }
    }

}
