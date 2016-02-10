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
 **/
package org.bonitasoft.engine.queriablelogger.model.builder.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLogSeverity;
import org.bonitasoft.engine.queriablelogger.model.builder.SQueriableLogBuilder;
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

    private final SQueriableLogBuilderFactoryImpl fact;

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
        fact = new SQueriableLogBuilderFactoryImpl();
    }

    @Test
    public void testSetUsername() {
        final SQueriableLogBuilder builder = fact.createNewInstance();
        builder.userId("john").severity(SQueriableLogSeverity.BUSINESS);
        builder.actionType("variable_update").actionScope("myVar").rawMessage("successFull").actionStatus(1);
        final SQueriableLog queriableLog = builder.done();
        assertEquals("john", queriableLog.getUserId());
    }

    @Test
    public void testSetClusterNode() {
        final SQueriableLogBuilder builder = fact.createNewInstance();
        builder.clusterNode("node1").severity(SQueriableLogSeverity.BUSINESS);
        builder.actionType("variable_update").actionScope("myVar").rawMessage("successFull").actionStatus(1);
        final SQueriableLog queriableLog = builder.done();
        assertEquals("node1", queriableLog.getClusterNode());
    }

    @Test
    public void testSetProductVersion() {
        final SQueriableLogBuilder builder = fact.createNewInstance();
        builder.productVersion("SP-5.4.1").severity(SQueriableLogSeverity.BUSINESS);
        builder.actionType("variable_update").actionScope("myVar").rawMessage("successFull").actionStatus(1);
        final SQueriableLog queriableLog = builder.done();
        assertEquals("SP-5.4.1", queriableLog.getProductVersion());
    }

    @Test
    public void testSetSeverity() {
        final SQueriableLogBuilder builder = fact.createNewInstance();
        builder.severity(SQueriableLogSeverity.BUSINESS);
        builder.actionType("variable_update").actionScope("myVar").rawMessage("successFull").actionStatus(1);
        final SQueriableLog queriableLog = builder.done();
        assertEquals(SQueriableLogSeverity.BUSINESS, queriableLog.getSeverity());
    }

    @Test
    public void testSetActionType() {
        final SQueriableLogBuilder builder = fact.createNewInstance();
        builder.actionType("excecute_connector").severity(SQueriableLogSeverity.BUSINESS);
        builder.actionScope("myVar").rawMessage("successFull").actionStatus(1);
        final SQueriableLog queriableLog = builder.done();
        assertEquals("excecute_connector", queriableLog.getActionType());
    }

    @Test
    public void testSetActionScope() {
        final SQueriableLogBuilder builder = fact.createNewInstance();
        builder.actionScope("setVariable").severity(SQueriableLogSeverity.BUSINESS);
        builder.actionType("variable_update").rawMessage("successFull").actionStatus(1);
        final SQueriableLog queriableLog = builder.done();
        assertEquals("setVariable", queriableLog.getActionScope());
    }

    @Test
    public void testSetActionStatus() {
        final SQueriableLogBuilder builder = fact.createNewInstance();
        builder.actionStatus(1).severity(SQueriableLogSeverity.BUSINESS);
        builder.actionType("variable_update").actionScope("myVar").rawMessage("successFull");
        final SQueriableLog queriableLog = builder.done();
        assertEquals(1, queriableLog.getActionStatus());
    }

    @Test
    public void testSetRawMessage() {
        final SQueriableLogBuilder builder = fact.createNewInstance();
        builder.rawMessage("successfully executed").severity(SQueriableLogSeverity.BUSINESS);
        builder.actionType("variable_update").actionScope("myVar").actionStatus(1);
        final SQueriableLog queriableLog = builder.done();
        assertEquals("successfully executed", queriableLog.getRawMessage());
    }

    @Test
    public void testSetNumericIndexes() {
        final SQueriableLogBuilder builder = fact.createNewInstance();
        builder.numericIndex(0, 10).rawMessage("successfully executed").severity(SQueriableLogSeverity.BUSINESS);
        builder.actionType("variable_update").actionScope("myVar").actionStatus(1);
        final SQueriableLog queriableLog = builder.done();
        assertEquals(10, queriableLog.getNumericIndex(0));
    }

    @Test
    public void testFromInstance() {
        SQueriableLogBuilder builder = fact.createNewInstance();builder.userId("john").rawMessage("successfully executed").severity(SQueriableLogSeverity.BUSINESS);
        builder.actionType("variable_update").actionScope("myVar").actionStatus(1);
        final SQueriableLog queriableLog1 = builder.done();
        assertEquals("john", queriableLog1.getUserId());

        builder = fact.fromInstance(queriableLog1);
        builder.clusterNode("suomi");
        final SQueriableLog queriableLog2 = builder.done();

        // verify if it's the same reference
        assertTrue(queriableLog1 == queriableLog2);
        assertEquals("john", queriableLog2.getUserId());
        assertEquals("suomi", queriableLog2.getClusterNode());
    }

    @Test
    public void testSetCallerClassName() {
        final SQueriableLogBuilder builder = fact.createNewInstance();
        builder.callerClassName("RecorderImpl.class").rawMessage("successfully executed").severity(SQueriableLogSeverity.BUSINESS);
        builder.actionType("variable_update").actionScope("myVar").actionStatus(1);
        final SQueriableLog queriableLog = builder.done();
        assertEquals("RecorderImpl.class", queriableLog.getCallerClassName());
    }

    @Test
    public void testSetCallerMethodName() {
        final SQueriableLogBuilder builder = fact.createNewInstance();
        builder.callerMethodName("insertRecord").rawMessage("successfully executed").severity(SQueriableLogSeverity.BUSINESS);
        builder.actionType("variable_update").actionScope("myVar").actionStatus(1);
        final SQueriableLog queriableLog = builder.done();
        assertEquals("insertRecord", queriableLog.getCallerMethodName());
    }

    @Test(expected = MissingMandatoryFieldsException.class)
    public void testFailIfSeverityIsNull() {
        final SQueriableLogBuilder builder = fact.createNewInstance();
        builder.rawMessage("successfully executed");
        builder.actionType("variable_update").actionScope("myVar").actionStatus(1);
        builder.done();
    }

    @Test(expected = MissingMandatoryFieldsException.class)
    public void testFailIfRawIsNull() {
        final SQueriableLogBuilder builder = fact.createNewInstance();
        builder.severity(SQueriableLogSeverity.BUSINESS);
        builder.actionType("variable_update").actionScope("myVar").actionStatus(1);
        builder.done();
    }

    @Test(expected = MissingMandatoryFieldsException.class)
    public void testFailIfActionTypeIsNull() {
        final SQueriableLogBuilder builder = fact.createNewInstance();
        builder.rawMessage("successfully executed").severity(SQueriableLogSeverity.BUSINESS);

        builder.actionScope("myVar").actionStatus(1);
        builder.done();
    }

    @Test(expected = MissingMandatoryFieldsException.class)
    public void testFailIfActionScopeIsNull() {
        final SQueriableLogBuilder builder = fact.createNewInstance();
        builder.rawMessage("successfully executed").severity(SQueriableLogSeverity.BUSINESS);
        builder.actionType("variable_update").actionStatus(1);
        builder.done();
    }

    @Test(expected = MissingMandatoryFieldsException.class)
    public void testFailIfActionStatusIsNull() {
        final SQueriableLogBuilder builder = fact.createNewInstance();
        builder.rawMessage("successfully executed").severity(SQueriableLogSeverity.BUSINESS);
        builder.actionType("variable_update").actionScope("myVar");
        builder.done();
    }

    @Test(expected = MissingMandatoryFieldsException.class)
    public void testFailIfActionStatusIsMoreThanOne() {
        final SQueriableLogBuilder builder = fact.createNewInstance();
        builder.rawMessage("successfully executed").severity(SQueriableLogSeverity.BUSINESS);
        builder.actionType("variable_update").actionScope("myVar").actionStatus(2);
        builder.done();
    }

}
