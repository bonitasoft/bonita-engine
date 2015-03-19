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
package org.bonitasoft.engine.api.impl;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.bpm.connector.ConnectorDefinitionWithInputValues;
import org.bonitasoft.engine.bpm.process.ProcessActivationException;
import org.bonitasoft.engine.bpm.process.ProcessDefinitionNotFoundException;
import org.bonitasoft.engine.bpm.process.ProcessExecutionException;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.process.comment.api.SCommentService;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionException;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionNotFoundException;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionReadException;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceCreationException;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.execution.Filter;
import org.bonitasoft.engine.execution.FlowNodeNameFilter;
import org.bonitasoft.engine.execution.FlowNodeSelector;
import org.bonitasoft.engine.execution.ProcessExecutor;
import org.bonitasoft.engine.execution.StartFlowNodeFilter;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.operation.Operation;
import org.bonitasoft.engine.service.ModelConvertor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceSingleton;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;

/**
 * @author Elias Ricken de Medeiros
 * @author Vincent Elcrin
 * @author Matthieu Chaffotte
 */
public class ProcessStarter {

    private final long userId;

    private final long processDefinitionId;

    private final List<Operation> operations;

    private final Map<String, Serializable> context;

    private final Filter<SFlowNodeDefinition> filter;

    private final Map<String, Serializable> instantiationInputs;

    private ProcessStarter(final long userId, final long processDefinitionId, final List<Operation> operations,
            final Map<String, Serializable> context, final Filter<SFlowNodeDefinition> filter, final Map<String, Serializable> instantiationInputs) {
        this.userId = userId;
        this.processDefinitionId = processDefinitionId;
        this.operations = operations;
        this.context = context;
        this.filter = filter;
        this.instantiationInputs = instantiationInputs;
    }

    public ProcessStarter(final long userId, final long processDefinitionId, final List<Operation> operations, final Map<String, Serializable> context) {
        this(userId, processDefinitionId, operations, context, new StartFlowNodeFilter(), null);
    }

    public ProcessStarter(final long userId, final long processDefinitionId, final List<Operation> operations, final Map<String, Serializable> context,
            final List<String> activityNames) {
        this(userId, processDefinitionId, operations, context, new FlowNodeNameFilter(activityNames), null);
    }

    public ProcessStarter(final long userId, final long processDefinitionId, final Map<String, Serializable> instantiationInputs) {
        this(userId, processDefinitionId, null, null, new StartFlowNodeFilter(), instantiationInputs);
    }

    public ProcessInstance start() throws ProcessDefinitionNotFoundException, ProcessActivationException, ProcessExecutionException {
        try {
            return start(null);
        } catch (final SProcessDefinitionNotFoundException e) {
            throw new ProcessDefinitionNotFoundException(e);
        } catch (final SProcessDefinitionReadException e) {
            throw new RetrieveException(e);
        } catch (final SProcessDefinitionException e) {
            throw new ProcessActivationException(e);
        } catch (final SBonitaException e) {
            throw new ProcessExecutionException(e);
        }
    }

    // For commands
    public ProcessInstance start(final List<ConnectorDefinitionWithInputValues> connectorsWithInput) throws SProcessInstanceCreationException,
            SProcessDefinitionReadException, SProcessDefinitionException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessExecutor processExecutor = tenantAccessor.getProcessExecutor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();

        final SProcessDefinition sProcessDefinition = processDefinitionService.getProcessDefinitionIfIsEnabled(processDefinitionId);
        final Map<String, Object> operationContext = getContext();
        final long starterSubstituteUserId = SessionInfos.getUserIdFromSession();
        final long starterUserId = getStarterUserId(starterSubstituteUserId);

        final SProcessInstance startedSProcessInstance;
        try {
            final List<SOperation> sOperations = ModelConvertor.convertOperations(operations);
            startedSProcessInstance = processExecutor.start(starterUserId, starterSubstituteUserId, sOperations, operationContext, connectorsWithInput,
                    new FlowNodeSelector(sProcessDefinition, filter), instantiationInputs);
        } catch (final SProcessInstanceCreationException e) {
            log(tenantAccessor, e);
            e.setProcessDefinitionIdOnContext(sProcessDefinition.getId());
            e.setProcessDefinitionNameOnContext(sProcessDefinition.getName());
            e.setProcessDefinitionVersionOnContext(sProcessDefinition.getVersion());
            throw e;
        }

        logProcessInstanceStartedAndAddComment(sProcessDefinition, starterUserId, starterSubstituteUserId, startedSProcessInstance);
        return ModelConvertor.toProcessInstance(sProcessDefinition, startedSProcessInstance);
    }

    protected void log(final TenantServiceAccessor tenantAccessor, final Exception e) {
        final TechnicalLoggerService logger = tenantAccessor.getTechnicalLoggerService();
        logger.log(this.getClass(), TechnicalLogSeverity.DEBUG, e);
    }

    protected long getStarterUserId(final long starterSubstituteUserId) {
        if (userId == 0) {
            return starterSubstituteUserId;
        }
        return userId;
    }

    protected Map<String, Object> getContext() {
        if (context != null) {
            return new HashMap<String, Object>(context);
        }
        return Collections.emptyMap();
    }

    private void logProcessInstanceStartedAndAddComment(final SProcessDefinition sProcessDefinition, final long starterId, final long starterSubstituteId,
            final SProcessInstance sProcessInstance) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TechnicalLoggerService logger = tenantAccessor.getTechnicalLoggerService();

        final StringBuilder stb = new StringBuilder();
        stb.append("The user <");
        stb.append(SessionInfos.getUserNameFromSession());
        if (starterId != starterSubstituteId) {
            stb.append("> acting as delegate of user with id <");
            stb.append(starterId);
        }
        stb.append("> has started the process instance <");
        stb.append(sProcessInstance.getId());
        stb.append("> of process <");
        stb.append(sProcessDefinition.getName());
        stb.append("> in version <");
        stb.append(sProcessDefinition.getVersion());
        stb.append("> and id <");
        stb.append(sProcessDefinition.getId());
        stb.append(">");

        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.INFO)) {
            logger.log(this.getClass(), TechnicalLogSeverity.INFO, stb.toString());
        }

        addSystemCommentOnProcessInstanceWhenStartingProcessFor(sProcessInstance, starterId, starterSubstituteId);
    }

    protected void addSystemCommentOnProcessInstanceWhenStartingProcessFor(final SProcessInstance sProcessInstance, final long starterId,
            final long starterSubstituteId) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TechnicalLoggerService logger = tenantAccessor.getTechnicalLoggerService();
        final SCommentService commentService = tenantAccessor.getCommentService();

        if (starterId != starterSubstituteId) {
            final IdentityService identityService = tenantAccessor.getIdentityService();
            try {
                final SUser starter = identityService.getUser(starterId);
                commentService.addSystemComment(sProcessInstance.getId(), "The user " + SessionInfos.getUserNameFromSession()
                        + " acting as delegate of the user " + starter.getUserName() + " has started the case.");
            } catch (final SBonitaException e) {
                logger.log(this.getClass(), TechnicalLogSeverity.ERROR, "Error when adding a comment on the process instance.", e);
            }
        }
    }

    protected TenantServiceAccessor getTenantAccessor() {
        try {
            final SessionAccessor sessionAccessor = ServiceAccessorFactory.getInstance().createSessionAccessor();
            final long tenantId = sessionAccessor.getTenantId();
            return TenantServiceSingleton.getInstance(tenantId);
        } catch (final Exception e) {
            throw new BonitaRuntimeException(e);
        }
    }

}
