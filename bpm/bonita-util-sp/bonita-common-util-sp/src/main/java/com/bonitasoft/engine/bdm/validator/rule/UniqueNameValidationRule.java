/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm.validator.rule;

import com.bonitasoft.engine.bdm.model.BusinessObjectModel;
import com.bonitasoft.engine.bdm.validator.UniqueNameValidator;
import com.bonitasoft.engine.bdm.validator.ValidationStatus;

/**
 * @author Colin PUY
 */
@Deprecated
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
