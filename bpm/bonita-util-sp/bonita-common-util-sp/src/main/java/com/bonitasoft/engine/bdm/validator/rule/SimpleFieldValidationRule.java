/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm.validator.rule;

import com.bonitasoft.engine.bdm.model.field.SimpleField;
import com.bonitasoft.engine.bdm.validator.ValidationStatus;

/**
 * @author Colin PUY
 */
@Deprecated
public class SimpleFieldValidationRule extends ValidationRule<SimpleField> {

    public SimpleFieldValidationRule() {
        super(SimpleField.class);
    }

    @Override
    public ValidationStatus validate(SimpleField field) {
        final ValidationStatus status = new ValidationStatus();
        if (field.getType() == null) {
            status.addError(field.getName() + " must have a type declared");
        }
        return status;
    }

}
