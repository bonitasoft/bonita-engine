/*******************************************************************************
 * Copyright (C) 2014 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
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
