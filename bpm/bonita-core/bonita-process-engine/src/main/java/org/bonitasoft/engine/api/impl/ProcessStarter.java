/**
 * Copyright (C) 2013 BonitaSoft S.A.
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

import org.bonitasoft.engine.bpm.process.ActivationState;
import org.bonitasoft.engine.bpm.process.ProcessActivationException;
import org.bonitasoft.engine.bpm.process.ProcessDefinitionNotFoundException;
import org.bonitasoft.engine.bpm.process.ProcessExecutionException;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionNotFoundException;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinitionDeployInfo;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.execution.FlowNodeNameFilter;
import org.bonitasoft.engine.execution.FlowNodeSelector;
import org.bonitasoft.engine.execution.Filter;
import org.bonitasoft.engine.execution.ProcessExecutor;
import org.bonitasoft.engine.execution.StartFlowNodeFilter;
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
 */
public class ProcessStarter {

    private final long userId;

    private final long processDefinitionId;

    private final List<Operation> operations;

    private final Map<String, Serializable> context;

    private final Filter<SFlowNodeDefinition> filter;

    private ProcessStarter(final long userId, final long processDefinitionId, final List<Operation> operations,
           final Map<String, Serializable> context, final Filter<SFlowNodeDefinition> filter) {
        this.userId = userId;
        this.processDefinitionId = processDefinitionId;
        this.operations = operations;
        this.context = context;
        this.filter = filter;
    }

    public ProcessStarter(final long userId, final long processDefinitionId, final List<Operation> operations, final Map<String, Serializable> context) {
        this(userId, processDefinitionId, operations, context, new StartFlowNodeFilter());
    }

    public ProcessStarter(final long userId, final long processDefinitionId, final List<Operation> operations, final Map<String, Serializable> context,
            final List<String> activityNames) {
        this(userId, processDefinitionId, operations, context, new FlowNodeNameFilter(activityNames));
    }

    public ProcessInstance start() throws ProcessDefinitionNotFoundException, ProcessActivationException, ProcessExecutionException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();

        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final SProcessDefinition sProcessDefinition = retrieveProcessDefinition(processDefinitionService);
        final ProcessExecutor processExecutor = tenantAccessor.getProcessExecutor();
        final long starterId;
        final long userIdFromSession = SessionInfos.getUserIdFromSession();
        if (userId == 0) {
            starterId = userIdFromSession;
        } else {
            starterId = userId;
        }
        final SProcessInstance startedInstance;
        try {
            final List<SOperation> sOperations = ModelConvertor.toSOperation(operations);
            Map<String, Object> operationContext;
            if (context != null) {
                operationContext = new HashMap<String, Object>(context);
            } else {
                operationContext = Collections.emptyMap();
            }
            startedInstance = processExecutor.start(starterId, userIdFromSession, sOperations, operationContext, null, new FlowNodeSelector(sProcessDefinition,
                    filter));
        } catch (final SBonitaException e) {
            throw new ProcessExecutionException(e);
        }

        final ProcessInstance processInstance = ModelConvertor.toProcessInstance(sProcessDefinition, startedInstance);
        final TechnicalLoggerService logger = tenantAccessor.getTechnicalLoggerService();
        logInstanceStarted(sProcessDefinition, starterId, userIdFromSession, processInstance, logger);
        return processInstance;
    }

    private SProcessDefinition retrieveProcessDefinition(final ProcessDefinitionService processDefinitionService) throws ProcessActivationException,
            ProcessDefinitionNotFoundException {
        final SProcessDefinition sProcessDefinition;
        try {
            final SProcessDefinitionDeployInfo deployInfo = processDefinitionService.getProcessDeploymentInfo(processDefinitionId);
            if (ActivationState.DISABLED.name().equals(deployInfo.getActivationState())) {
                throw new ProcessActivationException("Process disabled");
            }
            sProcessDefinition = processDefinitionService.getProcessDefinition(processDefinitionId);
        } catch (final SProcessDefinitionNotFoundException e) {
            throw new ProcessDefinitionNotFoundException(e);
        } catch (final SBonitaException e) {
            throw new RetrieveException(e);
        }
        return sProcessDefinition;
    }

    private void logInstanceStarted(final SProcessDefinition sProcessDefinition, final long starterId, final long userIdFromSession, final ProcessInstance processInstance,
            final TechnicalLoggerService logger) {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.INFO)) {
            final StringBuilder stb = new StringBuilder();
            stb.append("The user <");
            stb.append(SessionInfos.getUserNameFromSession());
            if (starterId != userIdFromSession) {
                stb.append("> acting as delegate of user with id <");
                stb.append(starterId);
            }
            stb.append("> has started instance <");
            stb.append(processInstance.getId());
            stb.append("> of process <");
            stb.append(sProcessDefinition.getName());
            stb.append("> in version <");
            stb.append(sProcessDefinition.getVersion());
            stb.append("> and id <");
            stb.append(sProcessDefinition.getId());
            stb.append(">");
            logger.log(this.getClass(), TechnicalLogSeverity.INFO, stb.toString());
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
