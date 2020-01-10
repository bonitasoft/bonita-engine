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
package org.bonitasoft.engine.data.instance.model.builder.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.bonitasoft.engine.data.definition.model.SDataDefinition;
import org.bonitasoft.engine.data.definition.model.STextDataDefinition;
import org.bonitasoft.engine.data.definition.model.SXMLDataDefinition;
import org.bonitasoft.engine.data.instance.model.SBlobDataInstance;
import org.bonitasoft.engine.data.instance.model.SBooleanDataInstance;
import org.bonitasoft.engine.data.instance.model.SDataInstance;
import org.bonitasoft.engine.data.instance.model.SDataInstanceBuilder;
import org.bonitasoft.engine.data.instance.model.SDateDataInstance;
import org.bonitasoft.engine.data.instance.model.SDoubleDataInstance;
import org.bonitasoft.engine.data.instance.model.SFloatDataInstance;
import org.bonitasoft.engine.data.instance.model.SIntegerDataInstance;
import org.bonitasoft.engine.data.instance.model.SLongDataInstance;
import org.bonitasoft.engine.data.instance.model.SLongTextDataInstance;
import org.bonitasoft.engine.data.instance.model.SShortTextDataInstance;
import org.bonitasoft.engine.data.instance.model.SXMLDataInstance;
import org.bonitasoft.engine.data.instance.model.SXMLObjectDataInstance;
import org.bonitasoft.engine.data.instance.model.exceptions.SDataInstanceNotWellFormedException;
import org.junit.Test;

public class SDataInstanceBuilderFactoryImplTest {

    @Test
    public void createNewInstanceHandlesDateType() throws Exception {
        testSpecificDataType(Date.class.getName(), SDateDataInstance.class);
    }

    @Test
    public void createNewInstanceHandlesIntegerType() throws Exception {
        testSpecificDataType(Integer.class.getName(), SIntegerDataInstance.class);
    }

    @Test
    public void createNewInstanceHandlesLongType() throws Exception {
        testSpecificDataType(Long.class.getName(), SLongDataInstance.class);
    }

    @Test
    public void createNewInstanceHandlesStringType() throws Exception {
        testSpecificDataType(String.class.getName(), SShortTextDataInstance.class);
    }

    @Test
    public void createNewInstanceHandlesDoubleType() throws Exception {
        testSpecificDataType(Double.class.getName(), SDoubleDataInstance.class);
    }

    @Test
    public void createNewInstanceHandlesFloatType() throws Exception {
        testSpecificDataType(Float.class.getName(), SFloatDataInstance.class);
    }

    @Test
    public void createNewInstanceHandlesByteArrayAsBLOBType() throws Exception {
        testSpecificDataType(byte[].class.getName(), SBlobDataInstance.class);
    }

    @Test
    public void createNewInstanceHandlesBooleanType() throws Exception {
        testSpecificDataType(Boolean.class.getName(), SBooleanDataInstance.class);
    }

    @Test
    public void createNewInstanceHandlesCustomTypeAsXMLObject() throws Exception {
        testSpecificDataType(Object.class.getName(), SXMLObjectDataInstance.class);
    }

    private void testSpecificDataType(final String dataTypeName, final Class<? extends SDataInstance> dataInstanceClass)
            throws SDataInstanceNotWellFormedException {
        final SDataDefinition dataDefinition = mock(SDataDefinition.class);
        when(dataDefinition.getClassName()).thenReturn(dataTypeName);

        final SDataInstanceBuilder newInstance = SDataInstanceBuilder.createNewInstance(dataDefinition);

        assertThat(newInstance.done()).isInstanceOf(dataInstanceClass);
    }

    @Test
    public void createNewInstanceHandlesXMLType() throws Exception {
        assertThat(SDataInstanceBuilder.createNewInstance(mock(SXMLDataDefinition.class)).done())
                .isInstanceOf(SXMLDataInstance.class);
    }

    @Test
    public void createNewInstanceHandlesShortTextType() throws Exception {
        assertThat(SDataInstanceBuilder.createNewInstance(mock(STextDataDefinition.class)).done()).isInstanceOf(
                SShortTextDataInstance.class);
    }

    @Test
    public void createNewInstanceHandlesLongTextType() throws Exception {
        final STextDataDefinition dataDef = mock(STextDataDefinition.class);
        when(dataDef.isLongText()).thenReturn(true);
        assertThat(SDataInstanceBuilder.createNewInstance(dataDef).done()).isInstanceOf(SLongTextDataInstance.class);
    }
}
