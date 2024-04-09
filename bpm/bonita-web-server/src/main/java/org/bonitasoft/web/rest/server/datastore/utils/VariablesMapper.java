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

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.web.rest.server.framework.json.JacksonDeserializer;
import org.bonitasoft.web.toolkit.client.common.util.StringUtil;

/**
 * Variables Mapper - Used for variables Json deserialization
 *
 * @author Colin PUY
 */
public class VariablesMapper {

    private final List<VariableMapper> variables;

    protected VariablesMapper(List<Variable> variables, JacksonDeserializer deserializer) {
        this.variables = convertToMappers(variables, deserializer);
    }

    public static VariablesMapper fromJson(String json) {
        JacksonDeserializer deserializer = new JacksonDeserializer();
        List<Variable> variables = deserializer.deserializeList(json, Variable.class);
        return new VariablesMapper(variables, deserializer);
    }

    private List<VariableMapper> convertToMappers(List<Variable> list, JacksonDeserializer deserializer) {
        ArrayList<VariableMapper> mappers = new ArrayList<>();
        for (Variable variable : list) {
            if (!StringUtil.isBlank(variable.getName())) {
                mappers.add(new VariableMapper(variable, deserializer));
            }
        }
        return mappers;
    }

    public List<VariableMapper> getVariables() {
        return variables;
    }

}
