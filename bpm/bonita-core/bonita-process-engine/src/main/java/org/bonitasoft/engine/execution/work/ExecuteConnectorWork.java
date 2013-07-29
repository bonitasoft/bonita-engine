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

import org.bonitasoft.engine.bpm.model.impl.BPMInstancesCreator;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.commons.transaction.TransactionExecutor;
import org.bonitasoft.engine.core.connector.ConnectorInstanceService;
import org.bonitasoft.engine.core.connector.ConnectorResult;
import org.bonitasoft.engine.core.connector.ConnectorService;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.process.definition.model.SConnectorDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.builder.BPMDefinitionBuilders;
import org.bonitasoft.engine.core.process.definition.model.event.SEndEventDefinition;
import org.bonitasoft.engine.core.process.instance.model.SConnectorInstance;
import org.bonitasoft.engine.core.process.instance.model.builder.BPMInstanceBuilders;
import org.bonitasoft.engine.core.process.instance.model.event.SThrowEventInstance;
import org.bonitasoft.engine.execution.event.EventsHandler;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.work.NonTxBonitaWork;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public abstract class ExecuteConnectorWork extends NonTxBonitaWork {

    /**
     * @author Emmanuel Duchastenier
     */
    private final class HandleConnectorOnFailEventTxContent implements TransactionContent {

        private final SBonitaException e;

        private boolean continueFlow;

        private HandleConnectorOnFailEventTxContent(final SBonitaException e) {
            this.e = e;
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

    protected final SProcessDefinition processDefinition;

    protected final ClassLoaderService classLoaderService;

    private final TransactionExecutor transactionExecutor;

    protected final SConnectorInstance connector;

    protected final SConnectorDefinition sConnectorDefinition;

    protected final ConnectorService connectorService;

    protected final Map<String, Object> inputParameters;

    protected EventsHandler eventsHandler;

    protected BPMInstanceBuilders bpmInstanceBuilders;

    protected BPMInstancesCreator bpmInstancesCreator;

    protected BPMDefinitionBuilders bpmDefinitionBuilders;

    protected final ConnectorInstanceService connectorInstanceService;

    private SBonitaException errorThrownWhenEvaluationOfInputParameters;

    public ExecuteConnectorWork(final SProcessDefinition processDefinition, final ClassLoaderService classLoaderService,
            final TransactionExecutor transactionExecutor, final SConnectorInstance connector, final SConnectorDefinition sConnectorDefinition,
            final ConnectorService connectorService, final ConnectorInstanceService connectorInstanceService, final Map<String, Object> inputParameters,
            final EventsHandler eventsHandler, final BPMInstanceBuilders bpmInstanceBuilders, final BPMInstancesCreator bpmInstancesCreator,
            final BPMDefinitionBuilders bpmDefinitionBuilders) {
        super();
        this.processDefinition = processDefinition;
        this.classLoaderService = classLoaderService;
        this.transactionExecutor = transactionExecutor;
        this.connector = connector;
        this.sConnectorDefinition = sConnectorDefinition;
        this.connectorService = connectorService;
        this.connectorInstanceService = connectorInstanceService;
        this.inputParameters = inputParameters;
        this.eventsHandler = eventsHandler;
        this.bpmInstanceBuilders = bpmInstanceBuilders;
        this.bpmInstancesCreator = bpmInstancesCreator;
        this.bpmDefinitionBuilders = bpmDefinitionBuilders;
    }

    protected ClassLoader getClassLoader() throws SBonitaException {
        return classLoaderService.getLocalClassLoader("process", processDefinition.getId());
    }

    protected abstract void errorEventOnFail() throws SBonitaException;

    protected abstract SThrowEventInstance createThrowErrorEventInstance(SEndEventDefinition eventDefinition) throws SBonitaException;

    protected void setConnectorAndContainerToFailed() throws SBonitaException {
        setConnectorOnlyToFailed();
        setContainerInFail();
    }

    protected void setConnectorOnlyToFailed() throws SBonitaException {
        final SConnectorInstance intTxConnectorInstance = connectorInstanceService.getConnectorInstance(connector.getId());
        connectorInstanceService.setState(intTxConnectorInstance, ConnectorService.FAILED);
    }

    protected abstract void setContainerInFail() throws SBonitaException;

    protected abstract void continueFlow() throws SBonitaException;

    protected void evaluateOutput(final ConnectorResult result, final Long id, final String containerType) throws SBonitaException {
        final List<SOperation> outputs = sConnectorDefinition.getOutputs();
        final SExpressionContext sExpressionContext = new SExpressionContext(id, containerType, processDefinition.getId());
        connectorService.executeOutputOperation(outputs, sExpressionContext, result);
        connectorInstanceService.setState(connectorInstanceService.getConnectorInstance(connector.getId()), ConnectorService.DONE);
    }

    public void setErrorThrownWhenEvaluationOfInputParameters(final SBonitaException errorThrownWhenEvaluationOfInputParameters) {
        this.errorThrownWhenEvaluationOfInputParameters = errorThrownWhenEvaluationOfInputParameters;
    }

    /**
     * @return the errorThrownWhenEvaluationOfInputParameters
     *         the error thrown when evaluating input parameters or null if there was no error when evaluating input parameters
     */
    public SBonitaException getErrorThrownWhenEvaluationOfInputParameters() {
        return errorThrownWhenEvaluationOfInputParameters;
    }

    protected abstract void evaluateOutput(ConnectorResult result) throws SBonitaException;

    @Override
    protected void work() throws SBonitaException {
        final ClassLoader processClassloader = getClassLoader();
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        HandleConnectorOnFailEventTxContent handleError = null;
        try {
            Thread.currentThread().setContextClassLoader(processClassloader);
            if (errorThrownWhenEvaluationOfInputParameters != null) {
                handleError = new HandleConnectorOnFailEventTxContent(errorThrownWhenEvaluationOfInputParameters);
                transactionExecutor.execute(handleError);
            } else {
                try {
                    final ConnectorResult result = connectorService.executeConnector(processDefinition.getId(), connector, processClassloader, inputParameters);
                    transactionExecutor.execute(new EvaluateConnectorOutputsTxContent(result));
                } catch (final SBonitaException e) {
                    handleError = new HandleConnectorOnFailEventTxContent(e);
                    transactionExecutor.execute(handleError);
                }
            }
            if (handleError == null || handleError.shouldContinueFlow()) {
                transactionExecutor.execute(new ContinueFlowTxContent());
            }
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }
}
