package com.bonitasoft.engine.migration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.bpm.model.ActivityInstance;
import org.bonitasoft.engine.bpm.model.ConnectorDefinition;
import org.bonitasoft.engine.bpm.model.ProcessDefinition;
import org.bonitasoft.engine.bpm.model.ProcessInstance;
import org.bonitasoft.engine.bpm.model.breakpoint.Breakpoint;
import org.bonitasoft.engine.bpm.model.breakpoint.BreakpointCriterion;
import org.bonitasoft.engine.core.operation.Operation;
import org.bonitasoft.engine.core.operation.OperatorType;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.InvalidMigrationPlanException;
import org.bonitasoft.engine.exception.InvalidSessionException;
import org.bonitasoft.engine.exception.MigrationPlanCreationException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.migration.ConnectorDefinitionWithEnablement;
import org.bonitasoft.engine.migration.MigrationMapping;
import org.bonitasoft.engine.migration.MigrationPlan;
import org.bonitasoft.engine.migration.MigrationPlanCriterion;
import org.bonitasoft.engine.migration.MigrationPlanDescriptor;
import org.bonitasoft.engine.migration.OperationWithEnablement;
import org.bonitasoft.engine.test.TestStates;
import org.bonitasoft.engine.util.IOUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.bonitasoft.engine.CommonAPISPTest;
import com.bonitasoft.engine.bpm.model.ProcessDefinitionBuilder;

/**
 * @author Frédéric Bouquet
 */

public class MigrationTest extends CommonAPISPTest {

    private static final String JOHN = "john";

    private User john;

    @After
    public void afterTest() throws BonitaException {
        deleteUser(JOHN);
        logout();
    }

    @Before
    public void beforeTest() throws BonitaException {
        login();
        john = createUser(JOHN, "bpm");
        loginWith(JOHN, "bpm");
    }

    @Test
    public void importMigrationPlan() throws Exception {
        final long id = importMigrationPlan("migrationplan.xml");
        final MigrationPlan migrationPlan = getMigrationAPI().getMigrationPlan(id);
        assertNotNull(migrationPlan);
        assertEquals("a complexe migration plan", migrationPlan.getDescription());
        assertEquals("srcProcess", migrationPlan.getSourceName());
        assertEquals("1.0", migrationPlan.getSourceVersion());
        assertEquals("targetProcess", migrationPlan.getTargetName());
        assertEquals("1.1", migrationPlan.getTargetVersion());
        assertEquals(1, migrationPlan.getMappings().size());
        final MigrationMapping migrationMapping = migrationPlan.getMappings().get(0);
        assertEquals("step1", migrationMapping.getSourceName());
        assertEquals(1, migrationMapping.getSourceState());
        assertEquals("step2", migrationMapping.getTargetName());
        assertEquals(4, migrationMapping.getTargetState());
        assertEquals(1, migrationMapping.getConnectors().size());
        assertConnectorEquals(migrationMapping.getConnectors().get(0));
        assertEquals(1, migrationMapping.getOperations().size());
        assertOperationEquals(migrationMapping.getOperations().get(0));
        assertEquals(1, migrationPlan.getConnectors().size());
        assertConnectorEquals(migrationPlan.getConnectors().get(0));
        assertEquals(1, migrationPlan.getOperations().size());
        assertOperationEquals(migrationPlan.getOperations().get(0));
        getMigrationAPI().deleteMigrationPlan(id);
    }

    private long importMigrationPlan(final String xmlFile) throws IOException, InvalidSessionException, InvalidMigrationPlanException,
            MigrationPlanCreationException {
        final InputStream resourceAsStream = this.getClass().getResourceAsStream("/org/bonitasoft/engine/migration/" + xmlFile);
        final byte[] content = IOUtil.getAllContentFrom(resourceAsStream);
        final MigrationPlanDescriptor migrationPlanDesciptor = getMigrationAPI().importMigrationPlan(content);
        return migrationPlanDesciptor.getId();
    }

    private void assertOperationEquals(final OperationWithEnablement operationWithEnablement) {
        assertSingleOperationEquals(operationWithEnablement.getOperation());
        assertExpressionEquals(operationWithEnablement.getEnablement());
    }

