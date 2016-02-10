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

import java.util.List;

import org.bonitasoft.engine.core.process.definition.model.SContractDefinition;
import org.bonitasoft.engine.core.process.definition.model.SInputDefinition;

public class ConstraintsDefinitionHelper {

    protected SInputDefinition getInputDefinition(final SContractDefinition contract, final String inputName) {
        final List<SInputDefinition> inputs = contract.getInputDefinitions();
        return getInputDefinition(inputName, inputs);

    }

    private SInputDefinition getInputDefinition(final String inputName, final List<SInputDefinition> inputs) {
        SInputDefinition inputDefinition = getInputDefinitionInSimple(inputName, inputs);
        if (inputDefinition == null) {
            inputDefinition = getInputDefinitionInComplex(inputName, inputs);
        }
        return inputDefinition;
    }

    private SInputDefinition getInputDefinitionInSimple(final String inputName, final List<SInputDefinition> simpleInputs) {
        for (final SInputDefinition sInputDefinition : simpleInputs) {
            if (sInputDefinition.getName().equals(inputName)) {
                return sInputDefinition;
            }
        }
        return null;
    }

    private SInputDefinition getInputDefinitionInComplex(final String inputName, final List<SInputDefinition> complexInputs) {
        for (final SInputDefinition sInputDefinition : complexInputs) {
            if (sInputDefinition.getName().equals(inputName)) {
                return sInputDefinition;
            }
            final SInputDefinition inputDefinition = getInputDefinition(inputName, sInputDefinition.getInputDefinitions());
            if (inputDefinition != null) {
                return inputDefinition;
            }
        }
        return null;
    }
}
