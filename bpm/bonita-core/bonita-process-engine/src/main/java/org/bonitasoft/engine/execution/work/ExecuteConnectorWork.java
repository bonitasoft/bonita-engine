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

    protected abstract void errorEventOnFail() throws SBonitaException;

    protected abstract SThrowEventInstance createThrowErrorEventInstance(final SEndEventDefinition eventDefinition) throws SBonitaException;

    protected abstract SConnectorDefinition getSConnectorDefinition(final TenantServiceAccessor tenantAccessor) throws SBonitaException;

    protected abstract void setContainerInFail() throws SBonitaException;

    protected abstract void continueFlow() throws SBonitaException;

    protected abstract void evaluateOutput(final ConnectorResult result) throws SBonitaException;

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

    protected void evaluateOutput(final ConnectorResult result, final Long id, final String containerType) throws SBonitaException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ConnectorInstanceService connectorInstanceService = tenantAccessor.getConnectorInstanceService();
        final ConnectorService connectorService = tenantAccessor.getConnectorService();
        final SConnectorDefinition sConnectorDefinition = getSConnectorDefinition(tenantAccessor);
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
        final SConnectorDefinition sConnectorDefinition = getSConnectorDefinition(tenantAccessor);

        final ClassLoader processClassloader = getClassLoader();
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        HandleConnectorOnFailEventTxContent handleError = null;
        try {
            Thread.currentThread().setContextClassLoader(processClassloader);
            try {
                EvaluateParameterAndGetConnectorInstance transactionContent = new EvaluateParameterAndGetConnectorInstance(connectorService,
                        sConnectorDefinition, connectorInstanceService);
                transactionExecutor.execute(transactionContent);
                final SConnectorInstance connectorInstance = transactionContent.getConnectorInstance();
                final ConnectorResult result = connectorService.executeConnector(processDefinitionId, connectorInstance, processClassloader,
                        transactionContent.getInputParameters());
                transactionExecutor.execute(new EvaluateConnectorOutputsTxContent(result));
            } catch (final SBonitaException e) {
                handleError = new HandleConnectorOnFailEventTxContent(e, sConnectorDefinition);
                transactionExecutor.execute(handleError);
            }
            if (handleError == null || handleError.shouldContinueFlow()) {
                transactionExecutor.execute(new ContinueFlowTxContent());
            }
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    protected BPMDefinitionBuilders getBPMDefinitionBuilders() {
        return getTenantAccessor().getBPMDefinitionBuilders();
    }

    private final class EvaluateParameterAndGetConnectorInstance implements TransactionContent {

        private final ConnectorService connectorService;

        private final SConnectorDefinition sConnectorDefinition;

        private final ConnectorInstanceService connectorInstanceService;

        private Map<String, Object> inputParameters;

        private SConnectorInstance connectorInstance;

        private EvaluateParameterAndGetConnectorInstance(ConnectorService connectorService, SConnectorDefinition sConnectorDefinition,
                ConnectorInstanceService connectorInstanceService) {
            this.connectorService = connectorService;
            this.sConnectorDefinition = sConnectorDefinition;
            this.connectorInstanceService = connectorInstanceService;
        }

        @Override
        public void execute() throws SBonitaException {
            inputParameters = connectorService.evaluateInputParameters(sConnectorDefinition.getInputs(), inputParametersContext, null);
            connectorInstance = connectorInstanceService.getConnectorInstance(connectorInstanceId);
        }

        public Map<String, Object> getInputParameters() {
            return inputParameters;
        }

        public SConnectorInstance getConnectorInstance() {
            return connectorInstance;
        }
    }

    /**
     * @author Emmanuel Duchastenier
     */
    private final class HandleConnectorOnFailEventTxContent implements TransactionContent {

        private final SBonitaException e;

        private final SConnectorDefinition sConnectorDefinition;

        private boolean continueFlow;

        private HandleConnectorOnFailEventTxContent(final SBonitaException e, final SConnectorDefinition sConnectorDefinition) {
            this.e = e;
            this.sConnectorDefinition = sConnectorDefinition;
        }

        @Override
        public void execute() throws SBonitaException {
            loggerService.log(getClass(), TechnicalLogSeverity.WARNING, "Error while executing connector", e);
            // TODO: store connection root cause in connnector_instance DB
            switch (sConnectorDefinition.getFailAction()) {
                case ERROR_EVENT:
                    errorEventOnFail();
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

        /**
         * @param result
         */
        private EvaluateConnectorOutputsTxContent(final ConnectorResult result) {
            this.result = result;
        }

        @Override
        public void execute() throws SBonitaException {
            evaluateOutput(result);
        }
    }
}
