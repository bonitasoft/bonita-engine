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

import org.bonitasoft.engine.persistence.model.impl.BlobValueImpl;
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
public class SQueriableLogParameterImplTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(SQueriableLogParameterImplTest.class);

    private SQueriableLogParameterImpl parameter;

    @Rule
    public TestName name = new TestName();

    @Before
    public void setUp() {
        LOGGER.info("Testing : {}", name.getMethodName());
        parameter = new SQueriableLogParameterImpl();
    }

    @After
    public void tearDown() {
        LOGGER.info("Tested: {}", name.getMethodName());
        parameter = null;
    }

    @Test
    public void testSetId() {
        assertEquals(-1L, parameter.getId());
        parameter.setId(123L);
        assertEquals(123L, parameter.getId());
    }

    @Test
    public void testSetName() {
        assertEquals(null, parameter.getName());
        parameter.setName("returnValue");
        assertEquals("returnValue", parameter.getName());
    }

    @Test
    public void testSetStringValue() {
        assertEquals(null, parameter.getStringValue());
        parameter.setStringValue("38");
        assertEquals("38", parameter.getStringValue());
    }

    @Test
    public void testSetBlobValue() {
        assertEquals(null, parameter.getBlobValue());
        BlobValueImpl blob = new BlobValueImpl();
        blob.setValue("blobValue");
        parameter.setBlobValue(blob);
        assertEquals("blobValue", parameter.getBlobValue().getValue());
    }

    @Test
    public void testSetValueType() {
        assertEquals(null, parameter.getValueType());
        parameter.setValueType("Integer");
        assertEquals("Integer", parameter.getValueType());
    }

}
