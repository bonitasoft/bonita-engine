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

import org.bonitasoft.engine.bdm.model.Index;
import org.bonitasoft.engine.bdm.validator.SQLNameValidator;
import org.bonitasoft.engine.bdm.validator.ValidationStatus;

public class IndexValidationRule extends ValidationRule<Index> {

    private static final int MAX_CONSTRAINTNAME_LENGTH = 25;

    private final SQLNameValidator sqlNameValidator;

    public IndexValidationRule() {
        super(Index.class);
        sqlNameValidator = new SQLNameValidator(MAX_CONSTRAINTNAME_LENGTH);
    }

    @Override
    protected ValidationStatus validate(Index index) {
        final ValidationStatus status = new ValidationStatus();
        final String name = index.getName();
        if (name == null || name.isEmpty()) {
            status.addError("An index must have name");
            return status;
        }
        final boolean isValid = sqlNameValidator.isValid(name);
        if (!isValid) {
            status.addError(name + " is not a valid SQL identifier");
        }

        if (index.getFieldNames().isEmpty()) {
            status.addError(name + " index must have at least one field declared");
        }

        return status;
    }

}
