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
package org.bonitasoft.engine.bpm.contract.validation;

import org.bonitasoft.engine.core.process.definition.model.SConstraintDefinition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class ContractVariableHelper {

    /**
     * @param constraint
     * @param contractVariables
     * @return
     *         list of elements that must be checked by this mandatory constraint
     */
    public List<Map<String, Serializable>> buildMandatoryMultipleInputVariables(final SConstraintDefinition constraint,
            final Map<String, Serializable> contractVariables) {
        final List<Map<String, Serializable>> constraintVariablesList = new ArrayList<Map<String, Serializable>>();
        for (final String inputName : constraint.getInputNames()) {
            buildRecursiveVariableList(contractVariables, constraintVariablesList, inputName);
        }
        return constraintVariablesList;

    }

    private List<Map<String, Serializable>> buildRecursiveVariableList(final Map<String, Serializable> currentVariables,
            final List<Map<String, Serializable>> constraintVariablesList,
            final String inputName) {
        if (currentVariables.containsKey(inputName)) {
            final Map<String, Serializable> value = new HashMap<String, Serializable>();
            value.put(inputName, currentVariables.get(inputName));
            constraintVariablesList.add(value);
        } else {
            buildRecursiveComplexVariableList(currentVariables, constraintVariablesList, inputName);
        }
        return constraintVariablesList;
    }

    @SuppressWarnings("unchecked")
    private Collection<? extends Map<String, Serializable>> buildRecursiveComplexVariableList(final Map<String, Serializable> currentVariables,
            final List<Map<String, Serializable>> constraintVariablesList, final String inputName) {
        for (final Entry<String, Serializable> variableEntry : currentVariables.entrySet()) {
            final Object variableValue = variableEntry.getValue();
            if (variableValue instanceof Map<?, ?>) {
                //complex case
                buildRecursiveVariableList((Map<String, Serializable>) variableValue, constraintVariablesList, inputName);
            }
            if (variableValue instanceof List<?>) {
                //multiple case
                for (final Serializable variableValueItem : (List<Serializable>) variableValue) {
                    if (variableValueItem instanceof Map<?, ?>) {
                        //complex case
                        buildRecursiveVariableList((Map<String, Serializable>) variableValueItem, constraintVariablesList, inputName);
                    }
                }
            }
        }
        return constraintVariablesList;
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Serializable>> convertMultipleToList(final Map<String, Serializable> multipleVariable) {
        final List<Map<String, Serializable>> multipleVariables = new ArrayList<Map<String, Serializable>>();
        final Set<Entry<String, Serializable>> entrySet = multipleVariable.entrySet();
        for (final Entry<String, Serializable> entry : entrySet) {
            if (entry.getValue() instanceof List<?>) {
                for (final Serializable value : (List<Serializable>) entry.getValue()) {
                    final Map<String, Serializable> item = new HashMap<String, Serializable>();
                    item.put(entry.getKey(), value);
                    multipleVariables.add(item);
                }
            }
        }
        return multipleVariables;
    }

}
