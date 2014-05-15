/*******************************************************************************
 * Copyright (C) 2014 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm.validator.rule;

import java.util.ArrayList;
import java.util.List;

import com.bonitasoft.engine.bdm.model.BusinessObject;
import com.bonitasoft.engine.bdm.model.BusinessObjectModel;
import com.bonitasoft.engine.bdm.model.field.RelationField;
import com.bonitasoft.engine.bdm.validator.ValidationStatus;

public class CompositionValidationRule extends ValidationRule<BusinessObjectModel> {

    public CompositionValidationRule() {
        super(BusinessObjectModel.class);
    }

    @Override
    ValidationStatus validate(BusinessObjectModel bom) {
        List<RelationField> compositionFields = bom.getCompositionFields();
        return getValidationStatus(compositionFields);
    }

    // TODO rename
    // TODO retrieve only composite BO, not relations
    private ValidationStatus getValidationStatus(List<RelationField> compositionFields) {
        ValidationStatus validationStatus = new ValidationStatus();

        List<BusinessObject> compositeBOs = new ArrayList<BusinessObject>();
        for (RelationField compositionField : compositionFields) {
            if (compositeBOs.contains(compositionField.getReference())) {
                validationStatus.addError("Business object " + compositionField.getReference().getQualifiedName()
                        + " is composed in two business objects");
            } else {
                compositeBOs.add(compositionField.getReference());
            }
        }
        return validationStatus;
    }
}
