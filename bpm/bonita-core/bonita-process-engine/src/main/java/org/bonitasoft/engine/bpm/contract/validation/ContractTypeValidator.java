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

    public void validate(SInputDefinition definition, Object object) throws InputValidationException {
        if (definition instanceof SSimpleInputDefinition) {
            SSimpleInputDefinition simpleDefinition = (SSimpleInputDefinition) definition;
            if (!isValidForSimpleType(simpleDefinition, object)) {
                throw new InputValidationException(object + " cannot be assigned to " + simpleDefinition.getType());
            }
        } else if (definition instanceof SComplexInputDefinition) {
            if (!isValidForComplexType(object)) {
                throw new InputValidationException(object + " cannot be assigned to COMPLEX type");
            }
        } 
    }

    private boolean isValidForSimpleType(SSimpleInputDefinition definition, Object object) {
        return definition.getType().validate(object);
    }

    private boolean isValidForComplexType(Object object) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) object;
            return map != null;
        } catch (ClassCastException e) {
            return false;
        }
    }
}
