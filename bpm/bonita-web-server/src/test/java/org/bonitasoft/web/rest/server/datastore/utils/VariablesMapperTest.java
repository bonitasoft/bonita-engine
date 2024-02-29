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
import static org.hamcrest.Matchers.hasItems;

import org.bonitasoft.web.rest.server.framework.json.JacksonDeserializer;
import org.junit.Test;

public class VariablesMapperTest {

    private static final String JSON_VARIABLES = "[" +
            "{\"name\": \"variable1\", \"value\": \"newValue\"}," +
            "{\"name\": \"variable2\", \"value\": 9}," +
            "{\"name\": \"variable3\", \"value\": 349246800000}" +
            "]";

    @Test
    public void variablesMapper_convert_json_variable_list_to_variableMappers() throws Exception {

        VariablesMapper variablesMapper = VariablesMapper.fromJson(JSON_VARIABLES);

        assertThat(variablesMapper.getVariables(), hasItems(
                aVariableMapper("variable1", "newValue"),
                aVariableMapper("variable2", 9),
                aVariableMapper("variable3", 349246800000L)));
    }

    private VariableMapper aVariableMapper(String name, Object value) {
        Variable variable = new Variable();
        variable.set("name", name);
        variable.set("value", value);
        return new VariableMapper(variable, new JacksonDeserializer());
    }
}
