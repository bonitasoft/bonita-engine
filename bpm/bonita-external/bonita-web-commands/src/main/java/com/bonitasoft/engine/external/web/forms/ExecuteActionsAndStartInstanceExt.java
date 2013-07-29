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

import org.bonitasoft.engine.bpm.connector.ConnectorDefinitionWithInputValues;
import org.bonitasoft.engine.bpm.process.ActivationState;
import org.bonitasoft.engine.bpm.process.ProcessDefinitionNotEnabledException;
import org.bonitasoft.engine.bpm.process.ProcessDefinitionNotFoundException;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.command.SCommandExecutionException;
import org.bonitasoft.engine.command.SCommandParameterizationException;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.commons.transaction.TransactionExecutor;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.operation.model.builder.SOperationBuilders;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.SProcessDefinitionNotFoundException;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionReadException;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinitionDeployInfo;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.execution.ProcessExecutor;
import org.bonitasoft.engine.expression.model.builder.SExpressionBuilders;
import org.bonitasoft.engine.external.web.forms.ExecuteActionsBaseEntry;
import org.bonitasoft.engine.operation.Operation;
import org.bonitasoft.engine.operation.OperationExecutionException;
import org.bonitasoft.engine.service.ModelConvertor;
import org.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * @author Ruiheng Fan
 * @author Celine Souchet
 * @author Matthieu Chaffotte
 */
public class ExecuteActionsAndStartInstanceExt extends ExecuteActionsBaseEntry {

    @SuppressWarnings("unchecked")
    @Override
    public Serializable execute(final Map<String, Serializable> parameters, final TenantServiceAccessor serviceAccessor)
            throws SCommandParameterizationException, SCommandExecutionException {
        final List<Operation> operations;
        final List<ConnectorDefinitionWithInputValues> connectorsWithInput;
        final Map<String, Object> operationsInputValues;
        try {
            operations = (List<Operation>) parameters.get(OPERATIONS_LIST_KEY);
        } catch (final Exception e) {
            throw new SCommandParameterizationException("Mandatory parameter " + OPERATIONS_LIST_KEY + " is missing or not convertible to List.", e);
        }
        try {
            operationsInputValues = (Map<String, Object>) parameters.get(OPERATIONS_INPUT_KEY);
        } catch (final Exception e) {
            throw new SCommandParameterizationException("Mandatory parameter " + OPERATIONS_INPUT_KEY + " is missing or not convertible to Map.", e);
        }

        try {
            connectorsWithInput = (List<ConnectorDefinitionWithInputValues>) parameters.get(CONNECTORS_LIST_KEY);
        } catch (final Exception e) {
            throw new SCommandParameterizationException("Mandatory parameter " + CONNECTORS_LIST_KEY + " is missing or not convertible to List.", e);
        }

        long sProcessDefinitionID = 0L;
        try {
            sProcessDefinitionID = (Long) parameters.get(PROCESS_DEFINITION_ID_KEY);
        } catch (final Exception e) {
            throw new SCommandParameterizationException("Mandatory parameter " + PROCESS_DEFINITION_ID_KEY + " is missing or not convertible to long.", e);
        }

        long userId;
        try {
            userId = (Long) parameters.get(USER_ID_KEY);
        } catch (final Exception e) {
            throw new SCommandParameterizationException("Mandatory parameter " + USER_ID_KEY + " is missing or not convertible to String.", e);
        }

        try {
            final ClassLoader processClassloader;
            final ClassLoaderService classLoaderService = serviceAccessor.getClassLoaderService();
            processClassloader = classLoaderService.getLocalClassLoader("process", sProcessDefinitionID);
            final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(processClassloader);

                return startProcess(userId, sProcessDefinitionID, operations, operationsInputValues, connectorsWithInput).getId();
            } finally {
                Thread.currentThread().setContextClassLoader(contextClassLoader);
            }
        } catch (final BonitaException e) {
            throw new SCommandExecutionException(
                    "Error executing command 'Map<String, Serializable> ExecuteActionsAndStartInstanceExt(Map<Operation, Map<String, Serializable>> operationsMap, long processDefinitionID)'",
                    e);
        } catch (final SBonitaException e) {
            throw new SCommandExecutionException(
                    "Error executing command 'Map<String, Serializable> ExecuteActionsAndStartInstanceExt(Map<Operation, Map<String, Serializable>> operationsMap, long processDefinitionID)'",
                    e);
        }
    }

    private ProcessInstance startProcess(long userId, final long processDefinitionId, final List<Operation> operations, final Map<String, Object> context,
            final List<ConnectorDefinitionWithInputValues> connectorsWithInput) throws ProcessDefinitionNotFoundException, CreationException,
            RetrieveException, ProcessDefinitionNotEnabledException, OperationExecutionException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final ProcessExecutor processExecutor = tenantAccessor.getProcessExecutor();
        final SOperationBuilders sOperationBuilders = tenantAccessor.getSOperationBuilders();
        final SExpressionBuilders sExpressionBuilders = tenantAccessor.getSExpressionBuilders();
        if (userId == 0) {
            userId = getUserIdFromSession();
        }
        // Retrieval of the process definition:
        SProcessDefinition sDefinition;
        try {
            final GetProcessDeploymentInfo transactionContentWithResult = new GetProcessDeploymentInfo(processDefinitionId, processDefinitionService);
            transactionExecutor.execute(transactionContentWithResult);
            final SProcessDefinitionDeployInfo deployInfo = transactionContentWithResult.getResult();
            if (ActivationState.DISABLED.name().equals(deployInfo.getActivationState())) {
                throw new ProcessDefinitionNotEnabledException(deployInfo.getName(), deployInfo.getVersion(), deployInfo.getProcessId());
            }
            sDefinition = getServerProcessDefinition(processDefinitionId, processDefinitionService);
        } catch (final SProcessDefinitionNotFoundException e) {
            throw new ProcessDefinitionNotFoundException(e);
        } catch (final SBonitaException e) {
            throw new RetrieveException(e);
        }
        SProcessInstance startedInstance;
        try {
            final List<SOperation> sOperations = toSOperation(operations, sOperationBuilders, sExpressionBuilders);
            startedInstance = processExecutor.start(userId, sDefinition, sOperations, context, connectorsWithInput);
        } catch (final SBonitaException e) {
            log(tenantAccessor, e);
            throw new CreationException(e);
        }// FIXME in case process instance creation exception -> put it in failed
        return ModelConvertor.toProcessInstance(sDefinition, startedInstance);
    }

    /**
     * @author Baptiste Mesta
     */
    private final class GetProcessDeploymentInfo implements TransactionContentWithResult<SProcessDefinitionDeployInfo> {

        private final Long processDefinitionUUID;

        private final ProcessDefinitionService processDefinitionService;

        private SProcessDefinitionDeployInfo processDefinitionDI;

        private GetProcessDeploymentInfo(final Long processDefinitionUUID, final ProcessDefinitionService processDefinitionService) {
            this.processDefinitionUUID = processDefinitionUUID;
            this.processDefinitionService = processDefinitionService;
        }

        @Override
        public void execute() throws SProcessDefinitionNotFoundException, SProcessDefinitionReadException {
            processDefinitionDI = processDefinitionService.getProcessDeploymentInfo(processDefinitionUUID);
        }

        @Override
        public SProcessDefinitionDeployInfo getResult() {
            return processDefinitionDI;
        }
    }

}
