/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package org.bonitasoft.engine.test;

import static org.junit.Assert.assertEquals;

import org.bonitasoft.engine.bpm.model.ActivityInstance;
import org.bonitasoft.engine.bpm.model.ConnectorEvent;
import org.bonitasoft.engine.bpm.model.ProcessDefinition;
import org.bonitasoft.engine.bpm.model.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.model.ProcessInstance;
import org.bonitasoft.engine.connector.Connector;
import org.bonitasoft.engine.connectors.ConnectorExecutionTests;
import org.bonitasoft.engine.exception.InvalidSessionException;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.Test;

import com.bonitasoft.engine.connector.impl.ConnectorExecutorTimedOut;
import com.bonitasoft.engine.service.TenantServiceAccessor;
import com.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import com.bonitasoft.engine.service.impl.TenantServiceSingleton;

/**
 * @author Baptiste Mesta
 */
public class ConnectorExecutionTimeOutTest extends ConnectorExecutionTests {

    protected TenantServiceAccessor getTenantAccessor() throws InvalidSessionException {
        try {
            final SessionAccessor sessionAccessor = ServiceAccessorFactory.getInstance().createSessionAccessor();
            final long tenantId = sessionAccessor.getTenantId();
            return TenantServiceSingleton.getInstance(tenantId);
        } catch (final Exception e) {
            throw new InvalidSessionException(e);
        }
    }

    @Cover(classes = Connector.class, concept = BPMNConcept.PROCESS, keywords = { "Connector", "Execution too long" }, jira = "ENGINE-472", story = "Test if connector fails when connector execution is too long.")
    @Test
    public void testExecuteConnectorWithExecutionTooLong() throws Exception {
        final String delivery = "Delivery men";
        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("testConnectorWithExecutionTooLong", "1.0");
        designProcessDefinition.addActor(delivery).addDescription("Delivery all day and night long");
        designProcessDefinition.addAutomaticTask("step1").addConnector("myConnector1", "testConnectorLongToExecute", "1.0.0", ConnectorEvent.ON_ENTER)
                .addInput("timeout", new ExpressionBuilder().createConstantLongExpression(350));

        final long userId = getIdentityAPI().getUserByUserName(JOHN).getId();
        final ProcessDefinition processDefinition = deployProcessWithDefaultTestConnector(delivery, userId, designProcessDefinition);
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ConnectorExecutorTimedOut connectorExecutor = (ConnectorExecutorTimedOut) tenantAccessor.getConnectorExecutor();
        final long oldTimeout = connectorExecutor.getTimeout();
        connectorExecutor.setTimeout(300);
        try {
            final ProcessInstance process = getProcessAPI().startProcess(processDefinition.getId());
            final ActivityInstance failedTask = waitForTaskToFail(process);
            assertEquals("step1", failedTask.getName());
            System.out.println("second start");
            connectorExecutor.setTimeout(oldTimeout);
            final ProcessInstance process2 = getProcessAPI().startProcess(processDefinition.getId());
            waitForProcessToFinish(process2);
            deleteUser(JOHN);
            disableAndDelete(processDefinition);
        } finally {
            connectorExecutor.setTimeout(oldTimeout);
        }
    }

}
