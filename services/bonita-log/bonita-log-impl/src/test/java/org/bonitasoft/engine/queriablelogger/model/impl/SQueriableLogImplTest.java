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
package org.bonitasoft.engine.queriablelogger.model.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
public class SQueriableLogImplTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(SQueriableLogImplTest.class);

    private SQueriableLogImpl queriableLog;

    @Rule
    public TestName name = new TestName();

    @Before
    public void setUp() {
        LOGGER.info("Testing : {}", name.getMethodName());
        queriableLog = new SQueriableLogImpl();
    }

    @After
    public void tearDown() {
        LOGGER.info("Tested: {}", name.getMethodName());
        queriableLog = null;
    }

    @Test
    public void testSetID() {
        assertEquals(0, queriableLog.getId());
        queriableLog.setId(123L);
        assertEquals(123L, queriableLog.getId());
    }

    @Test
    public void testSetDateElements() {
        assertTrue(queriableLog.getYear() > 0);
        assertTrue(queriableLog.getMonth() > 0);
        assertTrue(queriableLog.getDayOfYear() > 0);
        assertTrue(queriableLog.getWeekOfYear() > 0);
    }

    @Test
    public void testSetUsername() {
        assertEquals(null, queriableLog.getUserId());
        queriableLog.setUserId("john");
        assertEquals("john", queriableLog.getUserId());
    }

    @Test
    public void testSetThreadID() {
        assertEquals(Thread.currentThread().getId(), queriableLog.getThreadNumber());
    }

    @Test
    public void testSetClusterNode() {
        assertEquals(null, queriableLog.getClusterNode());
        queriableLog.setClusterNode("node1");
        assertEquals("node1", queriableLog.getClusterNode());
    }

    @Test
    public void testSetProductVersion() {
        assertEquals(null, queriableLog.getProductVersion());
        queriableLog.setProductVersion("SP-5.4.1");
        assertEquals("SP-5.4.1", queriableLog.getProductVersion());
    }

    @Test
    public void testSetSeverity() {
        assertEquals(null, queriableLog.getSeverity());
        queriableLog.setSeverity(SQueriableLogSeverity.BUSINESS);
        assertEquals(SQueriableLogSeverity.BUSINESS, queriableLog.getSeverity());
    }

    @Test
    public void testSetActionType() {
        assertEquals(null, queriableLog.getActionType());
        queriableLog.setActionType("excecute_connector");
        assertEquals("excecute_connector", queriableLog.getActionType());
    }

    @Test
    public void testSetActionScope() {
        assertEquals(null, queriableLog.getActionScope());
        queriableLog.setActionScope("setVariable");
        assertEquals("setVariable", queriableLog.getActionScope());
    }

    @Test
    public void testSetActionStatus() {
        assertEquals(-1, queriableLog.getActionStatus());
        queriableLog.setActionStatus(1);
        assertEquals(1, queriableLog.getActionStatus());
    }

    @Test
    public void testSetRawMessage() {
        assertEquals(null, queriableLog.getRawMessage());
        queriableLog.setRawMessage("successfully executed");
        assertEquals("successfully executed", queriableLog.getRawMessage());
    }

    @Test
    public void testSetNumericIndexes() {
        final int numberOfNumericIndexes = 5;
        for (int i = 0; i < numberOfNumericIndexes; i++) {
            assertEquals(-1L, queriableLog.getNumericIndex(i));
        }

        for (int i = 0; i < numberOfNumericIndexes; i++) {
            queriableLog.setNumericIndex(i, i);
            assertEquals(i, queriableLog.getNumericIndex(i));
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testSetNumericIndexOutOfRange() {
        queriableLog.setNumericIndex(10, 10);
    }

    @Test(expected = IllegalStateException.class)
    public void getNumericIndexOutOfRange() {
        queriableLog.getNumericIndex(10);
    }

    @Test
    public void testSetCallerClassName() {
        assertNull(queriableLog.getCallerClassName());
        queriableLog.setCallerClassName("RecorderImpl.class");
        assertEquals("RecorderImpl.class", queriableLog.getCallerClassName());
    }

    @Test
    public void testSetCallerMethodName() {
        assertNull(queriableLog.getCallerMethodName());
        queriableLog.setCallerMethodName("insertRecord");
        assertEquals("insertRecord", queriableLog.getCallerMethodName());
    }

}