    private void assertExpressionEquals(final Expression enablement) {
        assertEquals("trueExpr", enablement.getName());
        assertEquals("CONSTANT", enablement.getExpressionType());
        assertEquals("NONE", enablement.getInterpreter());
        assertEquals("java.lang.Boolean", enablement.getReturnType());
        assertEquals("true", enablement.getContent());
    }

    /**
     * @param connectorDefinitionWithEnablement
     */
    private void assertConnectorEquals(final ConnectorDefinitionWithEnablement connectorDefinitionWithEnablement) {
        final ConnectorDefinition connector = connectorDefinitionWithEnablement.getConnector();
        assertEquals("aConnector", connector.getName());
        assertEquals("org.connector.id", connector.getConnectorId());
        assertEquals("1.0", connector.getVersion());
        assertEquals(1, connector.getInputs().size());
        final Expression input = connector.getInputs().get("input1");
        assertGroovyExprEquals(input);
        assertEquals(1, connector.getOutputs().size());
        final Operation output = connector.getOutputs().get(0);
        assertSingleOperationEquals(output);
        final Expression enablement = connectorDefinitionWithEnablement.getEnablement();
        assertExpressionEquals(enablement);
    }

    /**
     * @param expression
     */
    private void assertGroovyExprEquals(final Expression expression) {
        assertEquals("aGroovyExpr", expression.getName());
        assertEquals("READ_WRITE_SCRIPT", expression.getExpressionType());
        assertEquals("GROOVY", expression.getInterpreter());
        assertEquals("java.lang.Integer", expression.getReturnType());
        assertEquals("a + b", expression.getContent());
    }

    /**
     * @param operation
     */
    private void assertSingleOperationEquals(final Operation operation) {
        assertEquals("=", operation.getOperator());
        assertEquals(OperatorType.ASSIGNMENT, operation.getType());
        assertEquals("c", operation.getLeftOperand().getName());
        assertGroovyExprEquals(operation.getRightOperand());
    }

