/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.tenant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import com.bonitasoft.engine.BPMTestSPUtil;
import com.bonitasoft.engine.CommonAPISPIT;
import com.bonitasoft.engine.api.TenantStatusException;
import com.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilderExt;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.connector.Connector;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.ExpressionConstants;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.operation.LeftOperand;
import org.bonitasoft.engine.operation.LeftOperandBuilder;
import org.bonitasoft.engine.operation.Operation;
import org.bonitasoft.engine.operation.OperationBuilder;
import org.bonitasoft.engine.operation.OperatorType;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Laurent Leseigneur
 * @author Celine Souchet
 */
@Deprecated // Please, do not delete this class when cleaning deprecated TenantMaintenanceAPI. The following tests must be kept:
//- twoTenantPauseMode
//- can_executeConnectorOnActivityInstance_after_resume_tenant
public class TenantMaintenanceIT extends CommonAPISPIT {

    @Before
    public void before() throws BonitaException {
        loginOnDefaultTenantWithDefaultTechnicalUser();
    }

    private void disableAndDeleteProcess(final long tenantId, final long processDefinitionId) throws Exception {
        loginOnTenantWithTechnicalUser(tenantId);
        disableAndDeleteProcess(processDefinitionId);
    }

    @Test
    public void pause_one_tenant_should_not_impact_another_tenant() throws Exception {
        // given
        // first tenant
        final long tenantId1 = BPMTestSPUtil.loginOnDefaultTenantWithDefaultTechnicalUser().getTenantId();
        final User userForTenant1 = BPMTestSPUtil.createUserOnTenantWithDefaultTechnicalLogger(USERNAME, PASSWORD, tenantId1);

        // second tenant (with deployed process)
        final long tenantId2 = createAndActivateTenant("TenantPauseTestSP3");
        final User userForTenant2 = BPMTestSPUtil.createUserOnTenantWithDefaultTechnicalLogger(USERNAME, PASSWORD, tenantId2);
        final ProcessDefinition processDefinitionForTenant2 = createProcessOnTenant(tenantId2);

        // when a tenant is paused mode
        pauseTenant(tenantId1);

        //then
        assertCannotLoginOnTenant(tenantId1);
        assertCanLoginOnTenantAndStartProcess(tenantId2, processDefinitionForTenant2);

        // cleanup
        resumeTenant(tenantId1);
        deleteUser(userForTenant1);
        disableAndDeleteProcess(tenantId2, processDefinitionForTenant2.getId());
        deleteUser(userForTenant2);
        BPMTestSPUtil.deactivateAndDeleteTenant(tenantId2);
    }

    private void assertCannotLoginOnTenant(final long tenantId) throws Exception{
        try {
            loginOnTenantWith(USERNAME, PASSWORD, tenantId);
            fail("Expected that user is not able to do login, but he is");
        } catch (TenantStatusException e) {
            assertThat(e.getMessage()).contains("in pause");
        }
    }

    private void assertCanLoginOnTenantAndStartProcess(final long tenantId, final ProcessDefinition processDefinition) throws Exception{
        loginOnTenantWith(USERNAME, PASSWORD, tenantId);
        ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask(processInstance, "step1");
        logoutOnTenant();
    }

    @Test
    public void should_be_able_to_start_process_after_pause_resume() throws Exception {
        //given
        final long tenantId = BPMTestSPUtil.loginOnDefaultTenantWithDefaultTechnicalUser().getTenantId();
        final User user = BPMTestSPUtil.createUserOnTenantWithDefaultTechnicalLogger(USERNAME, PASSWORD, tenantId);
        final ProcessDefinition processDefinition = createProcessOnTenant(tenantId);

        // when
        pauseTenant(tenantId);

        //then
        assertCannotLoginOnTenant(tenantId);

        //when
        resumeTenant(tenantId);

        // then
        assertCanLoginOnTenantAndStartProcess(tenantId, processDefinition);

        // cleanup
        disableAndDeleteProcess(tenantId, processDefinition.getId());
        deleteUser(user);
    }

    @Cover(classes = Connector.class, concept = BPMNConcept.CONNECTOR, keywords = { "Connector", "Operation", "EngineExecutionContext" }, jira = "BS-8687")
    @Test
    public void can_executeConnectorOnActivityInstance_after_resume_tenant() throws Exception {
        final User user = createUser(USERNAME, PASSWORD);
        final LeftOperand leftOperand = new LeftOperandBuilder().createNewInstance().setName("externalData").setType(LeftOperand.TYPE_EXTERNAL_DATA).done();
        final Operation operation = new OperationBuilder().createNewInstance().setLeftOperand(leftOperand)
                .setRightOperand(new ExpressionBuilder().createInputExpression(ExpressionConstants.PROCESS_DEFINITION_ID.getEngineConstantName(),
                        Long.class.getName()))
                .setType(OperatorType.ASSIGNMENT).done();

        final ProcessDefinitionBuilderExt processDefinitionBuilderExt = new ProcessDefinitionBuilderExt().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processDefinitionBuilderExt.addActor(ACTOR_NAME);
        processDefinitionBuilderExt.addUserTask("step1", ACTOR_NAME);
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActorAndTestConnectorEngineExecutionContext(processDefinitionBuilderExt,
                ACTOR_NAME, user);

        // when: put in maintenance the tenant
        getTenantManagementAPI().pause();

        // when: we stop and start the node
        stopAndStartPlatform();
        loginOnDefaultTenantWithDefaultTechnicalUser();

        // when: resume the tenant
        getTenantManagementAPI().resume();

        // Start a process instance. The connector should work.
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final long step1Id = waitForUserTask(processInstance, "step1");

        final Map<String, Serializable> res = getProcessAPI().executeConnectorOnActivityInstance(
                "org.bonitasoft.connector.testConnectorEngineExecutionContext", "1.0", Collections.<String, Expression> emptyMap(),
                Collections.<String, Map<String, Serializable>> emptyMap(), Collections.singletonList(operation),
                Collections.<String, Serializable> emptyMap(), step1Id);

        assertEquals(processDefinition.getId(), res.get("externalData"));

        // Clean up
        disableAndDeleteProcess(processDefinition);
        deleteUser(user);
    }

    private ProcessDefinition createProcessOnTenant(final long tenantId) throws Exception {
        loginOnTenantWith(USERNAME, PASSWORD, tenantId);
        final String processName = new StringBuilder().append(PROCESS_NAME).append(tenantId).toString();
        final DesignProcessDefinition designProcessDefinition = new ProcessDefinitionBuilder()
                .createNewInstance(processName,
                        PROCESS_VERSION)
                .addActor(ACTOR_NAME)
                .addStartEvent("start event")
                .addUserTask("step1", ACTOR_NAME)
                .addEndEvent("end event").getProcess();

        return deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, getSession().getUserId());
    }

    private void pauseTenant(final long tenantId) throws BonitaException, UpdateException {
        loginOnTenantWithTechnicalUser(tenantId);
        getTenantManagementAPI().pause();
    }

    private void resumeTenant(final long tenantId) throws BonitaException, UpdateException {
        loginOnTenantWithTechnicalUser(tenantId);
        getTenantManagementAPI().resume();
    }
}
