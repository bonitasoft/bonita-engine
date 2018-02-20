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
package org.bonitasoft.engine.bdm.validator.rule;

import java.util.List;

import org.bonitasoft.engine.bdm.model.UniqueConstraint;
import org.bonitasoft.engine.bdm.validator.SQLNameValidator;
import org.bonitasoft.engine.bdm.validator.ValidationStatus;

public class UniqueConstraintValidationRule extends ValidationRule<UniqueConstraint, ValidationStatus> {

    private static final int MAX_CONSTRAINTNAME_LENGTH = 25;

    private final SQLNameValidator sqlNameValidator;

    public UniqueConstraintValidationRule() {
        super(UniqueConstraint.class);
        sqlNameValidator = new SQLNameValidator(MAX_CONSTRAINTNAME_LENGTH);
    }

    @Override
    public ValidationStatus validate(final UniqueConstraint uc) {
        final ValidationStatus status = new ValidationStatus();
        final String name = uc.getName();
        if (name == null || name.isEmpty()) {
            status.addError("A unique constraint must have name");
            return status;
        }
        final boolean isValid = sqlNameValidator.isValid(name);
        if (!isValid) {
            status.addError(name + " is not a valid SQL identifier");
        }

        List<String> fieldNames = uc.getFieldNames();
        if (fieldNames == null || fieldNames.isEmpty()) {
            status.addError(name + " unique constraint must have at least one field declared");
        }
        return status;
    }

}
