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
package org.bonitasoft.engine.data.instance.model.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.Serializable;

import org.bonitasoft.engine.data.definition.model.SDataDefinition;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SXMLObjectDataInstanceImplTest {

    @Mock
    private SDataDefinition dataDefinition;

    @Test
    public void SXMLObjectDataInstanceImpl_should_return_given_value() {
        //given
        final Serializable givenValue = new String("serializable value");

        final SXMLObjectDataInstanceImpl sxmlObjectDataInstanceImpl = new SXMLObjectDataInstanceImpl(dataDefinition);

        //when
        sxmlObjectDataInstanceImpl.setValue(givenValue);
        final Serializable returnedValue = sxmlObjectDataInstanceImpl.getValue();

        //then
        assertThat(returnedValue).as("should be equal to given value").isEqualTo(givenValue);
    }

    @Test
    public void SXMLObjectDataInstanceImpl_should_return_null_when_no_given_value() {
        //given
        final SXMLObjectDataInstanceImpl sxmlObjectDataInstanceImpl = new SXMLObjectDataInstanceImpl(dataDefinition);

        //when
        final Serializable returnedValue = sxmlObjectDataInstanceImpl.getValue();

        //then
        assertThat(returnedValue).as("should be null").isNull();
    }

    @Test
    public void discriminator_should_not_be_null() {
        //given
        final SXMLObjectDataInstanceImpl sxmlObjectDataInstanceImpl = new SXMLObjectDataInstanceImpl();

        //when
        final String returnedValue = sxmlObjectDataInstanceImpl.getDiscriminator();

        //then
        assertThat(returnedValue).as("should not be null").isNotEmpty();
    }
}
