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
package org.bonitasoft.engine.data.instance.model.archive.impl;

import static org.junit.Assert.assertEquals;

import org.bonitasoft.engine.data.instance.model.impl.SXMLObjectDataInstanceImpl;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Celine Souchet
 */
public class SAXMLObjectDataInstanceImplTest {

    private SXMLObjectDataInstanceImpl sDataInstance;

    @Before
    public void setUp() {
        sDataInstance = new SXMLObjectDataInstanceImpl();
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
     * Test method for {@link SAXMLObjectDataInstanceImpl#SAXMLObjectDataInstanceImpl(org.bonitasoft.engine.data.instance.model.SDataInstance)} .
     */
    @Test
    public final void sAXMLObjectDataInstanceImplSDataInstance() {
        final SAXMLObjectDataInstanceImpl saxmlObjectDataInstanceImpl = new SAXMLObjectDataInstanceImpl(sDataInstance);
        assertEquals(sDataInstance.getClassName(), saxmlObjectDataInstanceImpl.getClassName());
        assertEquals(sDataInstance.getContainerId(), saxmlObjectDataInstanceImpl.getContainerId());
        assertEquals(sDataInstance.getContainerType(), saxmlObjectDataInstanceImpl.getContainerType());
        assertEquals(sDataInstance.getDescription(), saxmlObjectDataInstanceImpl.getDescription());
        assertEquals(sDataInstance.getId(), saxmlObjectDataInstanceImpl.getSourceObjectId());
        assertEquals(sDataInstance.getName(), saxmlObjectDataInstanceImpl.getName());
        assertEquals(sDataInstance.getValue(), saxmlObjectDataInstanceImpl.getValue());
        assertEquals(SAXMLObjectDataInstanceImpl.class.getSimpleName(), saxmlObjectDataInstanceImpl.getDiscriminator());
    }

    /**
     * Test method for {@link SAXMLObjectDataInstanceImpl#getValue()}.
     * Test method for {@link SAXMLObjectDataInstanceImpl#setValue(java.io.Serializable)}.
     */
    @Test
    public final void getSetValue() {
        final SAXMLObjectDataInstanceImpl saxmlObjectDataInstanceImpl = new SAXMLObjectDataInstanceImpl();
        saxmlObjectDataInstanceImpl.setValue("plop");
        assertEquals("plop", saxmlObjectDataInstanceImpl.getValue());
    }

    /**
     * Test method for {@link SAXMLObjectDataInstanceImpl#getValue()}.
     * Test method for {@link SAXMLObjectDataInstanceImpl#setValue(java.io.Serializable)}.
     */
    @Test
    public final void getValueShouldBeNull() {
        final SAXMLObjectDataInstanceImpl saxmlObjectDataInstanceImpl = new SAXMLObjectDataInstanceImpl();
        assertEquals(null, saxmlObjectDataInstanceImpl.getValue());
    }

    /**
     * Test method for {@link SAXMLObjectDataInstanceImpl#getDiscriminator()}.
     */
    @Test
    public final void getDiscriminator() {
        final SAXMLObjectDataInstanceImpl saxmlObjectDataInstanceImpl = new SAXMLObjectDataInstanceImpl();
        assertEquals(SAXMLObjectDataInstanceImpl.class.getSimpleName(), saxmlObjectDataInstanceImpl.getDiscriminator());
    }

    /**
     * Test method for {@link SADataInstanceImpl#getTenantId()}.
     * Test method for {@link SADataInstanceImpl#setTenantId(long)}.
     */
    @Test
    public final void getSetTenantId() {
        final SAXMLObjectDataInstanceImpl saxmlObjectDataInstanceImpl = new SAXMLObjectDataInstanceImpl();
        saxmlObjectDataInstanceImpl.setTenantId(69);
        assertEquals(69, saxmlObjectDataInstanceImpl.getTenantId());
    }

    /**
     * Test method for {@link SADataInstanceImpl#getId()}.
     * Test method for {@link SADataInstanceImpl#setId(long)}.
     */
    @Test
    public final void getSetId() {
        final SAXMLObjectDataInstanceImpl saxmlObjectDataInstanceImpl = new SAXMLObjectDataInstanceImpl();
        saxmlObjectDataInstanceImpl.setId(64);
        assertEquals(64, saxmlObjectDataInstanceImpl.getId());
    }

    /**
     * Test method for {@link SADataInstanceImpl#getName()}.
     * Test method for {@link SADataInstanceImpl#setName(String)}.
     */
    @Test
    public final void getSetName() {
        final SAXMLObjectDataInstanceImpl saxmlObjectDataInstanceImpl = new SAXMLObjectDataInstanceImpl();
        saxmlObjectDataInstanceImpl.setName("plop5");
        assertEquals("plop5", saxmlObjectDataInstanceImpl.getName());
    }

    /**
     * Test method for {@link SADataInstanceImpl#getDescription()}.
     * Test method for {@link SADataInstanceImpl#setDescription(String)}.
     */
    @Test
    public final void getDescription() {
        final SAXMLObjectDataInstanceImpl saxmlObjectDataInstanceImpl = new SAXMLObjectDataInstanceImpl();
        saxmlObjectDataInstanceImpl.setDescription("plop8");
        assertEquals("plop8", saxmlObjectDataInstanceImpl.getDescription());
    }

    /**
     * Test method for {@link SADataInstanceImpl#isTransientData()}.
     * Test method for {@link SADataInstanceImpl#setTransientData(boolean)}.
     */
    @Test
    public final void isSetTransientData() {
        final SAXMLObjectDataInstanceImpl saxmlObjectDataInstanceImpl = new SAXMLObjectDataInstanceImpl();
        saxmlObjectDataInstanceImpl.setTransientData(true);
        assertEquals(true, saxmlObjectDataInstanceImpl.isTransientData());
    }

    /**
     * Test method for {@link SADataInstanceImpl#getClassName()}.
     * Test method for {@link SADataInstanceImpl#setClassName(String)}.
     */
    @Test
    public final void getSetClassName() {
        final SAXMLObjectDataInstanceImpl saxmlObjectDataInstanceImpl = new SAXMLObjectDataInstanceImpl();
        saxmlObjectDataInstanceImpl.setClassName("plipou");
        assertEquals("plipou", saxmlObjectDataInstanceImpl.getClassName());
    }

    /**
     * Test method for {@link SADataInstanceImpl#getContainerId()}.
     * Test method for {@link SADataInstanceImpl#setContainerId(long)}.
     */
    @Test
    public final void getSetContainerId() {
        final SAXMLObjectDataInstanceImpl saxmlObjectDataInstanceImpl = new SAXMLObjectDataInstanceImpl();
        saxmlObjectDataInstanceImpl.setContainerId(14);
        assertEquals(14, saxmlObjectDataInstanceImpl.getContainerId());
    }

    /**
     * Test method for {@link SADataInstanceImpl#getContainerType()}.
     * Test method for {@link SADataInstanceImpl#setContainerType(String)}.
     */
    @Test
    public final void getSetContainerType() {
        final SAXMLObjectDataInstanceImpl saxmlObjectDataInstanceImpl = new SAXMLObjectDataInstanceImpl();
        saxmlObjectDataInstanceImpl.setContainerType("plipou95");
        assertEquals("plipou95", saxmlObjectDataInstanceImpl.getContainerType());
    }

    /**
     * Test method for {@link SADataInstanceImpl#getArchiveDate()}.
     * Test method for {@link SADataInstanceImpl#setArchiveDate(long)}.
     */
    @Test
    public final void getArchiveDate() {
        final SAXMLObjectDataInstanceImpl saxmlObjectDataInstanceImpl = new SAXMLObjectDataInstanceImpl();
        saxmlObjectDataInstanceImpl.setArchiveDate(83);
        assertEquals(83, saxmlObjectDataInstanceImpl.getArchiveDate());
    }

    /**
     * Test method for {@link SADataInstanceImpl#getSourceObjectId()}.
     * Test method for {@link SADataInstanceImpl#setSourceObjectId(long)}.
     */
    @Test
    public final void getSetSourceObjectId() {
        final SAXMLObjectDataInstanceImpl saxmlObjectDataInstanceImpl = new SAXMLObjectDataInstanceImpl();
        saxmlObjectDataInstanceImpl.setSourceObjectId(85);
        assertEquals(85, saxmlObjectDataInstanceImpl.getSourceObjectId());
    }

}
