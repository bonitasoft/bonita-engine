/*******************************************************************************
 * Copyright (C) 2014 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm.validator.rule;

import com.bonitasoft.engine.bdm.BusinessObjectModel;
import com.bonitasoft.engine.bdm.validator.ValidationStatus;

/**
 * @author Romain Bioteau
 */
public class BusinessObjectModelValidationRule implements ValidationRule {

    @Override
    public boolean appliesTo(final Object modelElement) {
        return modelElement instanceof BusinessObjectModel;
    }

    @Override
    public ValidationStatus checkRule(final Object modelElement) {
        if (!appliesTo(modelElement)) {
            throw new IllegalArgumentException(BusinessObjectModelValidationRule.class.getName() + " doesn't handle validation for "
                    + modelElement.getClass().getName());
        }
        final BusinessObjectModel bom = (BusinessObjectModel) modelElement;
        final ValidationStatus status = new ValidationStatus();
        if (bom.getBusinessObjects().isEmpty()) {
            status.addError("Business object model must have at least one business object declared");
        }
        return status;
    }
}
