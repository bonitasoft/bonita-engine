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
import java.util.Map;

import org.bonitasoft.engine.core.process.definition.model.SComplexInputDefinition;
import org.bonitasoft.engine.core.process.definition.model.SInputDefinition;
import org.bonitasoft.engine.core.process.definition.model.SSimpleInputDefinition;

/**
 * Validate that a value is assignable to a given contract type
 *
 * @author Colin Puy
 */
public class ContractTypeValidator {

    public void validate(final SInputDefinition definition, final Object object) throws InputValidationException {
        if (definition instanceof SSimpleInputDefinition) {
            final SSimpleInputDefinition simpleDefinition = (SSimpleInputDefinition) definition;
            if (!isValidForSimpleType(simpleDefinition, object)) {
                throw new InputValidationException(object + " cannot be assigned to " + simpleDefinition.getType());
            }
        }
        if (definition instanceof SComplexInputDefinition) {
            if (!isValidForComplexType((SComplexInputDefinition) definition, object)) {
                throw new InputValidationException(object + " cannot be assigned to COMPLEX type");
            }
        }
    }

    private boolean isValidForSimpleType(final SSimpleInputDefinition definition, final Object object) {
        if (definition.isMultiple()) {
            return isValidForMultipleSimpleType(definition, object);
        }
        else {
            return definition.getType().validate(object);
        }
    }

    @SuppressWarnings("unchecked")
    private boolean isValidForMultipleSimpleType(final SSimpleInputDefinition definition, final Object object) {
        if (!(object instanceof List<?>))
        {
            return false;
        }
        for (final Object item : (List<Object>) object) {
            if (!definition.getType().validate(item)) {
                return false;
            }
        }
        return true;
    }

    private boolean isValidForComplexType(final SComplexInputDefinition definition, final Object object) throws InputValidationException {
        if (definition.isMultiple()) {
            return isValidForMultipleComplexType(definition, object);
        }
        else {
            return isValidForSimpleComplexType(definition, object);

        }
    }

    @SuppressWarnings("unchecked")
    private boolean isValidForMultipleComplexType(final SComplexInputDefinition definition, final Object object) throws InputValidationException {
        if (!(object instanceof List<?>)) {
            return false;
        }
        for (final Object item : (List<Object>) object) {
            //throws exception if invalid
            isValidForSimpleComplexType(definition, item);
        }
        return true;
    }

    private boolean isValidForSimpleComplexType(final SComplexInputDefinition definition, final Object object) throws InputValidationException {
        try {
            @SuppressWarnings("unchecked")
            final Map<String, Object> map = (Map<String, Object>) object;

            final List<SSimpleInputDefinition> simpleInputDefinitions = definition.getSimpleInputDefinitions();
            for (final SSimpleInputDefinition sSimpleInputDefinition : simpleInputDefinitions) {
                validate(sSimpleInputDefinition, map.get(sSimpleInputDefinition.getName()));
            }

            final List<SComplexInputDefinition> complexInputDefinitions = definition.getComplexInputDefinitions();
            for (final SComplexInputDefinition sComplexInputDefinition : complexInputDefinitions) {
                validate(sComplexInputDefinition, map.get(sComplexInputDefinition.getName()));
            }

            return map != null;
        } catch (final ClassCastException e) {
            return false;
        }
    }
}
