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
package org.bonitasoft.engine.bpm.bar;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.flownode.GatewayType;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.InvalidProcessDefinitionException;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.InvalidExpressionException;
import org.junit.Test;

/**
 * @author Celine Souchet
 * 
 */
public class ProcessDefinitionBARContributionTest {

    private static final String PROCESS_NAME = "Name";

    private static final String PROCESS_VERSION = "1.0";

    private static final String DESCRIPTION = "Description";

    private static final String ACTOR_NAME = "Actor Name";

    /**
     * Test method for
     * {@link org.bonitasoft.engine.bpm.bar.ProcessDefinitionBARContribution#serializeProcessDefinition(java.io.File, org.bonitasoft.engine.bpm.process.DesignProcessDefinition)}
     * 
     * Test method for {@link org.bonitasoft.engine.bpm.bar.ProcessDefinitionBARContribution#deserializeProcessDefinition(java.io.File)}.
     */
    @Test
    public final void serializeDeserializeProcessDefinition() throws Exception {
        final DesignProcessDefinition designProcessDefinition = createDesignProcessDefinition();

        // Serialize designProcessDefinition
        final File processDesignFolder = new File(System.getProperty("java.io.tmpdir"), "tmpDesignProcessDefinition");
        processDesignFolder.mkdir();
        final ProcessDefinitionBARContribution processDefinitionBARContribution = new ProcessDefinitionBARContribution();
        processDefinitionBARContribution.serializeProcessDefinition(processDesignFolder, designProcessDefinition);

        // Deserialize designProcessDefinition
        final File processDesignFile = new File(processDesignFolder, ProcessDefinitionBARContribution.PROCESS_DEFINITION_XML);
        final DesignProcessDefinition resultDesignProcessDefinition = processDefinitionBARContribution.deserializeProcessDefinition(processDesignFile);
        assertEquals(designProcessDefinition, resultDesignProcessDefinition);

        // Clean up
        processDesignFolder.getParentFile().delete();
    }

    private DesignProcessDefinition createDesignProcessDefinition() throws InvalidExpressionException, InvalidProcessDefinitionException {
        final Expression targetProcessNameExpr = new ExpressionBuilder().createConstantStringExpression(PROCESS_NAME);
        final Expression targetProcessVersionExpr = new ExpressionBuilder().createConstantStringExpression(PROCESS_VERSION);

        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processBuilder.addActor(ACTOR_NAME, true);
        processBuilder.addActor("actor2").addDescription(DESCRIPTION);
        processBuilder.addDescription(DESCRIPTION);
        processBuilder.addAutomaticTask("AutomaticTask").addCallActivity("CallActivity", targetProcessNameExpr, targetProcessVersionExpr)
                .addManualTask("ManualTask", ACTOR_NAME)
                .addBoundaryEvent("BoundaryEvent").addSignalEventTrigger("signalName");
        processBuilder.addUserTask("UserTask", ACTOR_NAME).addUserFilter("test", "org.bonitasoft.engine.filter.user.testFilter", "1.0")
                .addInput("userId", new ExpressionBuilder().createConstantLongExpression(3));
        processBuilder.addConnector("testConnectorThatThrowException", "testConnectorThatThrowException", "1.0",
                ConnectorEvent.ON_ENTER);
        processBuilder.addDocumentDefinition("Doc").addUrl("plop");
        processBuilder.addGateway("Gateway", GatewayType.PARALLEL).addDescription(DESCRIPTION);
        processBuilder.addBlobData("BlobData", null).addDescription("blolbDescription").addBooleanData("BooleanData", null);
        processBuilder.addDisplayName("plop").addDisplayDescription("plop2").addEndEvent("EndEvent");
        processBuilder.addIntermediateCatchEvent("IntermediateCatchEvent").addIntermediateThrowEvent("IntermediateThrowEvent");
        processBuilder.addReceiveTask("ReceiveTask", "messageName");
        processBuilder.addSendTask("SendTask", "messageName", targetProcessNameExpr);
        processBuilder.addTransition("BoundaryEvent", "ManualTask");
        return processBuilder.done();
    }

    /**
     * Test method for {@link org.bonitasoft.engine.bpm.bar.ProcessDefinitionBARContribution#isMandatory()}.
     */
    @Test
    public final void isMandatory() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.bpm.bar.ProcessDefinitionBARContribution#readFromBarFolder(org.bonitasoft.engine.bpm.bar.BusinessArchive, java.io.File)}.
     */
    @Test
    public final void readFromBarFolder() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.bpm.bar.ProcessDefinitionBARContribution#checkProcessInfos(java.io.File, org.bonitasoft.engine.bpm.process.DesignProcessDefinition)}
     * .
     */
    @Test
    public final void checkProcessInfos() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.bpm.bar.ProcessDefinitionBARContribution#deserializeProcessDefinition(java.io.File)}.
     */
    @Test
    public final void deserializeProcessDefinition() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.bpm.bar.ProcessDefinitionBARContribution#saveToBarFolder(org.bonitasoft.engine.bpm.bar.BusinessArchive, java.io.File)}.
     */
    @Test
    public final void saveToBarFolder() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.bpm.bar.ProcessDefinitionBARContribution#serializeProcessDefinition(java.io.File, org.bonitasoft.engine.bpm.process.DesignProcessDefinition)}
     * .
     */
    @Test
    public final void serializeProcessDefinition() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.bpm.bar.ProcessDefinitionBARContribution#generateInfosFromDefinition(org.bonitasoft.engine.bpm.process.DesignProcessDefinition)}
     * .
     */
    @Test
    public final void generateInfosFromDefinition() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.bpm.bar.ProcessDefinitionBARContribution#getProcessInfos(java.lang.String)}.
     */
    @Test
    public final void getProcessInfos() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.bpm.bar.ProcessDefinitionBARContribution#getName()}.
     */
    @Test
    public final void getName() {
        // TODO : Not yet implemented
    }

}
