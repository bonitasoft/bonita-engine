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
import org.bonitasoft.engine.core.process.definition.model.event.SEndEventDefinition;
import org.bonitasoft.engine.core.process.instance.model.SConnectorInstance;
import org.bonitasoft.engine.core.process.instance.model.SConnectorInstanceWithFailureInfo;
import org.bonitasoft.engine.core.process.instance.model.event.SThrowEventInstance;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.tracking.TimeTracker;
import org.bonitasoft.engine.tracking.TimeTrackerRecords;
import org.bonitasoft.engine.transaction.UserTransactionService;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public abstract class ExecuteConnectorWork extends TenantAwareBonitaWork {

    private static final long serialVersionUID = 9031279948838300081L;

    protected final long processDefinitionId;

    protected final long connectorInstanceId;

    protected final String connectorDefinitionName;

    private final SExpressionContext inputParametersContext;

    public ExecuteConnectorWork(final long processDefinitionId, final long connectorInstanceId, final String connectorDefinitionName,
            final SExpressionContext inputParametersContext) {
        this(processDefinitionId, connectorInstanceId, connectorDefinitionName, inputParametersContext, null);
    }

    public ExecuteConnectorWork(final long processDefinitionId, final long connectorInstanceId, final String connectorDefinitionName,
            final SExpressionContext inputParametersContext, final Map<String, Object> inputs) {
        super();
        this.processDefinitionId = processDefinitionId;
        this.connectorInstanceId = connectorInstanceId;
        this.connectorDefinitionName = connectorDefinitionName;
        this.inputParametersContext = inputParametersContext;
        this.inputParametersContext.setInputValues(inputs);
    }

    protected abstract void errorEventOnFail(Map<String, Object> context, SConnectorDefinition sConnectorDefinition, Exception Exception)
            throws SBonitaException;

    protected abstract SThrowEventInstance createThrowErrorEventInstance(Map<String, Object> context, final SEndEventDefinition eventDefinition)
            throws SBonitaException;

    protected abstract SConnectorDefinition getSConnectorDefinition(final ProcessDefinitionService processDefinitionService) throws SBonitaException;

    protected abstract void setContainerInFail(Map<String, Object> context) throws SBonitaException;

    protected abstract void continueFlow(Map<String, Object> context) throws SBonitaException;

    protected abstract void evaluateOutput(Map<String, Object> context, final ConnectorResult result, SConnectorDefinition sConnectorDefinition)
            throws SBonitaException;

    protected ClassLoader getClassLoader(final Map<String, Object> context) throws SBonitaException {
        return getTenantAccessor(context).getClassLoaderService().getLocalClassLoader(ScopeType.PROCESS.name(), processDefinitionId);
    }

    protected void setConnectorAndContainerToFailed(final Map<String, Object> context, final Exception Exception) throws SBonitaException {
        setConnectorOnlyToFailed(context, Exception);
        setContainerInFail(context);
    }

    protected void setConnectorOnlyToFailed(final Map<String, Object> context, final Exception Exception) throws SBonitaException {
        final ConnectorInstanceService connectorInstanceService = getTenantAccessor(context).getConnectorInstanceService();
        final SConnectorInstanceWithFailureInfo connectorInstanceWithFailure = connectorInstanceService
                .getConnectorInstanceWithFailureInfo(connectorInstanceId);
        connectorInstanceService.setState(connectorInstanceWithFailure, ConnectorService.FAILED);
        connectorInstanceService.setConnectorInstanceFailureException(connectorInstanceWithFailure, Exception);
    }

    protected void evaluateOutput(final Map<String, Object> context, final ConnectorResult result, final SConnectorDefinition sConnectorDefinition,
            final Long id, final String containerType) throws SBonitaException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor(context);
        final ConnectorInstanceService connectorInstanceService = tenantAccessor.getConnectorInstanceService();
        final ConnectorService connectorService = tenantAccessor.getConnectorService();
        final List<SOperation> outputs = sConnectorDefinition.getOutputs();
        final SExpressionContext sExpressionContext = new SExpressionContext(id, containerType, processDefinitionId);
        connectorService.executeOutputOperation(outputs, sExpressionContext, result);
        connectorInstanceService.setState(connectorInstanceService.getConnectorInstance(connectorInstanceId), ConnectorService.DONE);
    }

    @Override
    public void work(final Map<String, Object> context) throws Exception {
        final long startTime = System.currentTimeMillis();
        final TenantServiceAccessor tenantAccessor = getTenantAccessor(context);
        final ConnectorService connectorService = tenantAccessor.getConnectorService();
        final ConnectorInstanceService connectorInstanceService = tenantAccessor.getConnectorInstanceService();
        final UserTransactionService userTransactionService = tenantAccessor.getUserTransactionService();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final TimeTracker timeTracker = tenantAccessor.getTimeTracker();
        final ClassLoader processClassloader = getClassLoader(context);
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(processClassloader);
            final EvaluateParameterAndGetConnectorInstance callable = new EvaluateParameterAndGetConnectorInstance(connectorService, processDefinitionService,
                    connectorInstanceService);
            userTransactionService.executeInTransaction(callable);
            final SConnectorDefinition sConnectorDefinition = callable.getsConnectorDefinition();
            final SConnectorInstance connectorInstance = callable.getConnectorInstance();
            final ConnectorResult result = connectorService.executeConnector(processDefinitionId, connectorInstance, processClassloader,
                    callable.getInputParameters());
            // evaluate output and trigger the execution of the flow node
            userTransactionService.executeInTransaction(new EvaluateConnectorOutputsTxContent(result, sConnectorDefinition, context));
        } finally {
            if (timeTracker.isTrackable(TimeTrackerRecords.EXECUTE_CONNECTOR_WORK)) {
                final long endTime = System.currentTimeMillis();
                final StringBuilder desc = new StringBuilder();
                desc.append("processDefinitionId: ");
                desc.append(processDefinitionId);
                desc.append(" - ");
                desc.append("connectorDefinitionName: ");
                desc.append(connectorDefinitionName);
                desc.append(" - ");
                desc.append("connectorInstanceId: ");
                desc.append(connectorInstanceId);
                timeTracker.track(TimeTrackerRecords.EXECUTE_CONNECTOR_WORK, desc.toString(), endTime - startTime);
            }
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    @Override
    public void handleFailure(final Exception e, final Map<String, Object> context) throws Exception {
        final UserTransactionService userTransactionService = getTenantAccessor(context).getUserTransactionService();
        final ProcessDefinitionService processDefinitionService = getTenantAccessor(context).getProcessDefinitionService();
        if (handleError(context, userTransactionService, processDefinitionService, e)) {
            userTransactionService.executeInTransaction(new ContinueFlowTxContent(context));
        }
    }

    private boolean handleError(final Map<String, Object> context, final UserTransactionService userTransactionService,
            final ProcessDefinitionService processDefinitionService, final Exception e) throws Exception {
        final HandleConnectorOnFailEventTxContent handleError = new HandleConnectorOnFailEventTxContent(e, processDefinitionService, context);
        return userTransactionService.executeInTransaction(handleError);
    }

    private final class EvaluateParameterAndGetConnectorInstance implements Callable<Void> {

        private final ConnectorService connectorService;

        private final ConnectorInstanceService connectorInstanceService;

        private Map<String, Object> inputParameters;

        private SConnectorInstance connectorInstance;

        private final ProcessDefinitionService processDefinitionService;

        private SConnectorDefinition sConnectorDefinition;

        private EvaluateParameterAndGetConnectorInstance(final ConnectorService connectorService, final ProcessDefinitionService processDefinitionService,
                final ConnectorInstanceService connectorInstanceService) {
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
            inputParameters = connectorService.evaluateInputParameters(sConnectorDefinition.getConnectorId(), sConnectorDefinition.getInputs(),
                    inputParametersContext, null);
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

        private final Map<String, Object> context;

        private HandleConnectorOnFailEventTxContent(final Exception e, final ProcessDefinitionService processDefinitionService,
                final Map<String, Object> context) {
            this.e = e;
            this.processDefinitionService = processDefinitionService;
            this.context = context;

        }

        @Override
        public Boolean call() throws Exception {
            final SConnectorDefinition sConnectorDefinition = getSConnectorDefinition(processDefinitionService);
            switch (sConnectorDefinition.getFailAction()) {
                case ERROR_EVENT:
                    errorEventOnFail(context, sConnectorDefinition, e);
                    return false;
                case FAIL:
                    setConnectorAndContainerToFailed(context, e);
                    return false;
                case IGNORE:
                    setConnectorOnlyToFailed(context, e);
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

        private final Map<String, Object> context;

        public ContinueFlowTxContent(final Map<String, Object> context) {
            this.context = context;
        }

        @Override
        public Void call() throws Exception {
            continueFlow(context);
            return null;
        }
    }

    /**
     * @author Emmanuel Duchastenier
     */
    private final class EvaluateConnectorOutputsTxContent implements Callable<Void> {

        private final ConnectorResult result;

        private final SConnectorDefinition sConnectorDefinition;

        private final Map<String, Object> context;

        private EvaluateConnectorOutputsTxContent(final ConnectorResult result, final SConnectorDefinition sConnectorDefinition,
                final Map<String, Object> context) {
            this.result = result;
            this.sConnectorDefinition = sConnectorDefinition;
            this.context = context;
        }

        @Override
        public Void call() throws Exception {
            evaluateOutput(context, result, sConnectorDefinition);
            continueFlow(context);
            return null;
        }
    }

}