    @Test(expected = InvalidMigrationPlanException.class)
    public void importInvalidMigrationPlan() throws Exception {
        getMigrationAPI().importMigrationPlan(new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 });
    }

    @Test
    public void getNumberOfMigrationPlan() throws Exception {
        assertEquals(0, getMigrationAPI().getNumberOfMigrationPlan());
        final long id1 = importMigrationPlan("migrationplan.xml");
        assertEquals(1, getMigrationAPI().getNumberOfMigrationPlan());
        final long id2 = importMigrationPlan("migrationplan_simple.xml");
        assertEquals(2, getMigrationAPI().getNumberOfMigrationPlan());
        getMigrationAPI().deleteMigrationPlan(id1);
        getMigrationAPI().deleteMigrationPlan(id2);
        assertEquals(0, getMigrationAPI().getNumberOfMigrationPlan());
    }

    @Test
    public void getMigrationPlansDescriptors() throws Exception {
        final int initialSize = getMigrationAPI().getMigrationPlanDescriptors(0, 5, MigrationPlanCriterion.SOURCE_PROCESS_NAME_ASC).size();
        final long id1 = importMigrationPlan("migrationplan.xml");
        final long id2 = importMigrationPlan("migrationplan_simple.xml");
        final List<MigrationPlanDescriptor> migrationPlanDescriptors = getMigrationAPI().getMigrationPlanDescriptors(0, 5,
                MigrationPlanCriterion.SOURCE_PROCESS_NAME_ASC);
        assertEquals(initialSize + 2, migrationPlanDescriptors.size());
        assertEquals(id1, migrationPlanDescriptors.get(0).getId());
        assertEquals(id2, migrationPlanDescriptors.get(1).getId());
        getMigrationAPI().deleteMigrationPlan(id1);
        getMigrationAPI().deleteMigrationPlan(id2);
    }

    @Test
    public void getMigrationPlansDescriptorsSorted() throws Exception {
        final long id2 = importMigrationPlan("migrationplan_simple.xml");
        final long id1 = importMigrationPlan("migrationplan.xml");
        List<MigrationPlanDescriptor> migrationPlanDescriptors = getMigrationAPI().getMigrationPlanDescriptors(0, 5,
                MigrationPlanCriterion.SOURCE_PROCESS_NAME_ASC);
        assertEquals(2, migrationPlanDescriptors.size());
        assertEquals(id1, migrationPlanDescriptors.get(0).getId());
        assertEquals(id2, migrationPlanDescriptors.get(1).getId());
        migrationPlanDescriptors = getMigrationAPI().getMigrationPlanDescriptors(0, 5, MigrationPlanCriterion.SOURCE_PROCESS_NAME_DESC);
        assertEquals(2, migrationPlanDescriptors.size());
        assertEquals(id2, migrationPlanDescriptors.get(0).getId());
        assertEquals(id1, migrationPlanDescriptors.get(1).getId());
        getMigrationAPI().deleteMigrationPlan(id1);
        getMigrationAPI().deleteMigrationPlan(id2);
    }

    @Test
    public void exportMigrationPlan() throws Exception {
        final InputStream resourceAsStream = this.getClass().getResourceAsStream("/org/bonitasoft/engine/migration/" + "migrationplan.xml");
        final byte[] content = IOUtil.getAllContentFrom(resourceAsStream);
        final MigrationPlanDescriptor migrationPlanDesciptor = getMigrationAPI().importMigrationPlan(content);
        final long id = migrationPlanDesciptor.getId();
        final byte[] migrationPlan = getMigrationAPI().exportMigrationPlan(id);
        assertEquals(new String(content), new String(migrationPlan));
        getMigrationAPI().deleteMigrationPlan(id);
    }

    @Test
    public void testPrepareForMigrationOneProcess() throws Exception {
        final long migrationPlanId = importMigrationPlan("stepRename.xml");
        final String actorName = "actor";

        final ProcessDefinitionBuilder designProcessDefinition1 = new ProcessDefinitionBuilder().createNewInstance("ProcessToMigrate", "1.0");
        designProcessDefinition1.addActor(actorName);
        designProcessDefinition1.addUserTask("step1", actorName);
        designProcessDefinition1.addUserTask("step2", actorName);
        designProcessDefinition1.addTransition("step1", "step2");
        final ProcessDefinition processDefinition1 = deployAndEnableWithActor(designProcessDefinition1.done(), actorName, john);
        final ProcessDefinitionBuilder designProcessDefinition2 = new ProcessDefinitionBuilder().createNewInstance("MigratedProcess", "1.0");
        designProcessDefinition2.addActor(actorName);
        designProcessDefinition2.addUserTask("step1", actorName);
        designProcessDefinition2.addUserTask("step2_migrated", actorName);
        designProcessDefinition2.addTransition("step1", "step2_migrated");
        final ProcessDefinition processDefinition2 = deployAndEnableWithActor(designProcessDefinition2.done(), actorName, john);

        final ProcessInstance pInstance1 = getProcessAPI().startProcess(processDefinition1.getId());

        final ActivityInstance step1 = waitForUserTask("step1", pInstance1);

        getMigrationAPI().prepareProcessesForMigration(Arrays.asList(pInstance1.getId()), migrationPlanId);
        assignAndExecuteStep(step1, john.getId());
        final WaitForStep waitForStep2 = new WaitForStep(50, 1000, "step2", pInstance1.getId(), TestStates.getReadyState(null));
        // should never reach step2
        assertFalse(waitForStep2.waitUntil());

        // TODO check that process is ready for migration
        // TODO migrate process
        // TODO check the new process
        final List<Breakpoint> breakpoints = getProcessAPI().getBreakpoints(0, 100, BreakpointCriterion.STATE_ID_ASC);
        for (final Breakpoint breakpoint : breakpoints) {
            getProcessAPI().removeBreakpoint(breakpoint.getId());
        }
        disableAndDelete(processDefinition1);
        disableAndDelete(processDefinition2);
        getMigrationAPI().deleteMigrationPlan(migrationPlanId);
    }
}
