/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.connector.exception.SConnectorException;
import org.bonitasoft.engine.core.connector.ConnectorInstanceService;
import org.bonitasoft.engine.core.connector.ConnectorResult;
import org.bonitasoft.engine.core.connector.ConnectorService;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.exception.SOperationExecutionException;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.SConnectorDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SEndEventDefinition;
import org.bonitasoft.engine.core.process.definition.model.impl.SConnectorDefinitionImpl;
import org.bonitasoft.engine.core.process.instance.model.SFlowElementsContainerType;
import org.bonitasoft.engine.core.process.instance.model.event.SThrowEventInstance;
import org.bonitasoft.engine.lock.BonitaLock;
import org.bonitasoft.engine.lock.LockService;
import org.bonitasoft.engine.service.ServiceAccessor;
import org.bonitasoft.engine.tracking.TimeTracker;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Baptiste Mesta
 */
@RunWith(MockitoJUnitRunner.class)
public class ExecuteConnectorWorkTest {

    private interface EvaluateOutputCallable {

        void call() throws SBonitaException;
    }

    private static final long PROCESS_DEFINITION_ID = 154323L;
    private static final long CONNECTOR_INSTANCE_ID = 95043L;
    private static final String CONNECTOR_NAME = "MyConnector";
    private static final long PROCESS_INSTANCE_ID = 92347844L;
    private static final long TENANT_ID = 125L;

    @Mock
    private ServiceAccessor serviceAccessor;
    @Mock
    private LockService lockService;
    @Mock
    private UserTransactionService userTransactionService;
    @Mock
    private ConnectorInstanceService connectorInstanceService;
    private final SConnectorDefinition sConnectorDefinition = new SConnectorDefinitionImpl("testDef", "connectorDef",
            "1.0",
            ConnectorEvent.ON_ENTER);
    private EvaluateOutputCallable evaluateOutput = () -> {
    };
    private final SExpressionContext expressionContext = new SExpressionContext(PROCESS_INSTANCE_ID, "PROCESS",
            PROCESS_DEFINITION_ID);
    private final Map<String, Object> workContext = new HashMap<>();
    private final ExecuteConnectorWork executeConnectorWork = new ExecuteConnectorWork(PROCESS_DEFINITION_ID,
            CONNECTOR_INSTANCE_ID, CONNECTOR_NAME, expressionContext, PROCESS_INSTANCE_ID) {

        @Override
        protected void errorEventOnFail(Map<String, Object> context, SConnectorDefinition sConnectorDefinition,
                Throwable Exception) throws SBonitaException {
        }

        @Override
        protected SThrowEventInstance createThrowErrorEventInstance(Map<String, Object> context,
                SEndEventDefinition eventDefinition) throws SBonitaException {
            return null;
        }

        @Override
        protected SConnectorDefinition getSConnectorDefinition(ProcessDefinitionService processDefinitionService)
                throws SBonitaException {
            return sConnectorDefinition;
        }

        @Override
        protected void setContainerInFail(Map<String, Object> context) throws SBonitaException {
        }

        @Override
        protected void continueFlow(Map<String, Object> context) throws SBonitaException {
        }

        @Override
        protected void evaluateOutput(Map<String, Object> context, ConnectorResult result,
                SConnectorDefinition sConnectorDefinition) throws SBonitaException {
            evaluateOutput.call();
        }

        @Override
        public String getDescription() {
            return null;
        }
    };
    @Mock
    private ClassLoaderService classLoaderService;
    @Mock
    private TimeTracker timeTracker;
    @Mock
    private ConnectorService connectorService;

    @Before
    public void before() throws Exception {
        workContext.put(TenantAwareBonitaWork.SERVICE_ACCESSOR, serviceAccessor);
        doReturn(lockService).when(serviceAccessor).getLockService();
        doReturn(userTransactionService).when(serviceAccessor).getUserTransactionService();
        doAnswer(args -> ((Callable) args.getArgument(0)).call()).when(userTransactionService)
                .executeInTransaction(any());
        executeConnectorWork.setTenantId(TENANT_ID);
        doReturn(classLoaderService).when(serviceAccessor).getClassLoaderService();
        doReturn(this.getClass().getClassLoader()).when(classLoaderService).getClassLoader(any());
        doReturn(timeTracker).when(serviceAccessor).getTimeTracker();
        doReturn(connectorService).when(serviceAccessor).getConnectorService();
        doReturn(connectorInstanceService).when(serviceAccessor).getConnectorInstanceService();
    }

    @Test
    public void should_execute_do_operations_in_a_lock() throws Exception {
        //given
        CompletableFuture<ConnectorResult> toBeReturned = completedFuture(
                new ConnectorResult(null, Collections.emptyMap(), 100));
        when(connectorService.executeConnector(anyLong(), any(), any(), any(), any())).thenReturn(toBeReturned);

        //when
        executeConnectorWork.work(workContext);

        //then
        InOrder inOrder = inOrder(lockService, userTransactionService);
        inOrder.verify(lockService).lock(eq(PROCESS_INSTANCE_ID), eq(SFlowElementsContainerType.PROCESS.name()),
                eq(TENANT_ID));
        inOrder.verify(userTransactionService).executeInTransaction(any());
        inOrder.verify(lockService).unlock(nullable(BonitaLock.class), eq(TENANT_ID));
    }

    @Test
    public void should_throw_SConnectorException_when_an_error_occurs_while_evaluating_output_operations()
            throws Exception {
        //We need to have an SConnectorException thrown when evaluating output operation because we don't want connectors to be automatically retried
        evaluateOutput = () -> {
            throw new SOperationExecutionException("Unable to save some connector output ¯\\_(ツ)_/¯");
        };
        CompletableFuture<ConnectorResult> toBeReturned = completedFuture(
                new ConnectorResult(null, Collections.emptyMap(), 100));
        when(connectorService.executeConnector(anyLong(), any(), any(), any(), any())).thenReturn(toBeReturned);

        CompletableFuture<Void> work = executeConnectorWork.work(workContext);

        assertThatThrownBy(work::get).satisfies(exception -> {
            assertThat(exception).hasCauseInstanceOf(SConnectorException.class);
            assertThat(exception.getCause())
                    .hasMessageContaining("Unable to evaluate output operations of connectors and continue");
            assertThat(exception.getCause()).hasCauseInstanceOf(SOperationExecutionException.class);
        });
    }
}
