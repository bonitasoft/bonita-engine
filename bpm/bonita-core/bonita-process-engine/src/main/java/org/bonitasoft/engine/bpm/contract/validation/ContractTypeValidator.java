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
 */
package org.bonitasoft.engine.bpm.contract.validation;

import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.core.process.definition.model.SInputDefinition;
import org.bonitasoft.engine.core.process.definition.model.SType;

/**
 * Validate that a value is assignable to a given contract type
 *
 * @author Colin Puy
 */
public class ContractTypeValidator {

    public void validate(final SInputDefinition definition, final Object object) throws InputValidationException {

        if (definition.hasChildren()) {
            if (!isValidForComplexType(definition, object)) {
                throw new InputValidationException(object + " cannot be assigned to COMPLEX type");
            }
        } else {
            if (!isValidForSimpleType(definition, object)) {
                throw new InputValidationException(object + " cannot be assigned to " + definition.getType());
            }

        }
    }

    private boolean isValidForSimpleType(final SInputDefinition definition, final Object object) {
        if (definition.isMultiple()) {
            return isValidForMultipleSimpleType(definition, object);
        } else {
            SType type = definition.getType();
            return type != null && type.validate(object);
        }
    }

    @SuppressWarnings("unchecked")
    private boolean isValidForMultipleSimpleType(final SInputDefinition definition, final Object object) {
        if (!(object instanceof List<?>)) {
            return false;
        }
        for (final Object item : (List<Object>) object) {
            if (!definition.getType().validate(item)) {
                return false;
            }
        }
        return true;
    }

    private boolean isValidForComplexType(final SInputDefinition definition, final Object object) throws InputValidationException {
        if (definition.isMultiple()) {
            return isValidForMultipleComplexType(definition, object);
        } else {
            return isValidForSimpleComplexType(definition, object);

        }
    }

    @SuppressWarnings("unchecked")
    private boolean isValidForMultipleComplexType(final SInputDefinition definition, final Object object) throws InputValidationException {
        if (!(object instanceof List<?>)) {
            return false;
        }
        for (final Object item : (List<Object>) object) {
            //throws exception if invalid
            isValidForSimpleComplexType(definition, item);
        }
        return true;
    }

    private boolean isValidForSimpleComplexType(final SInputDefinition definition, final Object object) throws InputValidationException {
        try {
            @SuppressWarnings("unchecked")
            final Map<String, Object> map = (Map<String, Object>) object;
            for (final SInputDefinition sInputDefinition : definition.getInputDefinitions()) {
                validate(sInputDefinition, map.get(sInputDefinition.getName()));
            }
            return map != null;
        } catch (final ClassCastException e) {
            return false;
        }
    }
}
