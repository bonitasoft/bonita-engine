/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm.validator.rule;

import com.bonitasoft.engine.bdm.model.Index;
import com.bonitasoft.engine.bdm.validator.SQLNameValidator;
import com.bonitasoft.engine.bdm.validator.ValidationStatus;

@Deprecated
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
