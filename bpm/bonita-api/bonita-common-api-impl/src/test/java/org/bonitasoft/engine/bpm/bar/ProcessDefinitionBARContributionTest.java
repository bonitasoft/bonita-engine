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
 */
package org.bonitasoft.engine.bpm.bar;

import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.context.ContextEntryImpl;
import org.bonitasoft.engine.bpm.contract.Type;
import org.bonitasoft.engine.bpm.flownode.GatewayType;
import org.bonitasoft.engine.bpm.flownode.UserTaskDefinition;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.InvalidProcessDefinitionException;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.InvalidExpressionException;
import org.bonitasoft.engine.io.IOUtil;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Celine Souchet
 */
public class ProcessDefinitionBARContributionTest {

    private static final String PROCESS_NAME = "Name";

    private static final String PROCESS_VERSION = "1.0";

    private static final String DESCRIPTION = "Description";

    private static final String ACTOR_NAME = "Actor Name";

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public final void serializeDeserializeProcessDefinition() throws Exception {
        checkSerializeDeserializeProcessDefinition(createDesignProcessDefinition());
    }

    @Test
    public final void serializeDeserializeProcessDefinitionWithContract() throws Exception {
        final ProcessDefinitionBuilder processBuilder = createProcessBuilderDefinition();
        processBuilder.addUserTask("taskWithInput", ACTOR_NAME).addContract();
        processBuilder.addUserTask("taskWithConstraint", ACTOR_NAME).addContract().addInput("simpleInput", Type.INTEGER, DESCRIPTION)
                .addConstraint("Mandatory", "in != null", "in must be set", "in");
        processBuilder.addUserTask("taskWithComplexInput", ACTOR_NAME).addContract()
                .addComplexInput("complexInput", DESCRIPTION).addInput("simple", Type.TEXT, DESCRIPTION);
        processBuilder
                .addUserTask("taskWithComplexComplexInput", ACTOR_NAME)
                .addContract()
                .addComplexInput("complexInput", DESCRIPTION)
                .addInput("simple", Type.TEXT, DESCRIPTION)
                .addComplexInput("expense", DESCRIPTION)
                .addInput("name", Type.TEXT, DESCRIPTION)
                .addInput("amount", Type.DECIMAL, DESCRIPTION)
                .addInput("date", Type.DATE, DESCRIPTION)
                .addInput("proof", Type.BYTE_ARRAY, DESCRIPTION)
                .addComplexInput("adress", DESCRIPTION)
                .addInput("city", Type.TEXT, DESCRIPTION)
                .addInput("zip", Type.INTEGER, DESCRIPTION);

        checkSerializeDeserializeProcessDefinition(processBuilder.done());
    }

    @Test
    public void should_deserializeProcessDefinition_of_old_process_throw_exception() throws Exception {
        exception.expect(InvalidBusinessArchiveFormatException.class);
        exception.expectMessage("Deserialization of the ProcessDesignFailed");
        final String allContentFrom = IOUtil.read(getClass().getResourceAsStream("/old-process.xml"));
        final File createTempFile = IOUtil.createTempFileInDefaultTempDirectory("old", "process.xml");

        try {
            IOUtil.writeContentToFile(allContentFrom, createTempFile);
            new ProcessDefinitionBARContribution().deserializeProcessDefinition(createTempFile);
        } finally {
            createTempFile.delete();
        }
    }

    @Test
    public void should_checkVersion_with_old_content_thrown_exception() throws Exception {
        exception.expect(InvalidBusinessArchiveFormatException.class);
        exception.expectMessage("6.0 namespace is not compatible with your current version");

        new ProcessDefinitionBARContribution().checkVersion(IOUtil.read(getClass().getResourceAsStream("/old-process.xml")));
    }

    @Test
    public void should_checkVersion_with_bad_content_thrown_exception() throws Exception {
        exception.expect(InvalidBusinessArchiveFormatException.class);
        exception.expectMessage("There is no bonitasoft process namespace declaration");

        new ProcessDefinitionBARContribution().checkVersion("invalid");
    }

