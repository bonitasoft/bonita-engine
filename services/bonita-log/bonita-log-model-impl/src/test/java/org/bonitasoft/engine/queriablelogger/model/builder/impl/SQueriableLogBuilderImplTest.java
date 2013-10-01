package org.bonitasoft.engine.queriablelogger.model.builder.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLogSeverity;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Elias Ricken de Medeiros
 */
public class SQueriableLogBuilderImplTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(SQueriableLogBuilderImplTest.class);

    private final SIndexedLogBuilderImpl builder;

    @Rule
    public TestName name = new TestName();

    @Before
    public void setUp() {
        LOGGER.info("Testing : {}", name.getMethodName());
    }

    @After
    public void tearDown() {
        LOGGER.info("Tested: {}", name.getMethodName());
    }

    public SQueriableLogBuilderImplTest() {
        builder = new SIndexedLogBuilderImpl();
    }

    @Test
    public void testSetUsername() {
        builder.createNewInstance().userId("john").severity(SQueriableLogSeverity.BUSINESS);
        builder.actionType("variable_update").actionScope("myVar").rawMessage("successFull").actionStatus(1);
        final SQueriableLog queriableLog = builder.done();
        assertEquals("john", queriableLog.getUserId());
    }

    @Test
    public void testSetClusterNode() {
        builder.createNewInstance().clusterNode("node1").severity(SQueriableLogSeverity.BUSINESS);
        builder.actionType("variable_update").actionScope("myVar").rawMessage("successFull").actionStatus(1);
        final SQueriableLog queriableLog = builder.done();
        assertEquals("node1", queriableLog.getClusterNode());
    }

    @Test
    public void testSetProductVersion() {
        builder.createNewInstance().productVersion("SP-5.4.1").severity(SQueriableLogSeverity.BUSINESS);
        builder.actionType("variable_update").actionScope("myVar").rawMessage("successFull").actionStatus(1);
        final SQueriableLog queriableLog = builder.done();
        assertEquals("SP-5.4.1", queriableLog.getProductVersion());
    }

    @Test
    public void testSetSeverity() {
        builder.createNewInstance().severity(SQueriableLogSeverity.BUSINESS);
        builder.actionType("variable_update").actionScope("myVar").rawMessage("successFull").actionStatus(1);
        final SQueriableLog queriableLog = builder.done();
        assertEquals(SQueriableLogSeverity.BUSINESS, queriableLog.getSeverity());
    }

    @Test
    public void testSetActionType() {
        builder.createNewInstance();
        builder.actionType("excecute_connector").severity(SQueriableLogSeverity.BUSINESS);
        builder.actionScope("myVar").rawMessage("successFull").actionStatus(1);
        final SQueriableLog queriableLog = builder.done();
        assertEquals("excecute_connector", queriableLog.getActionType());
    }

    @Test
    public void testSetActionScope() {
        builder.createNewInstance().actionScope("setVariable").severity(SQueriableLogSeverity.BUSINESS);
        builder.actionType("variable_update").rawMessage("successFull").actionStatus(1);
        final SQueriableLog queriableLog = builder.done();
        assertEquals("setVariable", queriableLog.getActionScope());
    }

    @Test
    public void testSetActionStatus() {
        builder.createNewInstance().actionStatus(1).severity(SQueriableLogSeverity.BUSINESS);
        builder.actionType("variable_update").actionScope("myVar").rawMessage("successFull");
        final SQueriableLog queriableLog = builder.done();
        assertEquals(1, queriableLog.getActionStatus());
    }

    @Test
    public void testSetRawMessage() {
        builder.createNewInstance().rawMessage("successfully executed").severity(SQueriableLogSeverity.BUSINESS);
        builder.actionType("variable_update").actionScope("myVar").actionStatus(1);
        final SQueriableLog queriableLog = builder.done();
        assertEquals("successfully executed", queriableLog.getRawMessage());
    }

    @Test
    public void testSetNumericIndexes() {
        builder.createNewInstance();
        builder.numericIndex(0, 10).rawMessage("successfully executed").severity(SQueriableLogSeverity.BUSINESS);
        builder.actionType("variable_update").actionScope("myVar").actionStatus(1);
        final SQueriableLog queriableLog = builder.done();
        assertEquals(10, queriableLog.getNumericIndex(0));
    }

    @Test
    public void testFromInstance() {
        builder.createNewInstance().userId("john").rawMessage("successfully executed").severity(SQueriableLogSeverity.BUSINESS);
        builder.actionType("variable_update").actionScope("myVar").actionStatus(1);
        final SQueriableLog queriableLog1 = builder.done();
        assertEquals("john", queriableLog1.getUserId());

        builder.fromInstance(queriableLog1).clusterNode("suomi");
        final SQueriableLog queriableLog2 = builder.done();

        // verify if it's the same reference
        assertTrue(queriableLog1 == queriableLog2);
        assertEquals("john", queriableLog2.getUserId());
        assertEquals("suomi", queriableLog2.getClusterNode());
    }

    @Test
    public void testSetCallerClassName() {
        builder.createNewInstance().callerClassName("RecorderImpl.class").rawMessage("successfully executed").severity(SQueriableLogSeverity.BUSINESS);
        builder.actionType("variable_update").actionScope("myVar").actionStatus(1);
        final SQueriableLog queriableLog = builder.done();
        assertEquals("RecorderImpl.class", queriableLog.getCallerClassName());
    }

    @Test
    public void testSetCallerMethodName() {
        builder.createNewInstance().callerMethodName("insertRecord").rawMessage("successfully executed").severity(SQueriableLogSeverity.BUSINESS);
        builder.actionType("variable_update").actionScope("myVar").actionStatus(1);
        final SQueriableLog queriableLog = builder.done();
        assertEquals("insertRecord", queriableLog.getCallerMethodName());
    }

    @Test(expected = MissingMandatoryFieldsException.class)
    public void testFailIfSeverityIsNull() {
        builder.createNewInstance().rawMessage("successfully executed");
        builder.actionType("variable_update").actionScope("myVar").actionStatus(1);
        builder.done();
    }

    @Test(expected = MissingMandatoryFieldsException.class)
    public void testFailIfRawIsNull() {
        builder.createNewInstance().severity(SQueriableLogSeverity.BUSINESS);
        builder.actionType("variable_update").actionScope("myVar").actionStatus(1);
        builder.done();
    }

    @Test(expected = MissingMandatoryFieldsException.class)
    public void testFailIfActionTypeIsNull() {
        builder.createNewInstance().rawMessage("successfully executed").severity(SQueriableLogSeverity.BUSINESS);

        builder.actionScope("myVar").actionStatus(1);
        builder.done();
    }

    @Test(expected = MissingMandatoryFieldsException.class)
    public void testFailIfActionScopeIsNull() {
        builder.createNewInstance().rawMessage("successfully executed").severity(SQueriableLogSeverity.BUSINESS);
        builder.actionType("variable_update").actionStatus(1);
        builder.done();
    }

    @Test(expected = MissingMandatoryFieldsException.class)
    public void testFailIfActionStatusIsNull() {
        builder.createNewInstance().rawMessage("successfully executed").severity(SQueriableLogSeverity.BUSINESS);
        builder.actionType("variable_update").actionScope("myVar");
        builder.done();
    }

    @Test(expected = MissingMandatoryFieldsException.class)
    public void testFailIfActionStatusIsMoreThanOne() {
        builder.createNewInstance().rawMessage("successfully executed").severity(SQueriableLogSeverity.BUSINESS);
        builder.actionType("variable_update").actionScope("myVar").actionStatus(2);
        builder.done();
    }

}
