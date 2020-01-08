/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.data.instance.model.archive.impl;

import static org.junit.Assert.assertEquals;

import org.bonitasoft.engine.data.instance.model.SXMLObjectDataInstance;
import org.bonitasoft.engine.data.instance.model.archive.SAXMLObjectDataInstance;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Celine Souchet
 */
public class SAXMLObjectDataInstanceTest {

    private SXMLObjectDataInstance sDataInstance;

    @Before
    public void setUp() {
        sDataInstance = new SXMLObjectDataInstance();
        sDataInstance.setValue(1);
        sDataInstance.setClassName(Long.class.getName());
        sDataInstance.setContainerId(2);
        sDataInstance.setContainerType("containerType");
        sDataInstance.setDataTypeClassName(String.class.getName());
        sDataInstance.setDescription("description");
        sDataInstance.setId(3);
        sDataInstance.setName("name");
        sDataInstance.setTenantId(4);
        sDataInstance.setTransientData(false);
        sDataInstance.setValue("value");
    }

    /**
     * Test method for
     * {@link SAXMLObjectDataInstance#SAXMLObjectDataInstance(org.bonitasoft.engine.data.instance.model.SDataInstance)}
     * .
     */
    @Test
    public final void sAXMLObjectDataInstanceImplSDataInstance() {
        final SAXMLObjectDataInstance saxmlObjectDataInstance = new SAXMLObjectDataInstance(sDataInstance);
        assertEquals(sDataInstance.getClassName(), saxmlObjectDataInstance.getClassName());
        assertEquals(sDataInstance.getContainerId(), saxmlObjectDataInstance.getContainerId());
        assertEquals(sDataInstance.getContainerType(), saxmlObjectDataInstance.getContainerType());
        assertEquals(sDataInstance.getDescription(), saxmlObjectDataInstance.getDescription());
        assertEquals(sDataInstance.getId(), saxmlObjectDataInstance.getSourceObjectId());
        assertEquals(sDataInstance.getName(), saxmlObjectDataInstance.getName());
        assertEquals(sDataInstance.getValue(), saxmlObjectDataInstance.getValue());
    }

    /**
     * Test method for {@link SAXMLObjectDataInstance#getValue()}.
     * Test method for {@link SAXMLObjectDataInstance#setValue(java.io.Serializable)}.
     */
    @Test
    public final void getSetValue() {
        final SAXMLObjectDataInstance saxmlObjectDataInstance = new SAXMLObjectDataInstance();
        saxmlObjectDataInstance.setValue("plop");
        assertEquals("plop", saxmlObjectDataInstance.getValue());
    }

    /**
     * Test method for {@link SAXMLObjectDataInstance#getValue()}.
     * Test method for {@link SAXMLObjectDataInstance#setValue(java.io.Serializable)}.
     */
    @Test
    public final void getValueShouldBeNull() {
        final SAXMLObjectDataInstance saxmlObjectDataInstance = new SAXMLObjectDataInstance();
        assertEquals(null, saxmlObjectDataInstance.getValue());
    }

    @Test
    public final void getSetTenantId() {
        final SAXMLObjectDataInstance saxmlObjectDataInstance = new SAXMLObjectDataInstance();
        saxmlObjectDataInstance.setTenantId(69);
        assertEquals(69, saxmlObjectDataInstance.getTenantId());
    }

    @Test
    public final void getSetId() {
        final SAXMLObjectDataInstance saxmlObjectDataInstance = new SAXMLObjectDataInstance();
        saxmlObjectDataInstance.setId(64);
        assertEquals(64, saxmlObjectDataInstance.getId());
    }

    @Test
    public final void getSetName() {
        final SAXMLObjectDataInstance saxmlObjectDataInstance = new SAXMLObjectDataInstance();
        saxmlObjectDataInstance.setName("plop5");
        assertEquals("plop5", saxmlObjectDataInstance.getName());
    }

    @Test
    public final void getDescription() {
        final SAXMLObjectDataInstance saxmlObjectDataInstance = new SAXMLObjectDataInstance();
        saxmlObjectDataInstance.setDescription("plop8");
        assertEquals("plop8", saxmlObjectDataInstance.getDescription());
    }

    @Test
    public final void isSetTransientData() {
        final SAXMLObjectDataInstance saxmlObjectDataInstance = new SAXMLObjectDataInstance();
        saxmlObjectDataInstance.setTransientData(true);
        assertEquals(true, saxmlObjectDataInstance.isTransientData());
    }

    @Test
    public final void getSetClassName() {
        final SAXMLObjectDataInstance saxmlObjectDataInstance = new SAXMLObjectDataInstance();
        saxmlObjectDataInstance.setClassName("plipou");
        assertEquals("plipou", saxmlObjectDataInstance.getClassName());
    }

    @Test
    public final void getSetContainerId() {
        final SAXMLObjectDataInstance saxmlObjectDataInstance = new SAXMLObjectDataInstance();
        saxmlObjectDataInstance.setContainerId(14);
        assertEquals(14, saxmlObjectDataInstance.getContainerId());
    }

    @Test
    public final void getSetContainerType() {
        final SAXMLObjectDataInstance saxmlObjectDataInstance = new SAXMLObjectDataInstance();
        saxmlObjectDataInstance.setContainerType("plipou95");
        assertEquals("plipou95", saxmlObjectDataInstance.getContainerType());
    }

    @Test
    public final void getArchiveDate() {
        final SAXMLObjectDataInstance saxmlObjectDataInstance = new SAXMLObjectDataInstance();
        saxmlObjectDataInstance.setArchiveDate(83);
        assertEquals(83, saxmlObjectDataInstance.getArchiveDate());
    }

    @Test
    public final void getSetSourceObjectId() {
        final SAXMLObjectDataInstance saxmlObjectDataInstance = new SAXMLObjectDataInstance();
        saxmlObjectDataInstance.setSourceObjectId(85);
        assertEquals(85, saxmlObjectDataInstance.getSourceObjectId());
    }

}
