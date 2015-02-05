/*******************************************************************************
 * Copyright (C) 2014 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package org.bonitasoft.engine.bdm.validator.rule;

import org.bonitasoft.engine.bdm.model.BusinessObjectModel;
import org.bonitasoft.engine.bdm.validator.UniqueNameValidator;
import org.bonitasoft.engine.bdm.validator.ValidationStatus;

/**
 * @author Colin PUY
 */
public class UniqueNameValidationRule extends ValidationRule<BusinessObjectModel> {

    private UniqueNameValidator uniqueNameValidator;

    public UniqueNameValidationRule(UniqueNameValidator uniqueNameValidator) {
        super(BusinessObjectModel.class);
        this.uniqueNameValidator = uniqueNameValidator;
    }

    @Override
    protected ValidationStatus validate(BusinessObjectModel bom) {
        ValidationStatus validationStatus = new ValidationStatus();
        validationStatus.addValidationStatus(uniqueNameValidator.validate(bom.getUniqueConstraints(), "unique contraints"));
        validationStatus.addValidationStatus(uniqueNameValidator.validate(bom.getIndexes(), "indexes"));
        return validationStatus;
    }
}
