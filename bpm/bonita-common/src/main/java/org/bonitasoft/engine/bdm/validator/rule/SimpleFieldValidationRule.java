/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.bdm.validator.rule;

import java.util.Collections;

import org.bonitasoft.engine.api.result.StatusCode;
import org.bonitasoft.engine.api.result.StatusContext;
import org.bonitasoft.engine.bdm.model.field.SimpleField;
import org.bonitasoft.engine.bdm.validator.ValidationStatus;

/**
 * @author Colin PUY
 */
public class SimpleFieldValidationRule extends ValidationRule<SimpleField, ValidationStatus> {

    public SimpleFieldValidationRule() {
        super(SimpleField.class);
    }

    @Override
    public ValidationStatus validate(SimpleField field) {
        final ValidationStatus status = new ValidationStatus();
        if (field.getType() == null) {
            status.addError(StatusCode.FIELD_WITHOUT_NAME,
                    String.format("%s must have a type declared", field.getName()),
                    Collections.singletonMap(StatusContext.BDM_ARTIFACT_NAME_KEY, field.getName()));
        }
        return status;
    }

}
