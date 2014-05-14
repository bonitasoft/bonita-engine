/*******************************************************************************
 * Copyright (C) 2014 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm.validator.rule;

import com.bonitasoft.engine.bdm.model.field.SimpleField;
import com.bonitasoft.engine.bdm.validator.ValidationStatus;

/**
 * @author Colin PUY
 */
public class SimpleFieldValidationRule extends ValidationRule<SimpleField> {

    @Override
    public boolean appliesTo(Object modelElement) {
        return modelElement instanceof SimpleField;
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
