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
package org.bonitasoft.engine.execution.work;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.connector.ConnectorInstanceService;
import org.bonitasoft.engine.core.connector.ConnectorResult;
import org.bonitasoft.engine.core.connector.ConnectorService;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.SConnectorDefinition;
import org.bonitasoft.engine.core.process.definition.model.builder.BPMDefinitionBuilders;
import org.bonitasoft.engine.core.process.definition.model.event.SEndEventDefinition;
import org.bonitasoft.engine.core.process.instance.model.SConnectorInstance;
import org.bonitasoft.engine.core.process.instance.model.event.SThrowEventInstance;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.transaction.TransactionService;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public abstract class ExecuteConnectorWork extends NonTxBonitaWork {

    private static final long serialVersionUID = 9031279948838300081L;

    protected final long processDefinitionId;

    protected final long connectorInstanceId;

    // protected final long connectorDefinitionId;// FIXME: Uncomment when generate id

    protected final String connectorDefinitionName;

    private final SExpressionContext inputParametersContext;

    public ExecuteConnectorWork(final long processDefinitionId, final long connectorInstanceId, final String connectorDefinitionName,
            SExpressionContext inputParametersContext) {
        super();
        this.processDefinitionId = processDefinitionId;
        this.connectorInstanceId = connectorInstanceId;
        this.connectorDefinitionName = connectorDefinitionName;
        this.inputParametersContext = inputParametersContext;
    }

    protected abstract void errorEventOnFail(SConnectorDefinition sConnectorDefinition) throws SBonitaException;

    protected abstract SThrowEventInstance createThrowErrorEventInstance(final SEndEventDefinition eventDefinition) throws SBonitaException;

    protected abstract SConnectorDefinition getSConnectorDefinition(final ProcessDefinitionService processDefinitionService) throws SBonitaException;

    protected abstract void setContainerInFail() throws SBonitaException;

    protected abstract void continueFlow() throws SBonitaException;

    protected abstract void evaluateOutput(final ConnectorResult result, SConnectorDefinition sConnectorDefinition) throws SBonitaException;

    protected ClassLoader getClassLoader() throws SBonitaException {
        return getTenantAccessor().getClassLoaderService().getLocalClassLoader("process", processDefinitionId);
    }

    protected void setConnectorAndContainerToFailed() throws SBonitaException {
        setConnectorOnlyToFailed();
        setContainerInFail();
    }

    protected void setConnectorOnlyToFailed() throws SBonitaException {
        final ConnectorInstanceService connectorInstanceService = getTenantAccessor().getConnectorInstanceService();
        final SConnectorInstance intTxConnectorInstance = connectorInstanceService.getConnectorInstance(connectorInstanceId);
        connectorInstanceService.setState(intTxConnectorInstance, ConnectorService.FAILED);
    }

    protected void evaluateOutput(final ConnectorResult result, SConnectorDefinition sConnectorDefinition, final Long id, final String containerType)
            throws SBonitaException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ConnectorInstanceService connectorInstanceService = tenantAccessor.getConnectorInstanceService();
        final ConnectorService connectorService = tenantAccessor.getConnectorService();
        final List<SOperation> outputs = sConnectorDefinition.getOutputs();
        final SExpressionContext sExpressionContext = new SExpressionContext(id, containerType, processDefinitionId);
        connectorService.executeOutputOperation(outputs, sExpressionContext, result);
        connectorInstanceService.setState(connectorInstanceService.getConnectorInstance(connectorInstanceId), ConnectorService.DONE);
    }

    @Override
    protected void work() throws Exception {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ConnectorService connectorService = tenantAccessor.getConnectorService();
        final ConnectorInstanceService connectorInstanceService = tenantAccessor.getConnectorInstanceService();
        final TransactionService transactionService = tenantAccessor.getTransactionService();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final ClassLoader processClassloader = getClassLoader();
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            SConnectorDefinition sConnectorDefinition = null;
            Thread.currentThread().setContextClassLoader(processClassloader);
            EvaluateParameterAndGetConnectorInstance callable = new EvaluateParameterAndGetConnectorInstance(connectorService,
                    processDefinitionService, connectorInstanceService);
            transactionService.executeInTransaction(callable);
            sConnectorDefinition = callable.getsConnectorDefinition();
            final SConnectorInstance connectorInstance = callable.getConnectorInstance();
            final ConnectorResult result = connectorService.executeConnector(processDefinitionId, connectorInstance, processClassloader,
                    callable.getInputParameters());
            transactionService.executeInTransaction(new EvaluateConnectorOutputsTxContent(result, sConnectorDefinition));
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
        transactionService.executeInTransaction(new ContinueFlowTxContent());
    }

    @Override
    protected void handleFailure(Exception e) throws Exception {
        TransactionService transactionService = getTenantAccessor().getTransactionService();
        ProcessDefinitionService processDefinitionService = getTenantAccessor().getProcessDefinitionService();
        if (handleError(transactionService, processDefinitionService, e)) {
            transactionService.executeInTransaction(new ContinueFlowTxContent());
        }
    }

    private boolean handleError(final TransactionService transactionService, ProcessDefinitionService processDefinitionService,
            final Exception e) throws Exception {
        HandleConnectorOnFailEventTxContent handleError;
        handleError = new HandleConnectorOnFailEventTxContent(e, processDefinitionService);
        return transactionService.executeInTransaction(handleError);
    }

    protected BPMDefinitionBuilders getBPMDefinitionBuilders() {
        return getTenantAccessor().getBPMDefinitionBuilders();
    }

    private final class EvaluateParameterAndGetConnectorInstance implements Callable<Void> {

        private final ConnectorService connectorService;

        private final ConnectorInstanceService connectorInstanceService;

        private Map<String, Object> inputParameters;

        private SConnectorInstance connectorInstance;

        private final ProcessDefinitionService processDefinitionService;

        private SConnectorDefinition sConnectorDefinition;

        private EvaluateParameterAndGetConnectorInstance(ConnectorService connectorService, ProcessDefinitionService processDefinitionService,
                ConnectorInstanceService connectorInstanceService) {
            this.connectorService = connectorService;
            this.processDefinitionService = processDefinitionService;
            this.connectorInstanceService = connectorInstanceService;
        }

        public Map<String, Object> getInputParameters() {
            return inputParameters;
        }

        public SConnectorInstance getConnectorInstance() {
            return connectorInstance;
        }

        public SConnectorDefinition getsConnectorDefinition() {
            return sConnectorDefinition;
        }

        @Override
        public Void call() throws Exception {
            sConnectorDefinition = getSConnectorDefinition(processDefinitionService);
            inputParameters = connectorService.evaluateInputParameters(sConnectorDefinition.getInputs(), inputParametersContext, null);
            connectorInstance = connectorInstanceService.getConnectorInstance(connectorInstanceId);
            return null;
        }
    }

    /**
     * @author Emmanuel Duchastenier
     */
    private final class HandleConnectorOnFailEventTxContent implements Callable<Boolean> {

        private final Exception e;

        private final ProcessDefinitionService processDefinitionService;

        private HandleConnectorOnFailEventTxContent(final Exception e, ProcessDefinitionService processDefinitionService) {
            this.e = e;
            this.processDefinitionService = processDefinitionService;
        }

        @Override
        public Boolean call() throws Exception {
            loggerService.log(getClass(), TechnicalLogSeverity.WARNING,
                    "Error while executing connector with id " + connectorInstanceId, e);
            SConnectorDefinition sConnectorDefinition = getSConnectorDefinition(processDefinitionService);
            switch (sConnectorDefinition.getFailAction()) {
                case ERROR_EVENT:
                    errorEventOnFail(sConnectorDefinition);
                    return false;
                case FAIL:
                    setConnectorAndContainerToFailed();
                    return false;
                case IGNORE:
                    setConnectorOnlyToFailed();
                    return true;
                default:
                    throw new Exception("No action defined for " + sConnectorDefinition.getFailAction());
            }
        }
    }

    /**
     * @author Emmanuel Duchastenier
     */
    private final class ContinueFlowTxContent implements Callable<Void> {

        @Override
        public Void call() throws Exception {
            continueFlow();
            return null;
        }
    }

    /**
     * @author Emmanuel Duchastenier
     */
    private final class EvaluateConnectorOutputsTxContent implements Callable<Void> {

        private final ConnectorResult result;

        private final SConnectorDefinition sConnectorDefinition;

        /**
         * @param result
         * @param sConnectorDefinition
         */
        private EvaluateConnectorOutputsTxContent(final ConnectorResult result, SConnectorDefinition sConnectorDefinition) {
            this.result = result;
            this.sConnectorDefinition = sConnectorDefinition;
        }

        @Override
        public Void call() throws Exception {
            evaluateOutput(result, sConnectorDefinition);
            return null;
        }
    }
}
