/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.process;

import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.identity.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.bonitasoft.engine.CommonAPISPTest;
import com.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilderExt;

public class ProcessWithBusinessDataTest extends CommonAPISPTest {

    @After
    public void afterTest() throws BonitaException {
        logout();
    }

    @Before
    public void beforeTest() throws BonitaException {
        login();
    }

    @Test
    public void deployProcessWithBusinessDataShouldBeRetrievable() throws Exception {
        final User user = createUser("login1", "password");
        ProcessDefinition processDefinition = null;
        try {
            final ProcessDefinitionBuilderExt processBuilder = new ProcessDefinitionBuilderExt().createNewInstance("firstProcess", "1.0");
            processBuilder.addActor("myActor");
            processBuilder.addBusinessData("myBizData", Long.class.getName(), new ExpressionBuilder().createConstantLongExpression(12L));
            processBuilder.addUserTask("Request", "myActor");
            processDefinition = deployAndEnableWithActor(processBuilder.done(), "myActor", user);
            // Should not fail here, if the Server process model is valid:

            disableAndDeleteProcess(processDefinition);
        } finally {
            deleteUser(user);
        }
    }

}
