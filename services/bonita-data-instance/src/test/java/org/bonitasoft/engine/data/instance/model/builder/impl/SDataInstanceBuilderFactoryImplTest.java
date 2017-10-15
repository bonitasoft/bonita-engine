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
package org.bonitasoft.engine.data.instance.model.builder.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.bonitasoft.engine.data.definition.model.SDataDefinition;
import org.bonitasoft.engine.data.definition.model.STextDataDefinition;
import org.bonitasoft.engine.data.definition.model.SXMLDataDefinition;
import org.bonitasoft.engine.data.instance.model.SXMLDataInstance;
import org.bonitasoft.engine.data.instance.model.builder.SDataInstanceBuilder;
import org.bonitasoft.engine.data.instance.model.exceptions.SDataInstanceNotWellFormedException;
import org.bonitasoft.engine.data.instance.model.impl.SBlobDataInstanceImpl;
import org.bonitasoft.engine.data.instance.model.impl.SBooleanDataInstanceImpl;
import org.bonitasoft.engine.data.instance.model.impl.SDataInstanceImpl;
import org.bonitasoft.engine.data.instance.model.impl.SDateDataInstanceImpl;
import org.bonitasoft.engine.data.instance.model.impl.SDoubleDataInstanceImpl;
import org.bonitasoft.engine.data.instance.model.impl.SFloatDataInstanceImpl;
import org.bonitasoft.engine.data.instance.model.impl.SIntegerDataInstanceImpl;
import org.bonitasoft.engine.data.instance.model.impl.SLongDataInstanceImpl;
import org.bonitasoft.engine.data.instance.model.impl.SLongTextDataInstanceImpl;
import org.bonitasoft.engine.data.instance.model.impl.SShortTextDataInstanceImpl;
import org.bonitasoft.engine.data.instance.model.impl.SXMLObjectDataInstanceImpl;
import org.junit.Test;

public class SDataInstanceBuilderFactoryImplTest {

    @Test
    public void createNewInstanceHandlesDateType() throws Exception {
        testSpecificDataType(Date.class.getName(), SDateDataInstanceImpl.class);
    }

    @Test
    public void createNewInstanceHandlesIntegerType() throws Exception {
        testSpecificDataType(Integer.class.getName(), SIntegerDataInstanceImpl.class);
    }

    @Test
    public void createNewInstanceHandlesLongType() throws Exception {
        testSpecificDataType(Long.class.getName(), SLongDataInstanceImpl.class);
    }

    @Test
    public void createNewInstanceHandlesStringType() throws Exception {
        testSpecificDataType(String.class.getName(), SShortTextDataInstanceImpl.class);
    }

    @Test
    public void createNewInstanceHandlesDoubleType() throws Exception {
        testSpecificDataType(Double.class.getName(), SDoubleDataInstanceImpl.class);
    }

    @Test
    public void createNewInstanceHandlesFloatType() throws Exception {
        testSpecificDataType(Float.class.getName(), SFloatDataInstanceImpl.class);
    }

    @Test
    public void createNewInstanceHandlesByteArrayAsBLOBType() throws Exception {
        testSpecificDataType(byte[].class.getName(), SBlobDataInstanceImpl.class);
    }

    @Test
    public void createNewInstanceHandlesBooleanType() throws Exception {
        testSpecificDataType(Boolean.class.getName(), SBooleanDataInstanceImpl.class);
    }

    @Test
    public void createNewInstanceHandlesCustomTypeAsXMLObject() throws Exception {
        testSpecificDataType(Object.class.getName(), SXMLObjectDataInstanceImpl.class);
    }

    private void testSpecificDataType(final String dataTypeName, final Class<? extends SDataInstanceImpl> dataInstanceClass)
            throws SDataInstanceNotWellFormedException {
        final SDataDefinition dataDefinition = mock(SDataDefinition.class);
        when(dataDefinition.getClassName()).thenReturn(dataTypeName);

        final SDataInstanceBuilder newInstance = new SDataInstanceBuilderFactoryImpl().createNewInstance(dataDefinition);

        assertThat(newInstance.done()).isInstanceOf(dataInstanceClass);
    }

    @Test
    public void createNewInstanceHandlesXMLType() throws Exception {
        assertThat(new SDataInstanceBuilderFactoryImpl().createNewInstance(mock(SXMLDataDefinition.class)).done()).isInstanceOf(SXMLDataInstance.class);
    }

    @Test
    public void createNewInstanceHandlesShortTextType() throws Exception {
        assertThat(new SDataInstanceBuilderFactoryImpl().createNewInstance(mock(STextDataDefinition.class)).done()).isInstanceOf(
                SShortTextDataInstanceImpl.class);
    }

    @Test
    public void createNewInstanceHandlesLongTextType() throws Exception {
        final STextDataDefinition dataDef = mock(STextDataDefinition.class);
        when(dataDef.isLongText()).thenReturn(true);
        assertThat(new SDataInstanceBuilderFactoryImpl().createNewInstance(dataDef).done()).isInstanceOf(SLongTextDataInstanceImpl.class);
    }
}
