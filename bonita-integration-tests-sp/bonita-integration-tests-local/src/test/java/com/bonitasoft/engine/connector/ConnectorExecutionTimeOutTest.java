/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.connector;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.connector.Connector;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.operation.Operation;
import org.bonitasoft.engine.operation.OperationBuilder;
import org.bonitasoft.engine.operation.OperatorType;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.Test;

import com.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilderExt;
import com.bonitasoft.engine.connector.impl.ConnectorExecutorTimedOut;
import com.bonitasoft.engine.service.TenantServiceAccessor;
import com.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import com.bonitasoft.engine.service.impl.TenantServiceSingleton;

/**
 * @author Baptiste Mesta
 */
public class ConnectorExecutionTimeOutTest extends ConnectorExecutionTest {

    protected TenantServiceAccessor getTenantAccessor() {
        try {
            final SessionAccessor sessionAccessor = ServiceAccessorFactory.getInstance().createSessionAccessor();
            final long tenantId = sessionAccessor.getTenantId();
            return TenantServiceSingleton.getInstance(tenantId);
        } catch (final Exception e) {
            throw new BonitaRuntimeException(e);
        }
    }

    @Cover(classes = Connector.class, concept = BPMNConcept.PROCESS, keywords = { "Connector", "Execution too long" }, jira = "ENGINE-472", story = "Test if connector fails when connector execution is too long.")
    @Test
    public void executeConnectorWithExecutionTooLong() throws Exception {
        final ProcessDefinitionBuilderExt designProcessDefinition = new ProcessDefinitionBuilderExt().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        designProcessDefinition.addActor(ACTOR_NAME);
        designProcessDefinition.addAutomaticTask("step1").addConnector("myConnector1", "testConnectorLongToExecute", "1.0.0", ConnectorEvent.ON_ENTER)
                .addInput("timeout", new ExpressionBuilder().createConstantLongExpression(5000));

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActorAndTestConnectorLongToExecute(designProcessDefinition, ACTOR_NAME, user);
        final SessionAccessor sessionAccessor = ServiceAccessorFactory.getInstance().createSessionAccessor();
        sessionAccessor.setSessionInfo(getSession().getId(), getSession().getTenantId()); // set session info
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ConnectorExecutorTimedOut connectorExecutor = (ConnectorExecutorTimedOut) tenantAccessor.getConnectorExecutor();
        final long oldTimeout = connectorExecutor.getTimeout();
        connectorExecutor.setTimeout(1);
        try {
            final ProcessInstance process = getProcessAPI().startProcess(processDefinition.getId());
            final ActivityInstance failedTask = waitForTaskToFail(process);
            assertEquals("step1", failedTask.getName());
            sessionAccessor.setSessionInfo(getSession().getId(), getSession().getTenantId()); // set session info cleaned by api call
            connectorExecutor.setTimeout(oldTimeout);
            final ProcessInstance process2 = getProcessAPI().startProcess(processDefinition.getId());
            waitForProcessToFinish(process2);
        } finally {
            disableAndDeleteProcess(processDefinition);
            sessionAccessor.setSessionInfo(getSession().getId(), getSession().getTenantId()); // set session info cleaned by api call
            connectorExecutor.setTimeout(oldTimeout);
            sessionAccessor.deleteSessionId();
        }
    }

}