    private DesignProcessDefinition checkSerializeDeserializeProcessDefinition(final DesignProcessDefinition designProcessDefinition) throws IOException,
            InvalidBusinessArchiveFormatException {
        // Serialize designProcessDefinition
        final File processDesignFolder = temporaryFolder.newFolder();
        final ProcessDefinitionBARContribution processDefinitionBARContribution = new ProcessDefinitionBARContribution();
        processDefinitionBARContribution.serializeProcessDefinition(processDesignFolder, designProcessDefinition);

        // Deserialize designProcessDefinition
        final File processDesignFile = new File(processDesignFolder, ProcessDefinitionBARContribution.PROCESS_DEFINITION_XML);
        final DesignProcessDefinition resultDesignProcessDefinition = processDefinitionBARContribution.deserializeProcessDefinition(processDesignFile);
        assertThat(resultDesignProcessDefinition).isEqualTo(designProcessDefinition);
        return resultDesignProcessDefinition;
    }

    private DesignProcessDefinition createDesignProcessDefinition() throws InvalidExpressionException, InvalidProcessDefinitionException {
        final ProcessDefinitionBuilder processBuilder = createProcessBuilderDefinition();
        return processBuilder.done();
    }

    private ProcessDefinitionBuilder createProcessBuilderDefinition() throws InvalidExpressionException {
        final Expression targetProcessNameExpr = new ExpressionBuilder().createConstantStringExpression(PROCESS_NAME);
        final Expression targetProcessVersionExpr = new ExpressionBuilder().createConstantStringExpression(PROCESS_VERSION);

        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processBuilder.addActor(ACTOR_NAME, true);
        processBuilder.addActor("actor2").addDescription(DESCRIPTION);
        processBuilder.addDescription(DESCRIPTION);
        processBuilder.addAutomaticTask("AutomaticTask").addCallActivity("CallActivity", targetProcessNameExpr, targetProcessVersionExpr)
                .addManualTask("ManualTask", ACTOR_NAME).addBoundaryEvent("BoundaryEvent").addSignalEventTrigger("signalName");
        processBuilder.addUserTask("UserTask", ACTOR_NAME).addUserFilter("test", "org.bonitasoft.engine.filter.user.testFilter", "1.0")
                .addInput("userId", new ExpressionBuilder().createConstantLongExpression(3));
        processBuilder.addConnector("testConnectorThatThrowException", "testConnectorThatThrowException", "1.0", ConnectorEvent.ON_ENTER);
        processBuilder.addDocumentDefinition("Doc").addUrl("plop");
        processBuilder.addGateway("Gateway", GatewayType.PARALLEL).addDescription(DESCRIPTION);
        processBuilder.addBlobData("BlobData", null).addDescription("blolbDescription").addBooleanData("BooleanData", null);
        processBuilder.addDisplayName("plop").addDisplayDescription("plop2").addEndEvent("EndEvent");
        processBuilder.addIntermediateCatchEvent("IntermediateCatchEvent").addIntermediateThrowEvent("IntermediateThrowEvent");
        processBuilder.addReceiveTask("ReceiveTask", "messageName");
        processBuilder.addSendTask("SendTask", "messageName", targetProcessNameExpr);
        processBuilder.addTransition("BoundaryEvent", "ManualTask");
        return processBuilder;
    }

    @Test
    public final void check_process_with_context() throws Exception {
        Expression value1 = new ExpressionBuilder().createConstantStringExpression("value1");
        Expression value2 = new ExpressionBuilder().createConstantStringExpression("value2");
        Expression processValue1 = new ExpressionBuilder().createConstantStringExpression("processValue1");
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("ProcessWithContext", "1.0");
        processBuilder.addUserTask("taskWithContext", ACTOR_NAME).addContextEntry("key1", value1).addContextEntry("key2", value2);
        processBuilder.addContextEntry("processKey1", processValue1);
        processBuilder.addUserTask("taskWithoutContext", ACTOR_NAME);
        processBuilder.addActor(ACTOR_NAME);

        DesignProcessDefinition designProcessDefinition = checkSerializeDeserializeProcessDefinition(processBuilder.done());
        assertThat(designProcessDefinition.getContext()).containsExactly(new ContextEntryImpl("processKey1", processValue1));
        assertThat(((UserTaskDefinition) designProcessDefinition.getFlowElementContainer().getActivity("taskWithContext")).getContext()).containsExactly(
                new ContextEntryImpl("key1", value1), new ContextEntryImpl("key2", value2));
        assertThat(((UserTaskDefinition) designProcessDefinition.getFlowElementContainer().getActivity("taskWithoutContext")).getContext()).isEmpty();
    }

}
