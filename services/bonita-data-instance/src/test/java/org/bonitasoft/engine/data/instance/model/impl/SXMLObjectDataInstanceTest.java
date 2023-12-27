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
package org.bonitasoft.engine.data.instance.model.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.Serializable;

import org.bonitasoft.engine.data.definition.model.SDataDefinition;
import org.bonitasoft.engine.data.instance.model.SXMLObjectDataInstance;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SXMLObjectDataInstanceTest {

    @Mock
    private SDataDefinition dataDefinition;

    @Test
    public void SXMLObjectDataInstanceImpl_should_return_given_value() {
        //given
        final Serializable givenValue = new String("serializable value");

        final SXMLObjectDataInstance sxmlObjectDataInstance = new SXMLObjectDataInstance(dataDefinition);

        //when
        sxmlObjectDataInstance.setValue(givenValue);
        final Serializable returnedValue = sxmlObjectDataInstance.getValue();

        //then
        assertThat(returnedValue).as("should be equal to given value").isEqualTo(givenValue);
    }

    @Test
    public void SXMLObjectDataInstanceImpl_should_return_null_when_no_given_value() {
        //given
        final SXMLObjectDataInstance sxmlObjectDataInstance = new SXMLObjectDataInstance(dataDefinition);

        //when
        final Serializable returnedValue = sxmlObjectDataInstance.getValue();

        //then
        assertThat(returnedValue).as("should be null").isNull();
    }
}
