/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine;

import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.process.impl.ConnectorDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.UserTaskDefinitionBuilder;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.InvalidExpressionException;
import org.bonitasoft.engine.test.BuildTestUtil;

import com.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilderExt;

public class BuildTestUtilSP {

    public static ProcessDefinitionBuilderExt buildProcessDefinitionWithFailedConnectorOnUserTask(final String processName)
            throws InvalidExpressionException {
        final ProcessDefinitionBuilderExt builder = new ProcessDefinitionBuilderExt().createNewInstance(processName, BuildTestUtil.PROCESS_VERSION);
        builder.addStartEvent("start");
        builder.addActor(BuildTestUtil.ACTOR_NAME);
        final UserTaskDefinitionBuilder userTaskBuilder = builder.addUserTask("StepWithFailedConnector", BuildTestUtil.ACTOR_NAME);
        final ConnectorDefinitionBuilder connectorDefinitionBuilder = userTaskBuilder.addConnector("testConnectorThatThrowException",
                "testConnectorThatThrowException", "1.0", ConnectorEvent.ON_FINISH);
        connectorDefinitionBuilder.addInput("kind", new ExpressionBuilder().createConstantStringExpression("plop"));
        builder.addUserTask("Step2", BuildTestUtil.ACTOR_NAME);
        builder.addEndEvent("end");
        builder.addTransition("start", "StepWithFailedConnector").addTransition("StepWithFailedConnector", "Step2").addTransition("Step2", "end");
        return builder;
    }
}
