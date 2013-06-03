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
import org.bonitasoft.engine.classloader.ClassLoaderException;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionExecutor;
import org.bonitasoft.engine.core.connector.ConnectorInstanceService;
import org.bonitasoft.engine.core.connector.ConnectorResult;
import org.bonitasoft.engine.core.connector.ConnectorService;
import org.bonitasoft.engine.core.connector.exception.SConnectorException;
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
import org.bonitasoft.engine.transaction.STransactionException;
import org.bonitasoft.engine.work.BonitaWork;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public abstract class ExecuteConnectorWork extends BonitaWork {

    protected final SProcessDefinition processDefinition;

    protected final ClassLoaderService classLoaderService;

    protected final TransactionExecutor transactionExecutor;

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
        ClassLoader processClassloader;
        try {
            processClassloader = classLoaderService.getLocalClassLoader("process", processDefinition.getId());
        } catch (final ClassLoaderException e) {
            // retry with a transaction if the classloader need to access dependencies in database (classloader was not initialized)
            final boolean txOpened = transactionExecutor.openTransaction();
            try {
                processClassloader = classLoaderService.getLocalClassLoader("process", processDefinition.getId());
            } catch (final SBonitaException be) {
                transactionExecutor.setTransactionRollback();
                throw be;
            } finally {
                transactionExecutor.completeTransaction(txOpened);
            }
        }
        return processClassloader;
    }

    protected void ignoreOnFail() throws SBonitaException {
        setInFailInTransaction(true);
    }

    protected void failOnFail(final SBonitaException e) throws SBonitaException {
        setInFailInTransaction(false);
        throw e;
    }

    protected abstract void errorEventOnFail(final SBonitaException errorThrownWhenEvaluationOfInputParameters2) throws SBonitaException;

    protected abstract SThrowEventInstance createThrowErrorEventInstance(SEndEventDefinition eventDefinition) throws SBonitaException;

    protected ConnectorResult executeConnector(final ClassLoader processClassloader) throws SBonitaException {

        ConnectorResult result = null;
        try {
            result = connectorService.executeConnector(processDefinition.getId(), connector, processClassloader, inputParameters);
        } catch (final SConnectorException e) {
            onFail(e);
        }
        return result;
    }

    public void onFail(final SBonitaException e) throws SBonitaException {
        switch (sConnectorDefinition.getFailAction()) {
            case ERROR_EVENT:
                errorEventOnFail(e);
                break;
            case FAIL:
                failOnFail(e);
                break;
            case IGNORE:
                ignoreOnFail();
                break;
            default:
                break;
        }
    }

    protected abstract void setContainerInFail() throws SBonitaException;

    protected void setInFailInTransaction(final boolean onlyConnector) throws STransactionException {
        boolean txOpened = false;
        try {
            txOpened = transactionExecutor.openTransaction();
            setInFail(onlyConnector);
        } catch (final SBonitaException e) {
            transactionExecutor.setTransactionRollback();
        } finally {
            transactionExecutor.completeTransaction(txOpened);
        }
    }

    private void setInFail(final boolean onlyConnector) throws SBonitaException {
        final SConnectorInstance intTxConnectorInstance = connectorInstanceService.getConnectorInstance(connector.getId());
        connectorInstanceService.setState(intTxConnectorInstance, ConnectorService.FAILED);
        if (!onlyConnector) {
            setContainerInFail();
        }
    }

    protected abstract void continueFlow(ClassLoader classLoader) throws SBonitaException;

    protected void evaluateOutput(final ClassLoader processClassloader, final ConnectorResult result, final Long id, final String containerType)
            throws SBonitaException {
        if (result != null) {
            final List<SOperation> outputs = sConnectorDefinition.getOutputs();
            final boolean txOpened = transactionExecutor.openTransaction();
            final SExpressionContext sExpressionContext = new SExpressionContext(id, containerType, processDefinition.getId());
            try {
                final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
                try {
                    Thread.currentThread().setContextClassLoader(processClassloader);
                    connectorService.executeOutputOperation(outputs, sExpressionContext, result);
                } finally {
                    Thread.currentThread().setContextClassLoader(contextClassLoader);
                }
                connectorInstanceService.setState(connectorInstanceService.getConnectorInstance(connector.getId()), ConnectorService.DONE);
            } catch (final SBonitaException e) {
                transactionExecutor.setTransactionRollback();
                throw e;
            } finally {
                transactionExecutor.completeTransaction(txOpened);
            }
        }
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

    protected abstract void evaluateOutput(ClassLoader processClassloader, ConnectorResult result) throws SBonitaException;

    @Override
    protected void work() throws SBonitaException {
        final ClassLoader processClassloader = getClassLoader();
        if (getErrorThrownWhenEvaluationOfInputParameters() != null) {
            onFail(getErrorThrownWhenEvaluationOfInputParameters());
        } else {
            final ConnectorResult result = executeConnector(processClassloader);
            try {
                evaluateOutput(processClassloader, result);
            } catch (final SBonitaException e) {
                onFail(e);
            }
        }
        continueFlow(processClassloader);
    }

}
