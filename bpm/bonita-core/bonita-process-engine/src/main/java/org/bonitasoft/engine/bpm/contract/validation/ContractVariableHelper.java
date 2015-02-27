/**
 * Copyright (C) 2014 BonitaSoft S.A.
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
package org.bonitasoft.engine.bpm.contract.validation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bonitasoft.engine.core.process.definition.model.SConstraintDefinition;

public class ContractVariableHelper {

    public List<Map<String, Object>> buildMandatoryMultipleInputVariables(final SConstraintDefinition constraint, final Map<String, Object> contractVariables) {
        final List<Map<String, Object>> constraintVariablesList = new ArrayList<Map<String, Object>>();
        for (final String inputName : constraint.getInputNames()) {
            buildRecursiveVariableList(contractVariables, constraintVariablesList, inputName);
        }
        return constraintVariablesList;

    }

    private List<Map<String, Object>> buildRecursiveVariableList(final Map<String, Object> currentVariables,
            final List<Map<String, Object>> constraintVariablesList,
            final String inputName) {
        if (currentVariables.containsKey(inputName)) {
            final Map<String, Object> value = new HashMap<String, Object>();
            value.put(inputName, currentVariables.get(inputName));
            constraintVariablesList.add(value);
        } else {
            buildRecursiveComplexVariableList(currentVariables, constraintVariablesList, inputName);
        }
        return constraintVariablesList;
    }

    @SuppressWarnings("unchecked")
    private Collection<? extends Map<String, Object>> buildRecursiveComplexVariableList(final Map<String, Object> currentVariables,
            final List<Map<String, Object>> constraintVariablesList, final String inputName) {
        for (final Entry<String, Object> variableEntry : currentVariables.entrySet()) {
            final Object variableValue = variableEntry.getValue();
            if (variableValue instanceof Map<?, ?>) {
                //complex case
                buildRecursiveVariableList((Map<String, Object>) variableValue, constraintVariablesList, inputName);
            }
            if (variableValue instanceof List<?>) {
                //multiple case
                for (final Map<String, Object> variableValueItem : (List<Map<String, Object>>) variableValue) {
                    buildRecursiveVariableList(variableValueItem, constraintVariablesList, inputName);
                }
            }
        }
        return constraintVariablesList;
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> convertMultipleToList(final Map<String, Object> multipleVariable) {
        final List<Map<String, Object>> multipleVariables = new ArrayList<Map<String, Object>>();
        final Set<Entry<String, Object>> entrySet = multipleVariable.entrySet();
        for (final Entry<String, Object> entry : entrySet) {
            if (entry.getValue() instanceof List<?>) {
                for (final Object value : (List<Object>) entry.getValue()) {
                    final Map<String, Object> item = new HashMap<String, Object>();
                    item.put(entry.getKey(), value);
                    multipleVariables.add(item);
                }
            }
        }
        return multipleVariables;
    }

}
