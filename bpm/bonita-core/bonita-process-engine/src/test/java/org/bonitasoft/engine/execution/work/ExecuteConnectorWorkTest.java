/**
 * Copyright (C) 2016 Bonitasoft S.A.
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;

import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.classloader.SClassLoaderException;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.connector.ConnectorResult;
import org.bonitasoft.engine.core.connector.ConnectorService;
import org.bonitasoft.engine.core.connector.parser.SConnectorImplementationDescriptor;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.SConnectorDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SEndEventDefinition;
import org.bonitasoft.engine.core.process.instance.model.SConnectorInstance;
import org.bonitasoft.engine.core.process.instance.model.SFlowElementsContainerType;
import org.bonitasoft.engine.core.process.instance.model.event.SThrowEventInstance;
import org.bonitasoft.engine.lock.BonitaLock;
import org.bonitasoft.engine.lock.LockService;
import org.bonitasoft.engine.service.TenantServiceAccessor;
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


    private static final long PROCESS_DEFINITION_ID = 154323L;
    private static final long CONNECTOR_INSTANCE_ID = 95043L;
    private static final String CONNECTOR_NAME = "MyConnector";
    private static final long PROCESS_INSTANCE_ID = 92347844L;
    private static final long TENANT_ID = 125L;

    @Mock
    private TenantServiceAccessor tenantServiceAccessor;
    @Mock
    private LockService lockService;
    @Mock
    private UserTransactionService userTransactionService;
    private SExpressionContext expressionContext = new SExpressionContext(PROCESS_INSTANCE_ID, "PROCESS", PROCESS_DEFINITION_ID);
    private Map<String, Object> workContext = new HashMap<>();
    private ExecuteConnectorWork executeConnectorWork = new ExecuteConnectorWork(PROCESS_DEFINITION_ID, CONNECTOR_INSTANCE_ID, CONNECTOR_NAME, expressionContext, PROCESS_INSTANCE_ID) {
        @Override
        protected void errorEventOnFail(Map<String, Object> context, SConnectorDefinition sConnectorDefinition, Throwable Exception) throws SBonitaException {
        }

        @Override
        protected SThrowEventInstance createThrowErrorEventInstance(Map<String, Object> context, SEndEventDefinition eventDefinition) throws SBonitaException {
            return null;
        }

        @Override
        protected SConnectorDefinition getSConnectorDefinition(ProcessDefinitionService processDefinitionService) throws SBonitaException {
            return null;
        }

        @Override
        protected void setContainerInFail(Map<String, Object> context) throws SBonitaException {
        }

        @Override
        protected void continueFlow(Map<String, Object> context) throws SBonitaException {
        }

        @Override
        protected void evaluateOutput(Map<String, Object> context, ConnectorResult result, SConnectorDefinition sConnectorDefinition) throws SBonitaException {
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
    public void before() throws SClassLoaderException {
        workContext.put(TenantAwareBonitaWork.TENANT_ACCESSOR, tenantServiceAccessor);
        doReturn(lockService).when(tenantServiceAccessor).getLockService();
        doReturn(userTransactionService).when(tenantServiceAccessor).getUserTransactionService();
        executeConnectorWork.setTenantId(TENANT_ID);
        doReturn(classLoaderService).when(tenantServiceAccessor).getClassLoaderService();
        doReturn(this.getClass().getClassLoader()).when(classLoaderService).getLocalClassLoader(anyString(), anyLong());
        doReturn(timeTracker).when(tenantServiceAccessor).getTimeTracker();
        doReturn(connectorService).when(tenantServiceAccessor).getConnectorService();
    }

    @Test
    public void should_execute_do_operations_in_a_lock() throws Exception {
        //given
        //when
        executeConnectorWork.work(workContext);

        //then
        InOrder inOrder = inOrder(lockService, userTransactionService, connectorService);
        inOrder.verify(connectorService).executeConnector(anyLong(), nullable(SConnectorInstance.class), nullable(SConnectorImplementationDescriptor.class), any(ClassLoader.class), nullable(Map.class));
        inOrder.verify(lockService).lock(eq(PROCESS_INSTANCE_ID), eq(SFlowElementsContainerType.PROCESS.name()), eq(TENANT_ID));
        inOrder.verify(userTransactionService).executeInTransaction(isA(ExecuteConnectorWork.EvaluateConnectorOutputsTxContent.class));
        inOrder.verify(lockService).unlock(nullable(BonitaLock.class), eq(TENANT_ID));
    }
}
