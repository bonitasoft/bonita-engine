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

import javax.lang.model.SourceVersion;

import org.bonitasoft.engine.bdm.model.field.Field;
import org.bonitasoft.engine.bdm.validator.SQLNameValidator;
import org.bonitasoft.engine.bdm.validator.ValidationStatus;

/**
 * @author Romain Bioteau
 */
public class FieldValidationRule extends ValidationRule<Field> {

    private static final int MAX_COLUMNAME_LENGTH = 50;

    private final SQLNameValidator sqlNameValidator;

    public FieldValidationRule() {
        super(Field.class);
        sqlNameValidator = new SQLNameValidator(MAX_COLUMNAME_LENGTH);
    }

    @Override
    public ValidationStatus validate(final Field field) {
        final ValidationStatus status = new ValidationStatus();
        final String name = field.getName();
        if (name == null || !SourceVersion.isIdentifier(name) || SourceVersion.isKeyword(name) || isForbiddenIdentifier(name)) {
            status.addError(name + " is not a valid field identifier");
            return status;
        }
        return status;
    }

    private boolean isForbiddenIdentifier(final String name) {
        return Field.PERSISTENCE_ID.equalsIgnoreCase(name) || Field.PERSISTENCE_VERSION.equalsIgnoreCase(name) || !sqlNameValidator.isValid(name);
    }
}
