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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.Serializable;

import org.bonitasoft.engine.data.instance.model.SDataInstance;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SAXMLDataInstanceImplTest {

    @Mock
    private SDataInstance dataInstance;

    @Test
    public void SAXMLDataInstanceImpl_should_return_given_value() {
        //given
        final Serializable givenValue = new String("serializable value");
        final SAXMLDataInstanceImpl sxmlObjectDataInstanceImpl = new SAXMLDataInstanceImpl(dataInstance);

        //when
        sxmlObjectDataInstanceImpl.setValue(givenValue);
        final Serializable returnedValue = sxmlObjectDataInstanceImpl.getValue();

        //then
        assertThat(returnedValue).as("should be equal to given value").isEqualTo(givenValue);
    }

    @Test
    public void SAXMLDataInstanceImpl_should_return_null_when_no_given_value() {
        //given
        final SAXMLDataInstanceImpl sxmlObjectDataInstanceImpl = new SAXMLDataInstanceImpl(dataInstance);

        //when
        final Serializable returnedValue = sxmlObjectDataInstanceImpl.getValue();

        //then
        assertThat(returnedValue).as("should be not null").isNull();
    }

    @Test
    public void discriminator_should_not_be_null() {
        //given
        final SAXMLDataInstanceImpl sxmlObjectDataInstanceImpl = new SAXMLDataInstanceImpl();

        //when
        final String returnedValue = sxmlObjectDataInstanceImpl.getDiscriminator();

        //then
        assertThat(returnedValue).as("should be not null").isNotEmpty();
    }

    @Test
    public void nameSpaceTest() {
        //given
        final String expected = "namspace";
        final SAXMLDataInstanceImpl sxmlObjectDataInstanceImpl = new SAXMLDataInstanceImpl();

        //when
        sxmlObjectDataInstanceImpl.setNamespace(expected);
        final String returnedValue = sxmlObjectDataInstanceImpl.getNamespace();

        //then
        assertThat(returnedValue).as("should be not null").isSameAs(expected);
    }

    @Test
    public void elementTest() {
        //given
        final String expected = "namspace";
        final SAXMLDataInstanceImpl sxmlObjectDataInstanceImpl = new SAXMLDataInstanceImpl();

        //when
        sxmlObjectDataInstanceImpl.setElement(expected);
        final String returnedValue = sxmlObjectDataInstanceImpl.getElement();

        //then
        assertThat(returnedValue).as("should be not null").isSameAs(expected);
    }

}
