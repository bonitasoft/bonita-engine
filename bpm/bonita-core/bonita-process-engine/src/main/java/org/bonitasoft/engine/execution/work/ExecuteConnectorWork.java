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

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.commons.transaction.TransactionExecutor;
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
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final ClassLoader processClassloader = getClassLoader();
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            SConnectorDefinition sConnectorDefinition = null;
            Thread.currentThread().setContextClassLoader(processClassloader);
            EvaluateParameterAndGetConnectorInstance transactionContent = new EvaluateParameterAndGetConnectorInstance(connectorService,
                    processDefinitionService, connectorInstanceService);
            transactionExecutor.execute(transactionContent);
            sConnectorDefinition = transactionContent.getsConnectorDefinition();
            final SConnectorInstance connectorInstance = transactionContent.getConnectorInstance();
            final ConnectorResult result = connectorService.executeConnector(processDefinitionId, connectorInstance, processClassloader,
                    transactionContent.getInputParameters());
            transactionExecutor.execute(new EvaluateConnectorOutputsTxContent(result, sConnectorDefinition));
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
        transactionExecutor.execute(new ContinueFlowTxContent());
    }

    @Override
    protected void handleError(SBonitaException e) {
        TransactionExecutor transactionExecutor = getTenantAccessor().getTransactionExecutor();
        ProcessDefinitionService processDefinitionService = getTenantAccessor().getProcessDefinitionService();
        try {
            if (handleError(transactionExecutor, processDefinitionService, e).shouldContinueFlow()) {
                transactionExecutor.execute(new ContinueFlowTxContent());
            }
        } catch (Exception e1) {
            // exception during the handling of the exception... we just log it with the original one and also the original one
            loggerService.log(this.getClass(), TechnicalLogSeverity.ERROR,
                    "Unable to handle the exception that happened execution of the connector because of an other exception", e1);
        }
    }

    private HandleConnectorOnFailEventTxContent handleError(final TransactionExecutor transactionExecutor, ProcessDefinitionService processDefinitionService,
            final SBonitaException e) throws SBonitaException {
        HandleConnectorOnFailEventTxContent handleError;
        handleError = new HandleConnectorOnFailEventTxContent(e, processDefinitionService);
        transactionExecutor.execute(handleError);
        return handleError;
    }

    protected BPMDefinitionBuilders getBPMDefinitionBuilders() {
        return getTenantAccessor().getBPMDefinitionBuilders();
    }

    private final class EvaluateParameterAndGetConnectorInstance implements TransactionContent {

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

        @Override
        public void execute() throws SBonitaException {
            sConnectorDefinition = getSConnectorDefinition(processDefinitionService);
            inputParameters = connectorService.evaluateInputParameters(sConnectorDefinition.getInputs(), inputParametersContext, null);
            connectorInstance = connectorInstanceService.getConnectorInstance(connectorInstanceId);
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
    }

    /**
     * @author Emmanuel Duchastenier
     */
    private final class HandleConnectorOnFailEventTxContent implements TransactionContent {

        private final SBonitaException e;

        private boolean continueFlow;

        private final ProcessDefinitionService processDefinitionService;

        private HandleConnectorOnFailEventTxContent(final SBonitaException e, ProcessDefinitionService processDefinitionService) {
            this.e = e;
            this.processDefinitionService = processDefinitionService;
        }

        @Override
        public void execute() throws SBonitaException {
            loggerService.log(getClass(), TechnicalLogSeverity.WARNING,
                    "Error while executing connector with id " + connectorInstanceId, e);
            SConnectorDefinition sConnectorDefinition = getSConnectorDefinition(processDefinitionService);
            // TODO: store connector exception root cause in connnector_instance DB
            switch (sConnectorDefinition.getFailAction()) {
                case ERROR_EVENT:
                    errorEventOnFail(sConnectorDefinition);
                    continueFlow = false;
                    break;
                case FAIL:
                    setConnectorAndContainerToFailed();
                    continueFlow = false;
                    break;
                case IGNORE:
                    setConnectorOnlyToFailed();
                    continueFlow = true;
                    break;
                default:
                    continueFlow = false;
                    break;
            }
        }

        public Boolean shouldContinueFlow() {
            return continueFlow;
        }
    }

    /**
     * @author Emmanuel Duchastenier
     */
    private final class ContinueFlowTxContent implements TransactionContent {

        @Override
        public void execute() throws SBonitaException {
            continueFlow();
        }
    }

    /**
     * @author Emmanuel Duchastenier
     */
    private final class EvaluateConnectorOutputsTxContent implements TransactionContent {

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
        public void execute() throws SBonitaException {
            evaluateOutput(result, sConnectorDefinition);
        }
    }
}
