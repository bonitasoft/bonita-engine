/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.external.web.forms;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.impl.SessionInfos;
import org.bonitasoft.engine.bpm.connector.ConnectorDefinitionWithInputValues;
import org.bonitasoft.engine.bpm.process.ActivationState;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.command.SCommandExecutionException;
import org.bonitasoft.engine.command.SCommandParameterizationException;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionException;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionReadException;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinitionDeployInfo;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceCreationException;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.execution.FlowNodeSelector;
import org.bonitasoft.engine.execution.ProcessExecutor;
import org.bonitasoft.engine.execution.StartFlowNodeFilter;
import org.bonitasoft.engine.expression.exception.SInvalidExpressionException;
import org.bonitasoft.engine.external.web.forms.ExecuteActionsBaseEntry;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.operation.Operation;
import org.bonitasoft.engine.service.ModelConvertor;
import org.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * @author Ruiheng Fan
 * @author Celine Souchet
 * @author Matthieu Chaffotte
 * @author Elias Ricken de Medeiros
 */
public class ExecuteActionsAndStartInstanceExt extends ExecuteActionsBaseEntry {

    @Override
    public Serializable execute(final Map<String, Serializable> parameters, final TenantServiceAccessor serviceAccessor)
            throws SCommandParameterizationException, SCommandExecutionException {
        final List<Operation> operations = getParameter(parameters, OPERATIONS_LIST_KEY, "Mandatory parameter " + OPERATIONS_LIST_KEY
                + " is missing or not convertible to List.");
        final Map<String, Object> operationsInputValues = getParameter(parameters, OPERATIONS_INPUT_KEY, "Mandatory parameter " + OPERATIONS_INPUT_KEY
                + " is missing or not convertible to Map.");
        final List<ConnectorDefinitionWithInputValues> connectorsWithInput = getParameter(parameters, CONNECTORS_LIST_KEY, "Mandatory parameter "
                + CONNECTORS_LIST_KEY + " is missing or not convertible to List.");
        final Long sProcessDefinitionID = getParameter(parameters, PROCESS_DEFINITION_ID_KEY, "Mandatory parameter " + PROCESS_DEFINITION_ID_KEY
                + " is missing or not convertible to long.");

        final Long userId = getParameter(parameters, USER_ID_KEY, "Mandatory parameter " + USER_ID_KEY + " is missing or not convertible to String.");

        try {
            final TechnicalLoggerService logger = serviceAccessor.getTechnicalLoggerService();
            final ClassLoaderService classLoaderService = serviceAccessor.getClassLoaderService();
            final ClassLoader processClassloader = classLoaderService.getLocalClassLoader(ScopeType.PROCESS.name(), sProcessDefinitionID);
            final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(processClassloader);

                return startProcess(sProcessDefinitionID, userId, operations, operationsInputValues, connectorsWithInput, logger).getId();
            } finally {
                Thread.currentThread().setContextClassLoader(contextClassLoader);
            }
        } catch (final SBonitaException e) {
            throw new SCommandExecutionException(
                    "Error executing command 'Map<String, Serializable> ExecuteActionsAndStartInstanceExt(Map<Operation, Map<String, Serializable>> operationsMap, long processDefinitionID)'",
                    e);
        }
    }

    private ProcessInstance startProcess(final long processDefinitionId, final long userId, final List<Operation> operations,
            final Map<String, Object> context, final List<ConnectorDefinitionWithInputValues> connectorsWithInput, final TechnicalLoggerService logger)
            throws SProcessDefinitionException, SProcessDefinitionReadException, SInvalidExpressionException, SProcessInstanceCreationException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final ProcessExecutor processExecutor = tenantAccessor.getProcessExecutor();
        final SessionInfos session = SessionInfos.getSessionInfos();

        final long starterId = session.getUserId();
        final long starterForId;
        if (userId == 0) {
            starterForId = starterId;
        } else {
            starterForId = userId;
        }
        // Retrieval of the process definition:
        final SProcessDefinitionDeployInfo deployInfo = processDefinitionService.getProcessDeploymentInfo(processDefinitionId);
        if (ActivationState.DISABLED.name().equals(deployInfo.getActivationState())) {
            throw new SProcessDefinitionException("Process " + deployInfo.getName() + " in version " + deployInfo.getVersion() + " with id "
                    + deployInfo.getProcessId() + " is not enabled !!");
        }
        final SProcessDefinition sDefinition = getProcessDefinition(tenantAccessor, processDefinitionId);
        SProcessInstance startedInstance = null;
        try {
            final List<SOperation> sOperations = toSOperation(operations);
            startedInstance = processExecutor.start(starterId, starterForId, sOperations, context, connectorsWithInput, new FlowNodeSelector(
                    sDefinition, new StartFlowNodeFilter()));
        } catch (final SProcessInstanceCreationException e) {
            log(tenantAccessor, e);
            e.setProcessDefinitionOnContext(sDefinition);
            throw e;
        }
        if (logger.isLoggable(getClass(), TechnicalLogSeverity.INFO)) {
            final StringBuilder stb = new StringBuilder();
            stb.append("The user <");
            stb.append(session.getUsername());
            if (starterForId != starterId) {
                stb.append("> acting as delegate of user with id <");
                stb.append(starterForId);
            }
            stb.append("> has started instance <");
            stb.append(startedInstance.getId());
            stb.append("> of process <");
            stb.append(sDefinition.getName());
            stb.append("> in version <");
            stb.append(sDefinition.getVersion());
            stb.append("> and id <");
            stb.append(sDefinition.getId());
            stb.append(">");
            logger.log(getClass(), TechnicalLogSeverity.INFO, stb.toString());

        }
        return ModelConvertor.toProcessInstance(sDefinition, startedInstance);
    }

}
