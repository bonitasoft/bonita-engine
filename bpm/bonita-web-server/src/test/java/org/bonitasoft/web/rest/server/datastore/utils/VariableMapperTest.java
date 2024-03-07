/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.web.rest.server.datastore.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.Serializable;

import org.bonitasoft.web.rest.server.APITestWithMock;
import org.bonitasoft.web.rest.server.framework.json.JacksonDeserializer;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.junit.Before;
import org.junit.Test;

public class VariableMapperTest extends APITestWithMock {

    private JacksonDeserializer jacksonDeserializer;

    @Before
    public void setUp() {
        jacksonDeserializer = new JacksonDeserializer();
    }

    @Test(expected = APIException.class)
    public void getSerializableValue_throw_exception_if_class_is_not_in_classpath() {
        new VariableMapper(new Variable(), jacksonDeserializer).getSerializableValue("a.class.not.in.classpath");
    }

    @Test(expected = APIException.class)
    public void getSerializableValue_throw_exception_if_classname_name_an_unserializable_object() {
        Variable variable = new Variable();
        variable.set("value", new NotSerializableObject());

        new VariableMapper(variable, jacksonDeserializer).getSerializableValue(NotSerializableObject.class.getName());
    }

    @Test(expected = APIException.class)
    public void getSerializableValue_cannot_convert_values_that_ont_fits_to_given_className() {
        Variable variable = new Variable();
        variable.set("value", "coucou");

        Serializable value = new VariableMapper(variable, jacksonDeserializer)
                .getSerializableValue(Long.class.getName());

        assertThat(value, is(1L));
    }

    @Test
    public void getSerializableValue_return_variable_value_converted_in_given_class_name_object() {
        Variable variable = new Variable();
        variable.set("value", 1);

        Serializable value = new VariableMapper(variable, jacksonDeserializer)
                .getSerializableValue(Long.class.getName());

        assertThat(value, is(1L));
    }
}
