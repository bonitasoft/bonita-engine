/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm.validator.rule.composition;

import java.util.ArrayList;
import java.util.List;

import com.bonitasoft.engine.bdm.model.BusinessObject;
import com.bonitasoft.engine.bdm.model.BusinessObjectModel;
import com.bonitasoft.engine.bdm.validator.ValidationStatus;
import com.bonitasoft.engine.bdm.validator.rule.ValidationRule;

/**
 * Check that there are no circular references on referenced business objects by composition.
 * 
 * @author Colin PUY
 */
@Deprecated
public class CyclicCompositionValidationRule extends ValidationRule<BusinessObjectModel> {

    public CyclicCompositionValidationRule() {
        super(BusinessObjectModel.class);
    }

    @Override
    protected ValidationStatus validate(BusinessObjectModel bom) {
        ValidationStatus validationStatus = new ValidationStatus();
        for (BusinessObject bo : bom.getBusinessObjects()) {
            validationStatus.addValidationStatus(validateThatThereIsNoCycleDependencies(bo, new ArrayList<BusinessObject>()));
        }
        return validationStatus;
    }

    private ValidationStatus validateThatThereIsNoCycleDependencies(BusinessObject bo, List<BusinessObject> parentBOs) {
        ValidationStatus validationStatus = new ValidationStatus();
        parentBOs.add(bo);
        for (BusinessObject businessObject : bo.getReferencedBusinessObjectsByComposition()) {
            if (parentBOs.contains(businessObject)) {
                validationStatus.addError("Business object " + businessObject.getQualifiedName() + " has a circular composition reference to itself");
            } else {
                validationStatus.addValidationStatus(validateThatThereIsNoCycleDependencies(businessObject, parentBOs));
            }
        }
        return validationStatus;
    }
}
